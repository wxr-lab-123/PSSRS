<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <div>退款审批</div>
        <el-space>
          <el-input v-model="query.refundNo" placeholder="退款单号" clearable style="width:160px" />
          <el-input v-model="query.orderNo" placeholder="订单号" clearable style="width:160px" />
          <el-select v-model="query.status" placeholder="状态" clearable style="width:140px">
            <el-option label="APPLIED" value="APPLIED" />
            <el-option label="PROCESSING" value="PROCESSING" />
            <el-option label="SUCCEEDED" value="SUCCEEDED" />
            <el-option label="FAILED" value="FAILED" />
          </el-select>
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" />
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-space>
      </div>
    </template>

    <el-table :data="rows" :loading="loading" style="width:100%">
      <el-table-column prop="refundNo" label="退款单号" min-width="220" />
      <el-table-column prop="orderNo" label="订单号" min-width="220" />
      <el-table-column prop="amount" label="金额" width="120">
        <template #default="{ row }">{{ Number(row.amount || 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="140" />
      <el-table-column prop="source" label="来源" width="120" />
      <el-table-column prop="createTime" label="创建时间" min-width="160" />
      <el-table-column prop="updateTime" label="更新时间" min-width="160" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="openApprove(row)" :disabled="row.status==='SUCCEEDED'">审批</el-button>
          <el-button size="small" @click="viewRow(row)">详情</el-button>
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

    <el-dialog v-model="approveVisible" title="退款审批" width="520px">
      <el-form :model="form" label-width="120px" style="max-width:560px">
        <el-form-item label="退款单号">
          <el-input v-model="form.refundNo" disabled />
        </el-form-item>
        <el-form-item label="是否通过">
          <el-radio-group v-model="form.approve">
            <el-radio :label="true">通过</el-radio>
            <el-radio :label="false">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="退款金额">
          <el-input v-model="form.amount" placeholder="不填默认全额" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveVisible=false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="submit">提交</el-button>
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
</style>
