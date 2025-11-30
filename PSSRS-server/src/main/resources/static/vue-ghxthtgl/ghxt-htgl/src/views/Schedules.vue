<template>
  <el-card>
    <template #header>
      <div class="toolbar">
        <div class="filters">
          <el-select v-model="query.departmentId" placeholder="科室" clearable style="width:180px" @change="onQueryDepartmentChange">
            <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
          <el-select v-model="query.doctorId" placeholder="医生" clearable style="width:160px" :disabled="!query.departmentId">
            <el-option v-for="doc in doctors" :key="doc.id" :label="doc.username" :value="doc.id" />
          </el-select>
          <el-date-picker
            v-model="query.scheduleDate"
            type="date"
            placeholder="选择日期"
            clearable
            style="width:160px"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
          <el-select v-model="query.timeSlot" placeholder="时间段" clearable style="width:120px">
            <el-option label="上午" value="上午" />
            <el-option label="下午" value="下午" />
          </el-select>
          <el-select v-model="query.status" placeholder="状态" clearable style="width:120px">
            <el-option label="可预约" value="AVAILABLE" />
            <el-option label="已满" value="FULL" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
          <el-select v-model="query.scheduleType" placeholder="号源类型" clearable style="width:120px">
            <el-option label="普通号" value="normal" />
            <el-option label="专家号" value="expert" />
          </el-select>
        </div>
        <div class="actions">
          <el-space>
            <el-button type="primary" @click="onSearch">查询</el-button>
            <el-button @click="onReset">重置</el-button>
            <el-button type="primary" @click="onAdd">新增排班</el-button>
            <el-button type="success" @click="onBatchAdd">批量排班</el-button>
          </el-space>
        </div>
      </div>
    </template>

    <el-table v-if="!isCardView" :data="rows" :loading="loading" style="width:100%">
      <el-table-column prop="schedule_date" label="日期" width="120" />
      <el-table-column prop="time_slot" label="时间段" width="100">
        <template #default="{ row }">
          <el-tag :type="getTimeSlotType(row.time_slot)" size="small">
            {{ row.time_slot }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="时间" width="140">
        <template #default="{ row }">
          {{ row.start_time }} - {{ row.end_time }}
        </template>
      </el-table-column>
      <el-table-column prop="schedule_type" label="号源类型" width="100">
        <template #default="{ row }">
          <el-tag :type="getScheduleTypeType(row.schedule_type)" size="small">
            {{ getScheduleTypeLabel(row.schedule_type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="doctor_name" label="医生" width="100" />
      <el-table-column prop="department_name" label="科室" width="120" />
      <el-table-column prop="room_number" label="诊室" width="100" />
      <el-table-column label="预约情况" width="180">
        <template #default="{ row }">
          <div style="display:flex;align-items:center;gap:8px">
            <el-progress 
              :percentage="getAppointmentProgress(row)" 
              :color="getProgressColor(row)"
              :stroke-width="8"
              style="flex:1"
            />
            <span style="font-size:12px;color:#606266">
              {{ row.current_appointments || 0 }}/{{ row.max_appointments }}
            </span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="notes" label="备注" show-overflow-tooltip />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="onEdit(row)">编辑</el-button>
          <el-button type="warning" link @click="onCopy(row)">复制</el-button>
          <el-button 
            v-if="row.status !== 'CANCELLED'" 
            type="danger" 
            link 
            @click="onDelete(row)"
          >
            取消
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-else class="cards-grid">
      <el-card v-for="row in rows" :key="row.id" class="schedule-card" shadow="hover" @click="onEdit(row)">
        <template #header>
          <div class="card-header">{{ row.schedule_date }} · {{ row.time_slot }}</div>
        </template>
        <div class="card-content">
          <div>医生：{{ row.doctor_name }}</div>
          <div>科室：{{ row.department_name }}</div>
          <div>诊室：{{ row.room_number || '-' }}</div>
          <div>时间：{{ row.start_time }} - {{ row.end_time }}</div>
          <div>号源：{{ getScheduleTypeLabel(row.schedule_type) }}</div>
          <div>预约：{{ row.current_appointments || 0 }}/{{ row.max_appointments }}</div>
          <div>状态：{{ getStatusLabel(row.status) }}</div>
        </div>
        <div class="card-actions" @click.stop>
          <el-button type="primary" size="small" @click="onEdit(row)">编辑</el-button>
          <el-button size="small" @click="onCopy(row)">复制</el-button>
          <el-button v-if="row.status !== 'CANCELLED'" type="danger" size="small" @click="onDelete(row)">取消</el-button>
        </div>
      </el-card>
    </div>

    <div style="display:flex;justify-content:flex-end;margin-top:12px">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :page-sizes="[10,20,50,100]"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <!-- 新增/编辑排班对话框 -->
    <el-dialog 
      v-model="editVisible" 
      :title="editForm.id ? '编辑排班' : '新增排班'" 
      width="600px"
    >
      <el-form ref="editFormRef" :model="editForm" :rules="rules" label-width="100px" v-loading="editLoading">
        <el-form-item label="科室" prop="department_id">
          <el-select 
            v-model="editForm.department_id" 
            placeholder="请选择科室" 
            style="width:100%"
            @change="onDepartmentChange"
            :disabled="editLoading"
          >
            <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="医生" prop="doctor_id">
          <el-select 
            v-model="editForm.doctor_id" 
            placeholder="请选择医生" 
            style="width:100%"
            :disabled="!editForm.department_id || editLoading"
          >
            <el-option 
              v-for="doc in filteredDoctors" 
              :key="doc.id" 
              :label="doc.username" 
              :value="doc.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="号源类型" prop="schedule_type">
          <el-select 
            v-model="editForm.schedule_type" 
            placeholder="请选择号源类型" 
            style="width:100%"
          >
            <el-option label="普通号" value="normal" />
            <el-option label="专家号" value="expert" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期" prop="schedule_date">
          <el-date-picker
            v-model="editForm.schedule_date"
            type="date"
            placeholder="选择日期"
            style="width:100%"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="时间段" prop="time_slot">
          <el-select v-model="editForm.time_slot" placeholder="请选择时间段" style="width:100%" @change="onTimeSlotChange">
            <el-option label="上午" value="上午" />
            <el-option label="下午" value="下午" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="start_time">
          <el-time-picker
            v-model="editForm.start_time"
            placeholder="选择开始时间"
            style="width:100%"
            format="HH:mm"
            value-format="HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="end_time">
          <el-time-picker
            v-model="editForm.end_time"
            placeholder="选择结束时间"
            style="width:100%"
            format="HH:mm"
            value-format="HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="最大预约数" prop="max_appointments">
          <el-input-number 
            v-model="editForm.max_appointments" 
            :min="1" 
            :max="100" 
            style="width:100%"
            :disabled="editLoading"
          />
        </el-form-item>
        <el-form-item label="诊室号" prop="room_number">
          <el-input v-model="editForm.room_number" placeholder="请输入诊室号" />
        </el-form-item>
        <el-form-item label="备注" prop="notes">
          <el-input 
            v-model="editForm.notes" 
            type="textarea" 
            rows="3" 
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editVisible = false">取消</el-button>
          <el-button type="primary" :loading="editSubmitting" @click="onSave">保 存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 批量排班对话框 -->
    <el-dialog v-model="batchVisible" title="批量排班" width="700px">
      <el-form ref="batchFormRef" :model="batchForm" :rules="batchRules" label-width="100px">
        <el-form-item label="科室" prop="department_id">
          <el-select 
            v-model="batchForm.department_id" 
            placeholder="请选择科室" 
            style="width:100%"
            @change="onBatchDepartmentChange"
          >
            <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="医生" prop="doctor_id">
          <el-select 
            v-model="batchForm.doctor_id" 
            placeholder="请选择医生" 
            style="width:100%"
            :disabled="!batchForm.department_id"
          >
            <el-option 
              v-for="doc in batchFilteredDoctors" 
              :key="doc.id" 
              :label="doc.username" 
              :value="doc.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围" prop="dateRange">
          <el-date-picker
            v-model="batchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width:100%"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="时间段配置" prop="timeSlots">
          <div style="display:flex;flex-direction:column;gap:12px;width:100%">
            <div 
              v-for="(slot, index) in batchForm.timeSlots" 
              :key="index"
              style="display:flex;gap:8px;align-items:center"
            >
              <el-select v-model="slot.time_slot" placeholder="时间段" style="width:100px" @change="() => onBatchSlotChange(slot)">
                <el-option label="上午" value="上午" />
                <el-option label="下午" value="下午" />
              </el-select>
              <el-time-picker
                v-model="slot.start_time"
                placeholder="开始"
                format="HH:mm"
                value-format="HH:mm:ss"
                style="width:140px"
              />
              <span>-</span>
              <el-time-picker
                v-model="slot.end_time"
                placeholder="结束"
                format="HH:mm"
                value-format="HH:mm:ss"
                style="width:140px"
              />
              <el-button 
                type="danger" 
                :icon="'Delete'" 
                circle 
                size="small"
                @click="removeTimeSlot(index)"
                :disabled="batchForm.timeSlots.length === 1"
              />
            </div>
            <el-button type="primary" size="small" @click="addTimeSlot" style="width:120px">
              添加时间段
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="最大预约数" prop="max_appointments">
          <el-input-number 
            v-model="batchForm.max_appointments" 
            :min="1" 
            :max="100" 
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="诊室号" prop="room_number">
          <el-input v-model="batchForm.room_number" placeholder="请输入诊室号" />
        </el-form-item>
        <el-form-item label="号源类型" prop="schedule_type">
          <el-select v-model="batchForm.schedule_type" placeholder="请选择号源类型" style="width:100%">
            <el-option label="普通号" value="normal" />
            <el-option label="专家号" value="expert" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="batchVisible = false">取消</el-button>
          <el-button type="primary" :loading="batchSubmitting" @click="onBatchSave">
            批量创建
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 复制排班对话框 -->
    <el-dialog v-model="copyVisible" title="复制排班" width="500px">
      <el-form ref="copyFormRef" :model="copyForm" :rules="copyRules" label-width="100px">
        <el-form-item label="目标日期" prop="targetDates">
          <el-date-picker
            v-model="copyForm.targetDates"
            type="dates"
            placeholder="选择多个日期"
            style="width:100%"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="copyVisible = false">取消</el-button>
          <el-button type="primary" :loading="copySubmitting" @click="onCopySave">
            确认复制
          </el-button>
        </span>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, inject, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Calendar } from '@element-plus/icons-vue'
import { 
  fetchSchedules, 
  fetchScheduleById,
  createSchedule, 
  updateSchedule, 
  deleteSchedule, 
  batchCreateSchedules,
  updateScheduleStatus,
  copySchedule
} from '../api/schedules'
import { fetchDoctors, fetchDoctorsByDepartment } from '../api/doctors'
import { fetchDepartmentsList } from '../api/departments'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const reqSeq = ref(0)
const query = reactive({ 
  departmentId: null, 
  doctorId: null, 
  scheduleDate: '', 
  timeSlot: '', 
  status: '',
  scheduleType: ''
})

const departments = ref([])
const doctors = ref([])
const filteredDoctorsList = ref([])
const batchFilteredDoctorsList = ref([])

const editVisible = ref(false)
const editSubmitting = ref(false)
const editFormRef = ref()
const editLoading = ref(false)
const editForm = reactive({
  id: null,
  doctor_id: null,
  department_id: null,
  schedule_date: '',
  time_slot: '',
  start_time: '',
  end_time: '',
  max_appointments: 20,
  room_number: '',
  notes: '',
  schedule_type: 'normal'
})

const batchVisible = ref(false)
const batchSubmitting = ref(false)
const batchFormRef = ref()
const batchForm = reactive({
  doctor_id: null,
  department_id: null,
  dateRange: [],
  timeSlots: [
    { time_slot: '上午', start_time: '08:30:00', end_time: '11:30:00' }
  ],
  max_appointments: 20,
  room_number: '',
  schedule_type: 'normal'
})

const copyVisible = ref(false)
const copySubmitting = ref(false)
const copyFormRef = ref()
const copyForm = reactive({
  source_schedule_id: null,
  targetDates: []
})

const rules = {
  doctor_id: [{ required: true, message: '请选择医生', trigger: 'change' }],
  department_id: [{ required: true, message: '请选择科室', trigger: 'change' }],
  schedule_date: [{ required: true, message: '请选择日期', trigger: 'change' }],
  time_slot: [{ required: true, message: '请选择时间段', trigger: 'change' }],
  start_time: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  end_time: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  max_appointments: [{ required: true, message: '请输入最大预约数', trigger: 'blur' }]
}

const batchRules = {
  doctor_id: [{ required: true, message: '请选择医生', trigger: 'change' }],
  department_id: [{ required: true, message: '请选择科室', trigger: 'change' }],
  dateRange: [{ required: true, message: '请选择日期范围', trigger: 'change' }],
  max_appointments: [{ required: true, message: '请输入最大预约数', trigger: 'blur' }],
  schedule_type: [{ required: true, message: '请选择号源类型', trigger: 'change' }]
}

const copyRules = {
  targetDates: [{ required: true, message: '请选择目标日期', trigger: 'change' }]
}

const filteredDoctors = computed(() => filteredDoctorsList.value)
const batchFilteredDoctors = computed(() => batchFilteredDoctorsList.value)

function normalizeDoctors(list) {
  return (list || []).map(d => ({
    ...d,
    username: d.username || d.name || d.realName || d.real_name || d.doctorName || ''
  }))
}

function normalizeSchedules(list) {
  return (list || []).map(r => {
    const normalized = {
      ...r,
      id: r.id ?? r.schedule_id ?? r.scheduleId,
      doctor_id: r.doctor_id ?? r.doctorId,
      doctor_name: r.doctor_name ?? r.doctorName,
      department_id: r.department_id ?? r.departmentId,
      department_name: r.department_name ?? r.departmentName,
      schedule_date: r.schedule_date ?? r.scheduleDate,
      time_slot: toChineseTimeSlot(r.time_slot ?? r.timeSlot),
      start_time: r.start_time ?? r.startTime,
      end_time: r.end_time ?? r.endTime,
      room_number: r.room_number ?? r.roomNumber,
      current_appointments: r.current_appointments ?? r.currentAppointments ?? r.currentAppointmentsCount,
      max_appointments: r.max_appointments ?? r.maxAppointments ?? 20,
      schedule_type: r.schedule_type ?? r.scheduleType ?? 'normal'
    }
    return normalized
  })
}

function toEnglishTimeSlot(v) {
  if (v === '上午') return 'MORNING'
  if (v === '下午') return 'AFTERNOON'
  if (v === '晚上') return 'EVENING'
  return v
}

function toChineseTimeSlot(v) {
  if (v === 'MORNING') return '上午'
  if (v === 'AFTERNOON') return '下午'
  if (v === 'EVENING') return '晚上'
  return v
}

function toNumber(v) {
  const n = Number(v)
  return Number.isFinite(n) ? n : v
}

async function loadDepartments() {
  try {
    const list = await fetchDepartmentsList()
    departments.value = list || []
  } catch {
    departments.value = []
  }
}

async function loadDoctors() {
  try {
    const data = await fetchDoctors({ page: 1, size: 1000 })
    doctors.value = data.records || []
  } catch {
    doctors.value = []
  }
}

async function onQueryDepartmentChange(departmentId) {
  query.doctorId = null
  doctors.value = []
  if (!departmentId) return
  try {
    const data = await fetchDoctorsByDepartment(departmentId)
    const list = Array.isArray(data?.records) ? data.records : (Array.isArray(data) ? data : [])
    doctors.value = normalizeDoctors(list)
  } catch {
    doctors.value = []
  }
}

async function fetchList() {
  loading.value = true
  const mySeq = ++reqSeq.value
  try {
    const slotEnum = toEnglishTimeSlot(query.timeSlot)
    const params = {
      page: page.value,
      size: pageSize.value,
      pageNum: page.value,
      pageSize: pageSize.value,
      ...(query.departmentId ? { department_id: query.departmentId, departmentId: query.departmentId } : {}),
      ...(query.doctorId ? { doctor_id: query.doctorId, doctorId: query.doctorId } : {}),
      ...(query.scheduleDate ? { schedule_date: query.scheduleDate, scheduleDate: query.scheduleDate } : {}),
      ...(query.timeSlot ? {
        time_slot: query.timeSlot,
        timeSlot: query.timeSlot,
        period: slotEnum,
        slot: slotEnum
      } : {}),
      ...(query.status ? { status: query.status } : {}),
      ...(query.scheduleType ? { schedule_type: query.scheduleType, scheduleType: query.scheduleType } : {})
    }
    const data = await fetchSchedules(params)
    if (mySeq !== reqSeq.value) return
    const list = Array.isArray(data?.records) ? data.records : (Array.isArray(data) ? data : [])
    if (mySeq !== reqSeq.value) return
    rows.value = []
    await nextTick()
    if (mySeq !== reqSeq.value) return
    rows.value = normalizeSchedules(list)
    total.value = data.total || 0
  } catch (e) {
    if (mySeq === reqSeq.value) {
      ElMessage.error(e?.msg || e?.message || '获取列表失败')
    }
  } finally {
    if (mySeq === reqSeq.value) {
      loading.value = false
    }
  }
}

function onSearch() {
  page.value = 1
  fetchList()
}

function onReset() {
  Object.assign(query, {
    departmentId: null,
    doctorId: null,
    scheduleDate: '',
    timeSlot: '',
    status: '',
    scheduleType: ''
  })
  doctors.value = []
  page.value = 1
  fetchList()
}

function handlePageChange(p) {
  page.value = p
  fetchList()
}

function handleSizeChange(size) {
  pageSize.value = size
  page.value = 1
  fetchList()
}

function onAdd() {
  Object.assign(editForm, {
    id: null,
    doctor_id: null,
    department_id: null,
    schedule_date: '',
    time_slot: '',
    start_time: '',
    end_time: '',
    max_appointments: 20,
    room_number: '',
    notes: '',
    schedule_type: 'normal'
  })
  editVisible.value = true
}

async function onEdit(row) {
  editVisible.value = true
  editLoading.value = true

  if (!Array.isArray(departments.value) || departments.value.length === 0) {
    await loadDepartments()
  }

  const idNum = Number(row.id ?? row.schedule_id ?? row.scheduleId)
  try {
    if (!Number.isFinite(idNum)) throw new Error('invalid id')
    const resp = await fetchScheduleById(idNum)
    const d = (resp && (resp.data ?? resp)) || {}
    const deptName = d.department_name ?? d.departmentName ?? row.department_name
    const deptIdFromName = (() => {
      if (!deptName) return null
      const found = (departments.value || []).find(x => x.name === deptName)
      return found ? found.id : null
    })()
    Object.assign(editForm, {
      id: d.id ?? row.id ?? row.schedule_id ?? row.scheduleId,
      doctor_id: toNumber(d.doctor_id ?? d.doctorId ?? row.doctor_id),
      department_id: toNumber(d.department_id ?? d.departmentId ?? row.department_id ?? deptIdFromName),
      schedule_date: d.schedule_date ?? d.scheduleDate ?? row.schedule_date,
      time_slot: toChineseTimeSlot(d.time_slot ?? d.timeSlot ?? row.time_slot),
      start_time: d.start_time ?? d.startTime ?? row.start_time,
      end_time: d.end_time ?? d.endTime ?? row.end_time,
      max_appointments: toNumber(d.max_appointments ?? d.maxAppointments ?? row.max_appointments ?? 20),
      room_number: (d.room_number ?? d.roomNumber ?? row.room_number) || '',
      notes: (d.notes ?? row.notes) || '',
      schedule_type: (d.schedule_type ?? d.scheduleType ?? row.schedule_type) || 'normal'
    })
  } catch {
    Object.assign(editForm, {
      id: row.id,
      doctor_id: toNumber(row.doctor_id),
      department_id: toNumber(row.department_id),
      schedule_date: row.schedule_date,
      time_slot: row.time_slot,
      start_time: row.start_time,
      end_time: row.end_time,
      max_appointments: toNumber(row.max_appointments ?? 20),
      room_number: row.room_number || '',
      notes: row.notes || '',
      schedule_type: row.schedule_type || 'normal'
    })
  }

  if (editForm.department_id) {
    try {
      const data = await fetchDoctorsByDepartment(editForm.department_id)
      const list = Array.isArray(data?.records) ? data.records : (Array.isArray(data) ? data : [])
      const normalized = normalizeDoctors(list)
      if (editForm.doctor_id != null && !normalized.some(d => toNumber(d.id) === toNumber(editForm.doctor_id))) {
        normalized.push({ id: toNumber(editForm.doctor_id), username: row.doctor_name || row.doctorName || '当前医生' })
      }
      filteredDoctorsList.value = normalized
    } catch {
      filteredDoctorsList.value = []
    }
  }

  await nextTick()
  if (editFormRef.value) {
    editFormRef.value.clearValidate()
    editFormRef.value.validate()
  }
  editLoading.value = false
}

async function onDepartmentChange(departmentId) {
  editForm.doctor_id = null
  filteredDoctorsList.value = []
  if (!departmentId) return
  try {
    const data = await fetchDoctorsByDepartment(departmentId)
    const list = Array.isArray(data?.records) ? data.records : (Array.isArray(data) ? data : [])
    filteredDoctorsList.value = normalizeDoctors(list)
  } catch (e) {
    ElMessage.error(e?.msg || e?.message || '获取医生列表失败')
    filteredDoctorsList.value = []
  }
}

async function onBatchDepartmentChange(departmentId) {
  batchForm.doctor_id = null
  batchFilteredDoctorsList.value = []
  if (!departmentId) return
  try {
    const data = await fetchDoctorsByDepartment(departmentId)
    const list = Array.isArray(data?.records) ? data.records : (Array.isArray(data) ? data : [])
    batchFilteredDoctorsList.value = normalizeDoctors(list)
  } catch (e) {
    ElMessage.error(e?.msg || e?.message || '获取医生列表失败')
    batchFilteredDoctorsList.value = []
  }
}

function onSave() {
  if (!editFormRef.value) return
  editFormRef.value.validate(async (valid) => {
    if (!valid) return
    editSubmitting.value = true
    try {
      const payload = {
        ...editForm,
        doctor_id: editForm.doctor_id != null ? Number(editForm.doctor_id) : null,
        department_id: editForm.department_id != null ? Number(editForm.department_id) : null,
        max_appointments: editForm.max_appointments != null ? Number(editForm.max_appointments) : null,
        schedule_type: editForm.schedule_type || 'normal'
      }
      if (editForm.id) {
        payload.id = Number(editForm.id)
        await updateSchedule(payload)
      } else {
        delete payload.id
        await createSchedule(payload)
      }
      ElMessage.success('保存成功')
      editVisible.value = false
      fetchList()
    } catch (e) {
      ElMessage.error(e?.msg || e?.message || '保存失败')
    } finally {
      editSubmitting.value = false
    }
  })
}

function onBatchAdd() {
  Object.assign(batchForm, {
    doctor_id: null,
    department_id: null,
    dateRange: [],
    timeSlots: [
      { time_slot: '上午', start_time: '08:30:00', end_time: '11:30:00' }
    ],
    max_appointments: 20,
    room_number: '',
    schedule_type: 'normal'
  })
  batchVisible.value = true
}

function addTimeSlot() {
  batchForm.timeSlots.push({
    time_slot: '下午',
    start_time: '13:30:00',
    end_time: '17:00:00'
  })
}

function removeTimeSlot(index) {
  batchForm.timeSlots.splice(index, 1)
}

function onBatchSave() {
  if (!batchFormRef.value) return
  batchFormRef.value.validate(async (valid) => {
    if (!valid) return
    batchSubmitting.value = true
    try {
      const dates = []
      const [startDate, endDate] = batchForm.dateRange
      const start = new Date(startDate)
      const end = new Date(endDate)
      for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
        dates.push(d.toISOString().split('T')[0])
      }
      const payload = {
        doctor_id: batchForm.doctor_id != null ? Number(batchForm.doctor_id) : null,
        department_id: batchForm.department_id != null ? Number(batchForm.department_id) : null,
        dates: dates,
        time_slots: batchForm.timeSlots,
        max_appointments: batchForm.max_appointments != null ? Number(batchForm.max_appointments) : null,
        room_number: batchForm.room_number,
        schedule_type: batchForm.schedule_type || 'normal'
      }
      await batchCreateSchedules(payload)
      ElMessage.success('批量创建成功')
      batchVisible.value = false
      fetchList()
    } catch (e) {
      ElMessage.error(e?.msg || e?.message || '批量创建失败')
    } finally {
      batchSubmitting.value = false
    }
  })
}

function onTimeSlotChange() {}

function onBatchSlotChange() {}

function onCopy(row) {
  const idNum = Number(row.id ?? row.schedule_id ?? row.scheduleId)
  if (!Number.isFinite(idNum)) {
    ElMessage.error('缺少排班ID，无法复制')
    return
  }
  copyForm.source_schedule_id = idNum
  copyForm.targetDates = []
  copyVisible.value = true
}

function onCopySave() {
  if (!copyFormRef.value) return
  copyFormRef.value.validate(async (valid) => {
    if (!valid) return
    copySubmitting.value = true
    try {
      await copySchedule({
        source_schedule_id: Number(copyForm.source_schedule_id),
        target_dates: copyForm.targetDates
      })
      ElMessage.success('复制成功')
      copyVisible.value = false
      fetchList()
    } catch (e) {
      ElMessage.error(e?.msg || e?.message || '复制失败')
    } finally {
      copySubmitting.value = false
    }
  })
}

function onCancel(row) {
  ElMessageBox.confirm('确认取消该排班吗？', '提示', { type: 'warning' })
    .then(async () => {
      try {
        const idNum = Number(row.id ?? row.schedule_id ?? row.scheduleId)
        if (!Number.isFinite(idNum)) throw new Error('缺少排班ID')
        await updateScheduleStatus(idNum, 'CANCELLED')
        ElMessage.success('取消成功')
        fetchList()
      } catch (e) {
        ElMessage.error(e?.msg || e?.message || '取消失败')
      }
    })
}

async function onEnable(row) {
  try {
    const idNum = Number(row.id ?? row.schedule_id ?? row.scheduleId)
    if (!Number.isFinite(idNum)) throw new Error('缺少排班ID')
    await updateScheduleStatus(idNum, 'AVAILABLE')
    ElMessage.success('已启用')
    fetchList()
  } catch (e) {
    ElMessage.error(e?.msg || e?.message || '启用失败')
  }
}

function onDelete(row) {
  ElMessageBox.confirm('确认删除该排班吗？', '提示', { type: 'warning' })
    .then(async () => {
      try {
        const idNum = Number(row.id ?? row.schedule_id ?? row.scheduleId)
        if (!Number.isFinite(idNum)) throw new Error('缺少排班ID')
        await deleteSchedule(idNum)
        ElMessage.success('删除成功')
        fetchList()
      } catch (e) {
        ElMessage.error(e?.msg || e?.message || '删除失败')
      }
    })
}

// 工具函数
function getTimeSlotClass(timeSlot) {
  const map = {
    '上午': 'status-success',
    '下午': 'status-warning'
  }
  return map[timeSlot] || 'status-info'
}

function getTimeSlotType(timeSlot) {
  const map = {
    '上午': 'success',
    '下午': 'warning'
  }
  return map[timeSlot] || ''
}

function getStatusClass(status) {
  const map = {
    'AVAILABLE': 'status-success',
    'FULL': 'status-warning',
    'CANCELLED': 'status-danger'
  }
  return map[status] || 'status-info'
}

function getStatusType(status) {
  const map = {
    'AVAILABLE': 'success',
    'FULL': 'warning',
    'CANCELLED': 'info'
  }
  return map[status] || ''
}

function getScheduleTypeClass(v) {
  const map = {
    'normal': 'status-info',
    'expert': 'status-warning'
  }
  return map[v] || 'status-info'
}

function getScheduleTypeType(v) {
  const map = {
    'normal': 'default',
    'expert': 'danger'
  }
  return map[v] || 'default'
}

function getScheduleTypeLabel(v) {
  const map = {
    'normal': '普通号',
    'expert': '专家号'
  }
  return map[v] || v || ''
}

function getStatusLabel(status) {
  const map = {
    'AVAILABLE': '可预约',
    'FULL': '已满',
    'CANCELLED': '已取消'
  }
  return map[status] || status
}

function getAppointmentProgress(row) {
  if (!row.max_appointments) return 0
  return Math.round(((row.current_appointments || 0) / row.max_appointments) * 100)
}

function getProgressColor(row) {
  const progress = getAppointmentProgress(row)
  if (progress >= 100) return '#F56C6C'
  if (progress >= 80) return '#E6A23C'
  return '#67C23A'
}

const uiViewMode = inject('ui_view_mode', ref('card'))
const isCardView = computed(() => (uiViewMode?.value || 'card') === 'card')

onMounted(() => {
  loadDepartments()
  fetchList()
})
</script>

<style scoped>
 .toolbar {
   display: flex;
   flex-direction: column;
   gap: 8px;
 }
 .filters {
   display: flex;
   flex-wrap: wrap;
   gap: 8px;
 }
 .actions {
   display: flex;
   justify-content: flex-end;
 }

 .cards-grid { display:grid; grid-template-columns:repeat(4,minmax(0,1fr)); gap:16px }
 @media (max-width:1200px){ .cards-grid{ grid-template-columns:repeat(3,minmax(0,1fr)) } }
 @media (max-width:900px){ .cards-grid{ grid-template-columns:repeat(2,minmax(0,1fr)) } }
 @media (max-width:600px){ .cards-grid{ grid-template-columns:1fr } }
 .schedule-card { border-radius:8px; transition: all .2s ease }
 .schedule-card:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,.15) }
 .card-header { font-weight:600; font-size:16px }
 .card-content { font-size:14px; line-height:1.6; padding: 0 16px; display:flex; flex-direction:column; gap:12px }
 .card-actions { display:flex; justify-content:flex-end; gap:8px; padding: 8px 16px }
</style>

