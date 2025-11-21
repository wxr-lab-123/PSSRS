<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <div>挂号管理</div>
        <el-space>
          <el-input
            v-model="query.keyword"
            placeholder="患者姓名 / 订单号"
            clearable
            style="width:220px"
          />
          <el-date-picker
            v-model="query.date"
            type="date"
            placeholder="预约日期"
            value-format="YYYY-MM-DD"
            clearable
          />
          <el-select
            v-model="query.status"
            placeholder="状态"
            clearable
            style="width:140px"
          >
            <el-option label="待就诊" value="0" />
            <el-option label="已完成" value="1" />
            <el-option label="已取消" value="2" />
          </el-select>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-space>
      </div>
    </template>

    <el-table :data="rows" :loading="loading" style="width:100%">
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="patientName" label="患者" width="120" />
      <el-table-column prop="departmentName" label="科室" width="140" />
      <el-table-column prop="doctorName" label="医生" width="120" />
      <el-table-column prop="appointmentDate" label="预约日期" width="120" />
      <el-table-column prop="timeSlot" label="时段" width="100" />
      <el-table-column prop="fee" label="费用" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          {{ row.statusText }}
        </template>
      </el-table-column>
    </el-table>

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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchDoctorOrders } from '../api/orders'

const loading = ref(false)
const rows = ref([])
const total = ref(0)

const query = reactive({
  page: 1,
  size: 10,
  date: '',
  status: '',
  keyword: ''
})

function mapStatus(s) {
  return s === 0 ? '待就诊' : s === 1 ? '已完成' : s === 2 ? '已取消' : '-'
}

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
  } catch (e) {
    ElMessage.error(e?.msg || e?.message || '获取列表失败')
  } finally {
    loading.value = false
  }
}

function onSearch() {
  query.page = 1
  fetchList()
}

function onReset() {
  Object.assign(query, {
    page: 1,
    size: 10,
    date: '',
    status: '',
    keyword: ''
  })
  fetchList()
}

onMounted(fetchList)
</script>

<style scoped>
</style>


