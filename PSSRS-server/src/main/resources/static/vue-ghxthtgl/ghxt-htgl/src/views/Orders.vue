<template>
  <el-card class="page-container">
    <template #header>
      <div class="page-header">
        <div class="header-title">
          <div class="title-decoration"></div>
          <span>订单管理</span>
        </div>
        <div class="header-actions">
          <el-input 
            v-model="query.orderNo" 
            placeholder="订单号" 
            clearable 
            style="width: 180px" 
            :prefix-icon="Search"
          />
          <el-input 
            v-model="query.patientName" 
            placeholder="患者姓名" 
            clearable 
            style="width: 140px" 
            :prefix-icon="User"
          />
          <el-select v-model="query.status" placeholder="支付状态" clearable style="width: 120px">
            <el-option label="待支付" value="0" />
            <el-option label="已支付" value="1" />
            <el-option label="已取消" value="2" />
            <el-option label="退款中" value="3" />
            <el-option label="已退款" value="4" />
          </el-select>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
          <el-button type="primary" @click="onSearch" :icon="Search">查询</el-button>
          <el-button @click="onReset" :icon="RefreshRight">重置</el-button>
        </div>
      </div>
    </template>

    <el-table 
      v-if="!isCardView" 
      :data="rows" 
      :loading="loading" 
      style="width:100%"
      stripe
      highlight-current-row
      header-cell-class-name="table-header"
    >
      <el-table-column prop="orderNo" label="订单号" min-width="220">
        <template #default="{ row }">
          <span class="mono-font">{{ row.orderNo }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="patientName" label="患者" min-width="120" />
      <el-table-column prop="amount" label="金额" width="120">
        <template #default="{ row }">
          <span class="price-text">¥{{ Number(row.amount || 0).toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="支付方式" width="180">
        <template #default="{ row }">
          <div class="pay-chip" :class="getPayWayMeta(row.payWay ?? row.pay_way ?? row.payMethod ?? row.pay_method, row).key" @click="openPayDetail(row)">
            <span class="pay-dot"></span>
            <span class="pay-chip-text">{{ getPayWayMeta(row.payWay ?? row.pay_way ?? row.payMethod ?? row.pay_method, row).label }}</span>
            <span v-if="getPayWayMeta(row.payWay ?? row.pay_way ?? row.payMethod ?? row.pay_method, row).desc" class="pay-chip-desc">{{ getPayWayMeta(row.payWay ?? row.pay_way ?? row.payMethod ?? row.pay_method, row).desc }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="payTime" label="支付时间" min-width="180">
        <template #default="{ row }">
          <span class="mono-font">{{ row.payTime || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="支付状态" width="140" align="center" fixed="right">
        <template #default="{ row }">
          <el-tag :type="getStatusMeta(row.status).type" effect="light" class="status-tag">
            {{ getStatusMeta(row.status).label }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <div v-else class="cards-grid">
      <el-card v-for="row in rows" :key="row.orderNo" class="order-card" shadow="hover" @click="handleCardClick(row)">
        <div class="card-header-row">
          <div class="order-no">{{ row.orderNo }}</div>
          <el-tag :type="getStatusMeta(row.status).type" size="small">{{ getStatusMeta(row.status).label }}</el-tag>
        </div>
        
        <div class="card-content">
          <div class="info-row">
            <span class="label">患者：</span>
            <span class="value">{{ row.patientName }}</span>
          </div>
          <div class="info-row">
            <span class="label">金额：</span>
            <span class="value price">¥{{ Number(row.amount || 0).toFixed(2) }}</span>
          </div>
          <div class="info-row">
            <span class="label">支付：</span>
            <div class="pay-chip small" :class="getPayWayMeta(row.payWay ?? row.pay_way ?? row.payMethod ?? row.pay_method, row).key">
              <span class="pay-dot"></span>
              <span>{{ getPayWayMeta(row.payWay ?? row.pay_way ?? row.payMethod ?? row.pay_method, row).label }}</span>
            </div>
          </div>
          <div class="info-row">
            <span class="label">时间：</span>
            <span class="value mono">{{ row.payTime || '-' }}</span>
          </div>
        </div>

        <div class="card-actions">
          <el-button type="primary" link @click.stop="handleCardClick(row)">查看详情</el-button>
        </div>
      </el-card>
    </div>

    <div class="pagination-container">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :page-sizes="[10, 20, 50, 100]"
        @current-change="fetchList"
        @size-change="fetchList"
      />
    </div>

    <el-dialog v-model="detailVisible" title="订单详情" width="500px" align-center class="custom-dialog">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="订单号">{{ detailData.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="患者姓名">{{ detailData.patientName }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">
          <span class="price-text">¥{{ Number(detailData.amount || 0).toFixed(2) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">
          {{ mapPayMethod(detailData.payWay ?? detailData.pay_way ?? detailData.payMethod ?? detailData.pay_method) }}
        </el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="getStatusMeta(detailData.status).type">{{ mapStatus(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ detailData.payTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="payDetailVisible" title="支付详情" width="400px" align-center class="custom-dialog">
      <div class="pay-detail-content">
        <div class="pay-amount">¥{{ Number(payDetailData?.amount || 0).toFixed(2) }}</div>
        <div class="pay-status">
          <el-icon color="#67C23A" size="20"><CircleCheckFilled /></el-icon>
          <span>支付成功</span>
        </div>
        <div class="pay-info-list">
          <div class="info-item">
            <span>支付方式</span>
            <span>{{ payDetailData?.method?.label }}</span>
          </div>
          <div class="info-item" v-if="payDetailData?.method?.desc">
            <span>支付账号</span>
            <span>{{ payDetailData?.method?.desc }}</span>
          </div>
          <div class="info-item">
            <span>交易时间</span>
            <span>{{ payDetailData?.payTime || '-' }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="payDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted, inject, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { CircleCheck, WarningFilled, CloseBold, CreditCard, RemoveFilled, Refresh, Search, User, RefreshRight, CircleCheckFilled } from '@element-plus/icons-vue'
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
  if (s === 2 || s === '2') return '已取消'
  if (s === 3 || s === '3') return '退款中'
  if (s === 4 || s === '4') return '已退款'
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
  if (s === 2 || s === '2') return { label: '已取消', type: 'info', icon: CloseBold, key: 'cancelled' }
  if (s === 3 || s === '3') return { label: '退款中', type: 'primary', icon: Refresh, key: 'refunding' }
  if (s === 4 || s === '4') return { label: '已退款', type: 'info', icon: RemoveFilled, key: 'refunded' }
  return { label: '待支付', type: 'warning', icon: WarningFilled, key: 'pending' }
}

function getPayWayKey(m) {
  if (m === 0 || m === '0') return 'wechat'
  if (m === 1 || m === '1') return 'alipay'
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
.page-container {
  min-height: calc(100vh - 120px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.title-decoration {
  width: 4px;
  height: 18px;
  background: var(--color-primary);
  border-radius: 2px;
  margin-right: 12px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.mono-font {
  font-family: 'Roboto Mono', monospace;
  color: var(--color-text-primary);
}

.price-text {
  font-weight: 600;
  color: var(--color-text-primary);
}

.status-tag {
  min-width: 80px;
  justify-content: center;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}

/* Card View */
.cards-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
}

@media (max-width: 1600px) { .cards-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); } }
@media (max-width: 1200px) { .cards-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 768px) { .cards-grid { grid-template-columns: 1fr; } }

.order-card { 
  border-radius: 12px; 
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
}

.order-card:hover { 
  transform: translateY(-4px); 
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
  border-color: var(--color-primary-soft);
}

.order-card :deep(.el-card__body) {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
}

.card-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--color-border);
}

.order-no {
  font-family: 'Roboto Mono', monospace;
  font-weight: 600;
  font-size: 13px;
  color: var(--color-text-secondary);
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.info-row .label {
  color: var(--color-text-secondary);
}

.info-row .value {
  color: var(--color-text-primary);
  font-weight: 500;
}

.info-row .value.price {
  font-size: 16px;
  font-weight: 700;
}

.info-row .value.mono {
  font-family: monospace;
  font-size: 13px;
  color: var(--color-text-secondary);
}

.card-actions {
  display: flex;
  justify-content: center;
  padding-top: 12px;
  border-top: 1px dashed var(--color-border);
}

.pay-chip { 
  display: inline-flex; 
  align-items: center; 
  gap: 6px; 
  padding: 4px 10px; 
  border-radius: 16px; 
  background: #f8fafc; 
  border: 1px solid var(--color-border);
  cursor: pointer; 
  transition: all 0.2s ease;
  font-size: 13px;
}

.pay-chip.small {
  padding: 2px 8px;
  font-size: 12px;
}

.pay-chip:hover { 
  background: #fff;
  border-color: var(--color-primary);
  color: var(--color-primary);
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.pay-dot { width: 6px; height: 6px; border-radius: 50%; background: #94a3b8; }
.pay-chip.wechat .pay-dot { background: #07c160; }
.pay-chip.alipay .pay-dot { background: #1677ff; }
.pay-chip.bankcard .pay-dot { background: #f59e0b; }

.pay-chip-text { font-weight: 500; }
.pay-chip-desc { font-size: 12px; color: var(--color-text-secondary); margin-left: 4px; }

/* Pay Detail Dialog */
.pay-detail-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.pay-amount {
  font-size: 32px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 8px;
}

.pay-status {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #67c23a;
  margin-bottom: 32px;
  font-size: 14px;
}

.pay-info-list {
  width: 100%;
  background: #f8fafc;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.pay-info-list .info-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.pay-info-list .info-item span:first-child {
  color: var(--color-text-secondary);
}

.pay-info-list .info-item span:last-child {
  color: var(--color-text-primary);
  font-weight: 500;
}

:deep(.table-header) {
  background-color: #f8fafc !important;
  color: var(--color-text-primary);
  font-weight: 600;
  height: 50px;
}
</style>


