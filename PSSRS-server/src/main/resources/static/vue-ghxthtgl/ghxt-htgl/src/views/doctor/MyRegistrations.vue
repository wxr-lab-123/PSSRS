<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <div>我的挂号</div>
        <el-space>
          <el-date-picker v-model="query.date" type="date" placeholder="预约日期" value-format="YYYY-MM-DD" />
          <el-select v-model="query.status" placeholder="状态" clearable style="width:140px">
            <el-option label="待就诊" value="0" />
            <el-option label="已完成" value="1" />
            <el-option label="已取消" value="2" />
          </el-select>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-space>
      </div>
    </template>
    <el-table v-if="!isCardView" :data="rows" :loading="loading" style="width:100%">
      <el-table-column prop="orderNo" label="订单号" width="160" />
      <el-table-column prop="patientName" label="患者" />
      <el-table-column prop="departmentName" label="科室" />
      <el-table-column prop="appointmentDate" label="日期" />
      <el-table-column prop="timeSlot" label="时段" />
      <el-table-column prop="fee" label="费用" width="100" />
      <el-table-column prop="statusText" label="状态" width="100" />
    </el-table>

    <div v-else class="cards-grid">
      <el-card v-for="row in rows" :key="row.orderNo || row.id" class="reg-card" shadow="hover">
        <template #header>
          <div class="card-header">{{ row.orderNo }}</div>
        </template>
        <div class="card-content">
          <div>患者：{{ row.patientName }}</div>
          <div>科室：{{ row.departmentName }}</div>
          <div>日期/时段：{{ row.appointmentDate }} / {{ row.timeSlot }}</div>
          <div>费用：¥{{ row.fee }}</div>
          <div>状态：{{ row.statusText }}</div>
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
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted, inject, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchDoctorOrders } from '../../api/orders'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10, date: '', status: '' })

function mapStatus(s) { return s === 0 ? '待就诊' : s === 1 ? '已完成' : s === 2 ? '已取消' : '-' }

async function fetchList() {
  loading.value = true
  try {
    const params = { ...query }
    const data = await fetchDoctorOrders(params)
    rows.value = (data.records || []).map(r => ({
      ...r,
      statusText: mapStatus(r.status)
    }))
    total.value = data.total || 0
  } catch (e) { ElMessage.error(e?.msg || e?.message || '获取列表失败') }
  finally { loading.value = false }
}

function onSearch() { query.page = 1; fetchList() }
function onReset() { Object.assign(query, { page: 1, size: 10, date: '', status: '' }); fetchList() }

const uiViewMode = inject('ui_view_mode', ref('card'))
const isCardView = computed(() => (uiViewMode?.value || 'card') === 'card')

onMounted(fetchList)
</script>

<style scoped>
.cards-grid { display:grid; grid-template-columns:repeat(4,minmax(0,1fr)); gap:16px }
@media (max-width:1200px){ .cards-grid{ grid-template-columns:repeat(3,minmax(0,1fr)) } }
@media (max-width:900px){ .cards-grid{ grid-template-columns:repeat(2,minmax(0,1fr)) } }
@media (max-width:600px){ .cards-grid{ grid-template-columns:1fr } }
.reg-card { border-radius:8px; transition: all .2s ease }
.reg-card:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,.15) }
.card-header { font-weight:600; font-size:16px }
.card-content { font-size:14px; line-height:1.6; padding: 0 16px; display:flex; flex-direction:column; gap:12px }
</style>


