<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <div>挂号管理</div>
        <el-space>
          <el-input v-model="query.patientName" placeholder="患者姓名" clearable style="width:160px" @keyup.enter="onSearch" />
          <el-input v-model="query.doctorName" placeholder="医生姓名" clearable style="width:160px" @keyup.enter="onSearch" />
          <el-input v-model="query.departmentName" placeholder="科室" clearable style="width:160px" @keyup.enter="onSearch" />
          <el-select v-model="query.status" placeholder="状态" clearable style="width:120px">
            <el-option label="全部" value="" />
            <el-option label="待就诊" value="0" />
            <el-option label="已完成" value="1" />
            <el-option label="已取消" value="2" />
          </el-select>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 280px"
          />
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-space>
      </div>
    </template>
    <el-table v-if="!isCardView" :data="rows" :loading="loading" style="width:100%">
      <el-table-column prop="orderNo" label="订单号" min-width="180" />
      <el-table-column prop="patientName" label="患者" width="100" />
      <el-table-column prop="departmentName" label="科室" width="120" />
      <el-table-column prop="doctorName" label="医生" width="120" />
      <el-table-column prop="appointmentDate" label="日期" width="120" />
      <el-table-column prop="timeSlot" label="时段" width="180" />
      <el-table-column prop="fee" label="费用" width="100" align="right">
        <template #default="{ row }">
          <span style="color: #f56c6c; font-weight: 500">¥{{ row.fee }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="statusText" label="状态" width="100" align="center">
        <template #default="{ row }">
          <span :class="['status-tag', `status-${getStatusType(row.status)}`]">
            {{ row.statusText }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right" align="center">
        <template #default="{ row }">
          <el-button 
            type="primary" 
            link 
            size="small"
            @click="handleView(row)"
          >
            详情
          </el-button>
          <el-button 
            v-if="hasPerm('registrations:update') && row.status !== '2'"
            type="primary" 
            link 
            size="small"
            @click="onEdit(row)"
          >
            编辑
          </el-button>
          <el-button 
            v-if="hasPerm('registrations:cancel') && row.status !== '2'" 
            type="danger" 
            link 
            size="small"
            @click="openCancel(row)"
          >
            取消
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-else class="cards-grid">
      <el-card v-for="row in rows" :key="row.orderNo || row.id" class="reg-card" shadow="hover" @click="handleView(row)">
        <template #header>
          <div class="card-header">{{ row.orderNo }}</div>
        </template>
        <div class="card-content">
          <div>患者：{{ row.patientName }}</div>
          <div>科室/医生：{{ row.departmentName }} / {{ row.doctorName }}</div>
          <div>日期/时段：{{ row.appointmentDate }} / {{ row.timeSlot }}</div>
          <div>费用：¥{{ row.fee }}</div>
          <div>状态：{{ row.statusText }}</div>
        </div>
        <div class="card-actions" @click.stop>
          <el-button type="primary" size="small" @click="handleView(row)">详情</el-button>
          <el-button v-if="hasPerm('registrations:update') && row.status !== '2'" size="small" @click="onEdit(row)">编辑</el-button>
          <el-button v-if="hasPerm('registrations:cancel') && row.status !== '2'" type="danger" size="small" @click="openCancel(row)">取消</el-button>
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
        @size-change="handleSizeChange"
      />
    </div>

    <el-dialog v-model="detailVisible" title="挂号详情" width="560px">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="订单号">{{ detailData.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detailData.statusText }}</el-descriptions-item>
        <el-descriptions-item label="患者">{{ detailData.patientName }}</el-descriptions-item>
        <el-descriptions-item label="科室">{{ detailData.departmentName }}</el-descriptions-item>
        <el-descriptions-item label="医生">{{ detailData.doctorName }}</el-descriptions-item>
        <el-descriptions-item label="日期">{{ detailData.appointmentDate }}</el-descriptions-item>
        <el-descriptions-item label="时段">{{ detailData.timeSlot }}</el-descriptions-item>
        <el-descriptions-item label="费用">¥{{ detailData.fee }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button 
          v-if="detailData && !(detailData.status === '2' || detailData.status === 2)" 
          type="danger" 
          @click="detailVisible = false; openCancel(detailData)"
        >
          取消挂号
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editVisible" title="修改挂号单" width="640px">
      <el-form ref="editFormRef" :model="editForm" label-width="96px">
        <el-form-item label="科室">
          <el-select v-model="editForm.departmentId" placeholder="选择科室" filterable @change="onEditDepartmentChange" style="width:100%">
            <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="医生">
          <el-select v-model="editForm.doctorId" placeholder="选择医生" :disabled="!editForm.departmentId" filterable style="width:100%">
            <el-option v-for="doc in doctors" :key="doc.id" :label="doc.username || doc.name" :value="doc.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="就诊日期">
          <el-date-picker v-model="editForm.appointmentDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="时段">
          <el-select v-model="editForm.timeSlot" placeholder="选择时段" style="width:100%">
            <el-option label="上午" value="上午" />
            <el-option label="下午" value="下午" />
          </el-select>
        </el-form-item>
        <el-form-item label="就诊类型">
          <el-select v-model="editForm.registrationType" placeholder="选择类型" style="width:100%">
            <el-option label="普通号" value="normal" />
            <el-option label="专家号" value="expert" />
          </el-select>
        </el-form-item>
        <el-form-item label="挂号状态">
          <el-select v-model="editForm.status" placeholder="选择状态" style="width:100%">
            <el-option label="未到诊" value="0" />
            <el-option label="已到诊" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注信息">
          <el-input v-model="editForm.notes" type="textarea" rows="3" placeholder="填写备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSubmitting" @click="onEditSubmit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="cancelVisible" title="取消挂号单" width="520px">
      <el-form :model="cancelForm" label-width="96px">
        <el-form-item label="取消原因">
          <el-select v-model="cancelForm.reason" placeholder="选择原因" style="width:100%">
            <el-option label="患者取消" value="PATIENT_CANCELLED" />
            <el-option label="医生停诊" value="DOCTOR_CANCELLED" />
            <el-option label="系统纠错" value="SYSTEM_ADJUST" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="cancelForm.remark" type="textarea" rows="3" placeholder="备注说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelVisible = false">返回</el-button>
        <el-button type="danger" :loading="cancelSubmitting" @click="handleCancelSubmit">确认取消</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted, computed, inject } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Calendar, User, UserFilled, OfficeBuilding } from '@element-plus/icons-vue'
import { fetchAdminRegistrations, cancelRegistration, updateRegistration } from '../api/registrations'
import { fetchDepartmentsList } from '../api/departments'
import { fetchDoctorsByDepartment } from '../api/doctors'
import { useAuthStore } from '../stores/auth'
import request from '../api/request'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const dateRange = ref([])
const detailVisible = ref(false)
const detailData = ref({})
const editVisible = ref(false)
const editSubmitting = ref(false)
const editFormRef = ref()
const editForm = reactive({
  id: undefined,
  departmentId: null,
  doctorId: null,
  appointmentDate: '',
  timeSlot: '',
  registrationType: 'normal',
  notes: '',
  status: '0'
})
const departments = ref([])
const doctors = ref([])
const cancelVisible = ref(false)
const cancelSubmitting = ref(false)
const cancelForm = reactive({ reason: '', remark: '' })
const currentRowId = ref(null)

const query = reactive({
  page: 1,
  size: 10,
  patientName: '',
  doctorName: '',
  departmentName: '',
  status: ''
})

const auth = useAuthStore()
const hasPerm = (code) => auth.hasPerm(code)

// 状态映射
function mapStatus(s) {
  if (s === 0 || s === '0') return '待就诊'
  if (s === 1 || s === '1') return '已完成'
  if (s === 2 || s === '2') return '已取消'
  return '未知'
}

// 获取状态样式类名
function getStatusClass(status) {
  if (status === '0' || status === 0) return 'status-warning'  // 待就诊
  if (status === '1' || status === 1) return 'status-success'  // 已完成
  if (status === '2' || status === 2) return 'status-danger'   // 已取消
  return 'status-info'
}

// 状态标签类型
function getStatusType(status) {
  const map = {
    '0': 'warning',  // 待就诊
    '1': 'success',  // 已完成
    '2': 'danger'    // 已取消
  }
  return map[status] || 'info'
}

async function fetchList() {
  loading.value = true
  try {
    const params = { 
      ...query,
      page: query.page || 1,
      size: query.size || 10
    }
    
    if (Array.isArray(dateRange.value) && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    
    // 请求参数添加时间戳防止缓存
    const timestamp = new Date().getTime()
    const res = await fetchAdminRegistrations({
      ...params,
      _t: timestamp
    })
    
    // 处理返回的数据
    const listData = res?.records || []
    const totalCount = res?.total || 0
    
    // 映射数据
    rows.value = listData.map(item => ({
      ...item,
      orderNo: item.orderNo || item.registrationNo,
      statusText: mapStatus(item.status)
    }))
    
    total.value = totalCount
  } catch (e) {
    ElMessage.error(e?.msg || e?.message || '获取挂号列表失败')
  } finally {
    loading.value = false
  }
}

function onSearch() {
  query.page = 1
  fetchList()
}

// 处理分页大小变化
function handleSizeChange(size) {
  query.size = size
  query.page = 1  // 重置到第一页
  fetchList()
}

// 重置查询条件
function onReset() {
  query.page = 1
  query.size = 10
  query.patientName = ''
  query.doctorName = ''
  query.departmentName = ''
  query.status = ''
  dateRange.value = []
  fetchList()
}

// 查看详情
function handleView(row) {
  detailData.value = { ...row }
  detailVisible.value = true
}

function openCancel(row) {
  currentRowId.value = row.id
  cancelForm.reason = ''
  cancelForm.remark = ''
  cancelVisible.value = true
}

async function handleCancelSubmit() {
  if (!currentRowId.value) return
  if (!hasPerm('registrations:cancel')) { ElMessage.error('无权限'); return }
  cancelSubmitting.value = true
  try {
    await cancelRegistration(currentRowId.value, { reason: cancelForm.reason, remark: cancelForm.remark })
    ElMessage.success('取消挂号成功')
    cancelVisible.value = false
    fetchList()
    try { await request.post('/audit/events', { module: 'registrations', action: 'cancel', targetId: currentRowId.value, payload: { ...cancelForm }, ts: Date.now() }) } catch {}
  } catch (e) {
    ElMessage.error(e?.msg || e?.message || '操作失败')
  } finally {
    cancelSubmitting.value = false
  }
}

async function onEdit(row) {
  editVisible.value = true
  editForm.id = row.id
  editForm.appointmentDate = row.appointmentDate || ''
  editForm.timeSlot = /上午/.test(row.timeSlot || '') ? '上午' : (/下午/.test(row.timeSlot || '') ? '下午' : '')
  editForm.notes = ''
  editForm.registrationType = 'normal'
  editForm.status = (row.status === '1' || row.status === 1) ? '1' : '0'
  if (!departments.value.length) {
    try { departments.value = await fetchDepartmentsList() || [] } catch { departments.value = [] }
  }
  doctors.value = []
  editForm.departmentId = null
  editForm.doctorId = null
}

async function onEditDepartmentChange(deptId) {
  editForm.doctorId = null
  doctors.value = []
  if (!deptId) return
  try {
    const data = await fetchDoctorsByDepartment(deptId)
    const list = Array.isArray(data?.records) ? data.records : (Array.isArray(data) ? data : [])
    doctors.value = list || []
  } catch {
    doctors.value = []
  }
}

async function onEditSubmit() {
  if (!editForm.id) return
  if (!hasPerm('registrations:update')) { ElMessage.error('无权限'); return }
  editSubmitting.value = true
  try {
    const payload = {
      ...(editForm.departmentId ? { department_id: editForm.departmentId, departmentId: editForm.departmentId } : {}),
      ...(editForm.doctorId ? { doctor_id: editForm.doctorId, doctorId: editForm.doctorId } : {}),
      ...(editForm.appointmentDate ? { appointment_date: editForm.appointmentDate, appointmentDate: editForm.appointmentDate, date: editForm.appointmentDate } : {}),
      ...(editForm.timeSlot ? { time_slot: editForm.timeSlot, timeSlot: editForm.timeSlot, period: editForm.timeSlot, slot: editForm.timeSlot } : {}),
      ...(editForm.registrationType ? { registration_type: editForm.registrationType, registrationType: editForm.registrationType, type: editForm.registrationType } : {}),
      ...(editForm.notes ? { notes: editForm.notes, remark: editForm.notes } : {}),
      ...(editForm.status ? { status: editForm.status } : {})
    }
    await updateRegistration(editForm.id, payload)
    ElMessage.success('修改成功')
    editVisible.value = false
    fetchList()
    try { await request.post('/audit/events', { module: 'registrations', action: 'update', targetId: editForm.id, payload, ts: Date.now() }) } catch {}
  } catch (e) {
    ElMessage.error(e?.msg || e?.message || '修改失败')
  } finally {
    editSubmitting.value = false
  }
}

const uiViewMode = inject('ui_view_mode', ref('card'))
const isCardView = computed(() => (uiViewMode?.value || 'card') === 'card')

onMounted(fetchList)
</script>

<style scoped>
/* 状态标签 */
.status-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  line-height: 20px;
}

.status-success {
  color: #67c23a;
  background-color: #f0f9eb;
  border: 1px solid #e1f3d8;
}

.status-warning {
  color: #e6a23c;
  background-color: #fdf6ec;
  border: 1px solid #faecd8;
}

.status-danger {
  color: #f56c6c;
  background-color: #fef0f0;
  border: 1px solid #fde2e2;
}

.cards-grid { display:grid; grid-template-columns:repeat(4,minmax(0,1fr)); gap:16px }
@media (max-width:1200px){ .cards-grid{ grid-template-columns:repeat(3,minmax(0,1fr)) } }
@media (max-width:768px){ .cards-grid{ grid-template-columns:repeat(2,minmax(0,1fr)) } }
@media (max-width:480px){ .cards-grid{ grid-template-columns:1fr } }
.reg-card { border-radius: 8px; transition: all .2s ease }
.reg-card:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,.15) }
.card-header { font-weight:600; font-size:16px }
.card-content { font-size:14px; line-height:1.6; padding: 0 16px; display:flex; flex-direction:column; gap:12px }
.card-actions { display:flex; justify-content:flex-end; gap:8px; padding: 8px 16px }
</style>


