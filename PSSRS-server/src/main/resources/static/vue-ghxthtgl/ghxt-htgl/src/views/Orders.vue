<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <div>订单管理</div>
        <el-space>
          <el-input v-model="query.orderNo" placeholder="订单号" clearable style="width:180px" />
          <el-input v-model="query.patientName" placeholder="患者姓名" clearable style="width:160px" />
          <el-select v-model="query.status" placeholder="支付状态" clearable style="width:140px">
            <el-option label="待支付" value="0" />
            <el-option label="已支付" value="1" />
            <el-option label="已退款" value="2" />
          </el-select>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-space>
      </div>
    </template>

    <el-table v-if="!isCardView" :data="rows" :loading="loading" style="width:100%">
      <el-table-column prop="orderNo" label="订单号" min-width="220" />
      <el-table-column prop="patientName" label="患者" min-width="120" />
      <el-table-column prop="amount" label="金额" width="100">
        <template #default="{ row }">{{ Number(row.amount || 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="支付方式" width="180">
        <template #default="{ row }">
          <div class="pay-chip" :class="getPayWayMeta(row.payWay ?? row.pay_way ?? row.payMethod ?? row.pay_method, row).key" @click="openPayDetail(row)" role="button" tabindex="0" @keydown.enter.prevent="openPayDetail(row)" @keydown.space.prevent="openPayDetail(row)" :aria-label="getPayWayMeta(row.payWay ?? row.pay_way ?? row.payMethod ?? row.pay_method, row).label">
            <span class="pay-dot"></span>
            <span class="pay-chip-text">{{ getPayWayMeta(row.payWay ?? row.pay_way ?? row.payMethod ?? row.pay_method, row).label }}</span>
            <span v-if="getPayWayMeta(row.payWay ?? row.pay_way ?? row.payMethod ?? row.pay_method, row).desc" class="pay-chip-desc">{{ getPayWayMeta(row.payWay ?? row.pay_way ?? row.payMethod ?? row.pay_method, row).desc }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="payTime" label="支付时间" min-width="160" />
      <el-table-column label="支付状态" width="140">
        <template #default="{ row }">
          <transition name="fade">
            <el-tag :type="getStatusMeta(row.status).type" class="status-badge" :aria-label="getStatusMeta(row.status).label">
              <el-icon :size="16"><component :is="getStatusMeta(row.status).icon" /></el-icon>
              <span class="status-text">{{ getStatusMeta(row.status).label }}</span>
            </el-tag>
          </transition>
        </template>
      </el-table-column>
    </el-table>

    <div v-else class="cards-grid">
      <el-card v-for="row in rows" :key="row.orderNo" class="order-card" shadow="hover" @click="handleCardClick(row)">
        <template #header>
          <div class="card-header">{{ row.orderNo }}</div>
        </template>
        <div class="card-body">
          <div v-if="row.imageUrl" class="card-image">
            <img :src="cacheBustUrl(row.imageUrl)" alt="image" />
          </div>
          <div class="card-content">
            <div>患者：{{ row.patientName }}</div>
            <div>金额：¥{{ Number(row.amount || 0).toFixed(2) }}</div>
            <div class="card-line">
              <span>支付方式：</span>
              <span class="pay-chip" :class="getPayWayMeta(row.payWay ?? row.pay_way ?? row.payMethod ?? row.pay_method, row).key" @click="openPayDetail(row)" role="button" tabindex="0" @keydown.enter.prevent="openPayDetail(row)" @keydown.space.prevent="openPayDetail(row)" :aria-label="getPayWayMeta(row.payWay ?? row.pay_way ?? row.payMethod ?? row.pay_method, row).label">
                <span class="pay-dot"></span>
                <span class="pay-chip-text">{{ getPayWayMeta(row.payWay ?? row.pay_way ?? row.payMethod ?? row.pay_method, row).label }}</span>
                <span v-if="getPayWayMeta(row.payWay ?? row.pay_way ?? row.payMethod ?? row.pay_method, row).desc" class="pay-chip-desc">{{ getPayWayMeta(row.payWay ?? row.pay_way ?? row.payMethod ?? row.pay_method, row).desc }}</span>
              </span>
            </div>
            <div class="card-line">
              <span>支付状态：</span>
              <el-tag :type="getStatusMeta(row.status).type" class="status-badge" :aria-label="getStatusMeta(row.status).label">
                <el-icon :size="16"><component :is="getStatusMeta(row.status).icon" /></el-icon>
                <span class="status-text">{{ getStatusMeta(row.status).label }}</span>
              </el-tag>
            </div>
            <div>支付时间：{{ row.payTime || '-' }}</div>
          </div>
        </div>
        <div class="card-actions" @click.stop>
          <el-button type="primary" size="small" @click="handleCardClick(row)">详情</el-button>
          <el-button size="small">更多</el-button>
        </div>
      </el-card>
    </div>

    <div style="display:flex;justify-content:flex-end;margin-top:12px">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :page-sizes="[10,20,50,100]"
        @current-change="fetchList"
        @size-change="fetchList"
      />
    </div>

    <el-dialog v-model="detailVisible" title="订单详情" width="520px">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="订单号">{{ detailData.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="患者">{{ detailData.patientName }}</el-descriptions-item>
        <el-descriptions-item label="金额">¥{{ Number(detailData.amount || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ mapPayMethod(detailData.payWay ?? detailData.pay_way ?? detailData.payMethod ?? detailData.pay_method) }}</el-descriptions-item>
        <el-descriptions-item label="支付状态">{{ mapStatus(detailData.status) }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ detailData.payTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="payDetailVisible" title="支付方式详情" width="440px">
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="支付方式">{{ payDetailData?.method?.label }}</el-descriptions-item>
        <el-descriptions-item v-if="payDetailData?.method?.desc" label="附加信息">{{ payDetailData?.method?.desc }}</el-descriptions-item>
        <el-descriptions-item label="订单号">{{ payDetailData?.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="金额">¥{{ Number(payDetailData?.amount || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="患者">{{ payDetailData?.patientName }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ payDetailData?.payTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="payDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted, inject, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { CircleCheck, WarningFilled, CloseBold, CreditCard } from '@element-plus/icons-vue'
import { fetchAdminOrders } from '../api/orders'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const dateRange = ref([])
const uiViewMode = inject('ui_view_mode', ref('card'))
const isCardView = computed(() => (uiViewMode?.value || 'card') === 'card')
const detailVisible = ref(false)
const detailData = ref({})
const cacheBust = ref(Date.now())

const query = reactive({
  page: 1,
  size: 10,
  orderNo: '',
  patientName: '',
  status: ''
})

function mapStatus(s) {
  if (s === 0 || s === '0') return '待支付'
  if (s === 1 || s === '1') return '已支付'
  if (s === 2 || s === '2') return '已退款'
  return '-'
}

function mapPayMethod(m) {
  if (m === 1 || m === '1') return '微信'
  if (m === 2 || m === '2') return '支付宝'
  if (m === 3 || m === '3') return '银行卡'
  return '未选择'
}

function getStatusMeta(s) {
  if (s === 1 || s === '1') return { label: '已支付', type: 'success', icon: CircleCheck, key: 'paid' }
  if (s === 2 || s === '2') return { label: '已退款', type: 'danger', icon: CloseBold, key: 'refunded' }
  return { label: '待支付', type: 'warning', icon: WarningFilled, key: 'pending' }
}

function getPayWayKey(m) {
  if (m === 1 || m === '1') return 'wechat'
  if (m === 2 || m === '2') return 'alipay'
  if (m === 3 || m === '3') return 'bankcard'
  return 'none'
}

function getPayWayMeta(m, row = {}) {
  const key = getPayWayKey(m)
  const base = {
    wechat: { label: '微信', color: '#09BB07' },
    alipay: { label: '支付宝', color: '#1677FF' },
    bankcard: { label: '银行卡', color: '#4e5969' },
    none: { label: '未选择', color: '#909399' }
  }[key]
  const desc = row.cardLast4 || row.card_tail || row.cardSuffix || row.accountName || row.payAccount || ''
  return { key, ...base, desc: desc ? String(desc) : '' }
}

const payDetailVisible = ref(false)
const payDetailData = ref(null)
function openPayDetail(row) {
  payDetailData.value = {
    method: getPayWayMeta(row.payWay ?? row.pay_way ?? row.payMethod ?? row.pay_method, row),
    orderNo: row.orderNo,
    amount: row.amount,
    patientName: row.patientName,
    payTime: row.payTime
  }
  payDetailVisible.value = true
}

async function fetchList() {
  loading.value = true
  try {
    const params = { ...query }
    if (Array.isArray(dateRange.value) && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const data = await fetchAdminOrders(params)
    rows.value = (data.records || []).map(r => ({
      ...r,
      statusText: mapStatus(r.status)
    }))
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error(e?.msg || e?.message || '获取订单列表失败')
  } finally {
    loading.value = false
  }
}

function onSearch() {
  query.page = 1
  fetchList()
}

function onReset() {
  query.page = 1
  query.size = 10
  query.orderNo = ''
  query.patientName = ''
  query.status = ''
  dateRange.value = []
  fetchList()
}

function handleCardClick(row) {
  detailData.value = { ...row }
  detailVisible.value = true
}

function cacheBustUrl(u) {
  if (!u) return ''
  const sep = u.includes('?') ? '&' : '?'
  return `${u}${sep}_t=${cacheBust.value}`
}

onMounted(() => { fetchList() })
</script>

<style scoped>
.cards-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}
@media (max-width: 1200px) { .cards-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); } }
@media (max-width: 768px) { .cards-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 480px) { .cards-grid { grid-template-columns: 1fr; } }

.order-card { border-radius: 12px; transition: all .2s ease; }
.order-card:hover { transform: translateY(-2px); box-shadow: 0 10px 24px rgba(0,0,0,.08); }
.card-header { font-weight: 600; font-size: 16px; }
.card-body { display: flex; gap: 12px; }
.card-image img { width: 96px; height: 96px; object-fit: cover; border-radius: 8px; }
.card-content { font-size: 14px; line-height: 22px; flex: 1; }
.card-actions { display: flex; justify-content: flex-end; gap: 8px; margin-top: 8px; }

@media (prefers-color-scheme: dark) {
  .order-card { background: #1e1e1e; color: #e5eaf3; }
}

.status-badge { display:inline-flex; align-items:center; gap:6px; transition: all .3s ease }
.status-text { font-weight:500 }
.pay-chip { display:inline-flex; align-items:center; gap:6px; padding:4px 8px; border-radius:16px; border:1px solid var(--el-border-color); background:#fff; color:#606266; cursor:pointer; transition: all .2s ease }
.pay-chip:hover { box-shadow: 0 4px 12px rgba(0,0,0,.08); transform: translateY(-1px) }
.pay-dot { width:8px; height:8px; border-radius:50%; background:#909399 }
.pay-chip.wechat .pay-dot { background:#09BB07 }
.pay-chip.alipay .pay-dot { background:#1677FF }
.pay-chip.bankcard .pay-dot { background:#4e5969 }
.pay-chip.none { opacity:.8 }
.pay-chip-desc { font-size:12px; color:#909399 }
.card-line { display:flex; align-items:center; gap:8px }

.fade-enter-active, .fade-leave-active { transition: opacity .3s ease }
.fade-enter-from, .fade-leave-to { opacity: 0 }
</style>


