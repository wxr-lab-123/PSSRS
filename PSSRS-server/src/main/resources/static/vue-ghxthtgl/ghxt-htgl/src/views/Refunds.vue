<template>
  <el-card shadow="hover" class="page-card">
    <template #header>
      <div class="card-header">
        <div class="header-title">退款审批</div>
        <el-space size="small">
          <el-input v-model="query.refundNo" placeholder="退款单号" clearable class="filter-input" />
          <el-input v-model="query.orderNo" placeholder="订单号" clearable class="filter-input" />
          <el-select v-model="query.status" placeholder="状态" clearable class="filter-select">
            <el-option label="申请中" value="APPLIED" />
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="已成功" value="SUCCEEDED" />
            <el-option label="已失败" value="FAILED" />
          </el-select>
          <el-date-picker 
            v-model="dateRange" 
            type="daterange" 
            range-separator="至" 
            start-placeholder="开始日期" 
            end-placeholder="结束日期" 
            value-format="YYYY-MM-DD" 
            class="filter-date"
          />
          <el-button type="primary" @click="onSearch" class="btn-primary">查询</el-button>
          <el-button @click="onReset" class="btn-default">重置</el-button>
        </el-space>
      </div>
    </template>

    <el-table :data="rows" :loading="loading" stripe border highlight-current-row style="width:100%" class="custom-table">
      <el-table-column prop="refundNo" label="退款单号" min-width="200" show-overflow-tooltip />
      <el-table-column prop="orderNo" label="订单号" min-width="200" show-overflow-tooltip />
      <el-table-column prop="amount" label="金额" width="120" align="right">
        <template #default="{ row }">
          <span class="amount">¥{{ Number(row.amount || 0).toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" effect="plain" round>{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="source" label="来源" width="100" align="center" />
      <el-table-column prop="createTime" label="创建时间" min-width="160" align="center" />
      <el-table-column prop="updateTime" label="更新时间" min-width="160" align="center" />
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'APPLIED'" type="primary" link size="small" @click="openApprove(row)">审批</el-button>
          <el-button type="info" link size="small" @click="viewRow(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
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

    <el-dialog v-model="approveVisible" title="退款审批" width="500px" align-center class="custom-dialog">
      <el-form :model="form" label-width="100px">
        <el-form-item label="退款单号">
          <el-input v-model="form.refundNo" disabled class="dialog-input" />
        </el-form-item>
        <el-form-item label="审批结果">
          <el-radio-group v-model="form.approve">
            <el-radio :label="true" border>通过</el-radio>
            <el-radio :label="false" border>拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="退款金额">
          <el-input v-model="form.amount" placeholder="不填则全额退款" class="dialog-input">
            <template #prefix>¥</template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="approveVisible=false">取消</el-button>
          <el-button type="primary" :loading="loading" @click="submit">确认提交</el-button>
        </div>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { approveRefund } from '../api/refunds'
import request from '../api/request'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const dateRange = ref([])
const query = reactive({ page: 1, size: 10, refundNo: '', orderNo: '', status: '' })

const approveVisible = ref(false)
const form = reactive({ refundNo: '', approve: true, amount: '' })

// 状态辅助函数
function getStatusType(status) {
  const map = {
    'APPLIED': 'warning',
    'PROCESSING': 'primary',
    'SUCCEEDED': 'success',
    'FAILED': 'danger'
  }
  return map[status] || 'info'
}

function getStatusText(status) {
  const map = {
    'APPLIED': '申请中',
    'PROCESSING': '处理中',
    'SUCCEEDED': '已成功',
    'FAILED': '已失败'
  }
  return map[status] || status
}

async function fetchList() {
  loading.value = true
  try {
    const params = { ...query }
    if (Array.isArray(dateRange.value) && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const data = await request.get('/order/refund/list', { params })
    rows.value = (data.records || data.list || [])
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error(e?.msg || e?.message || '获取退款申请失败')
  } finally {
    loading.value = false
  }
}

function onSearch(){ query.page = 1; fetchList() }
function onReset(){ query.page = 1; query.size = 10; query.refundNo = ''; query.orderNo = ''; query.status=''; dateRange.value = []; fetchList() }

function openApprove(row){ form.refundNo = row.refundNo; form.approve = true; form.amount = ''; approveVisible.value = true }
function viewRow(row){ ElMessage.info(`退款单号：${row.refundNo}，订单号：${row.orderNo}`) }

async function submit() {
  if (!form.refundNo) { ElMessage.error('请输入退款单号'); return }
  loading.value = true
  try {
    const payload = { refundNo: form.refundNo, approve: form.approve }
    if (form.amount) payload.amount = form.amount
    await approveRefund(payload)
    ElMessage.success('已提交审批')
    approveVisible.value = false
    fetchList()
  } catch (e) {
    ElMessage.error(e?.msg || e?.message || '提交失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => { fetchList() })
</script>

<style scoped>
.page-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.filter-input {
  width: 180px;
}

.filter-select {
  width: 140px;
}

.filter-date {
  width: 240px;
}

.custom-table {
  margin-top: 16px;
  border-radius: 4px;
}

.amount {
  font-family: Consolas, monospace;
  font-weight: 600;
  color: #606266;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.custom-dialog :deep(.el-dialog__body) {
  padding: 20px 40px;
}

.dialog-input {
  width: 100%;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-table__header) th {
  background-color: #f5f7fa !important;
  color: #606266;
  font-weight: 600;
}

.btn-primary {
  background-color: #409eff;
  border-color: #409eff;
}

.btn-primary:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
}

.btn-default {
  background-color: #fff;
  border-color: #dcdfe6;
  color: #606266;
}

.btn-default:hover {
  color: #409eff;
  border-color: #c6e2ff;
  background-color: #ecf5ff;
}
</style>
