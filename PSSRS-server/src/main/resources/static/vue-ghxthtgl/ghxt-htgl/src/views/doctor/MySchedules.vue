<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <div>我的排班 · {{ weekLabel }}</div>
        <el-space class="toolbar-buttons">
          <el-button :disabled="weekIndex === 0" @click="prevWeek">上一周</el-button>
          <el-button :disabled="weekIndex >= totalWeeks - 1" @click="nextWeek">下一周</el-button>
          <el-tag :type="wsStateType">{{ wsStateText }}</el-tag>
          <el-button size="small" @click="sendTest">测试连接</el-button>
        </el-space>
      </div>
    </template>

    <div v-loading="loading" class="calendar-wrap">
      <el-carousel v-if="isCardView" height="540px" indicator-position="none" :autoplay="false" ref="carouselRef" :initial-index="weekIndex" @change="onCarouselChange">
        <el-carousel-item v-for="(week, wi) in calendarWeeks" :key="wi">
          <div class="week-grid" @touchstart="onTouchStart($event)" @touchmove="onTouchMove($event)" @touchend="onTouchEnd($event)">
            <div v-for="day in week" :key="day.date" class="day-cell" @click="toggleDay(day.date)">
              <div class="day-header">
                <div class="day-date">{{ day.label }}</div>
                <div class="day-sub">{{ day.weekday }} · {{ day.date }}</div>
              </div>
              <div class="day-body">
                <template v-if="(groupedOf(wi)[day.date] || []).length">
                  <el-card v-for="s in displaySchedules(wi, day.date)" :key="s.id" shadow="never" class="sched-item">
                    <div class="sched-line">{{ s.time_slot }} · {{ s.department_name }} · {{ getScheduleTypeLabel(s.schedule_type) }}</div>
                    <div class="sched-actions">
                      <el-tag size="small" :type="getStatusType(s.status)">{{ getStatusLabel(s.status) }}</el-tag>
                      <el-button v-if="canLeave(s)" type="warning" size="small" @click="openLeave(s)">申请请假</el-button>
                      <template v-if="leaveStatus[s.id] && (leaveStatus[s.id].state==='success' || leaveStatus[s.id].state==='error')">
                        <el-tag size="small" :type="leaveStatus[s.id].state === 'success' ? 'success' : 'danger'">
                          {{ leaveStatusText(leaveStatus[s.id]) }}
                        </el-tag>
                      </template>
                    </div>
                  </el-card>
                </template>
                <template v-else>
                  <div class="empty-day">无排班</div>
                </template>
              </div>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
      <el-table v-else :data="tableRows" style="width:100%">
        <el-table-column prop="schedule_date" label="日期" width="120" />
        <el-table-column prop="weekday" label="星期" width="80" />
        <el-table-column prop="time_slot" label="时间段" width="100" />
        <el-table-column prop="department_name" label="科室" width="140" />
        <el-table-column label="号种" width="100">
          <template #default="{ row }">{{ getScheduleTypeLabel(row.schedule_type) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><el-tag size="small" :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="start_time" label="开始" width="100" />
        <el-table-column prop="end_time" label="结束" width="100" />
        <el-table-column prop="room_number" label="诊室" width="80" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button v-if="canLeave(row)" type="warning" size="small" @click="openLeave(row)">申请请假</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="leaveVisible" title="申请请假" width="560px">
      <el-form ref="leaveFormRef" :model="leaveForm" :rules="leaveRules" label-width="100px">
        <el-row :gutter="12">
          <el-col :xs="24" :sm="12">
            <el-form-item label="请假类型" prop="leaveType">
              <el-select v-model="leaveForm.leaveType" placeholder="请选择" style="width:100%">
                <el-option label="病假" value="sick" />
                <el-option label="事假" value="personal" />
                <el-option label="年假" value="annual" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="附件" prop="attachments">
              <el-upload ref="uploadRef" action="/api/admin/files/upload" :headers="uploadHeaders" :auto-upload="true" :show-file-list="true" :http-request="handleUpload" :on-remove="handleRemove" accept="image/*">
                <el-button>上传附件</el-button>
              </el-upload>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :xs="24" :sm="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker v-model="leaveForm.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker v-model="leaveForm.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="请假事由" prop="reason">
          <el-input v-model="leaveForm.reason" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="leaveVisible=false">取消</el-button>
          <el-button type="primary" :loading="leaveSubmitting || uploadingAttachment" @click="submitLeave">提交申请</el-button>
        </span>
      </template>
    </el-dialog>

    <el-card class="leave-status-card" style="margin-top:12px">
      <template #header>
        <div>我的请假申请状态</div>
      </template>
      <el-empty v-if="!leaveStatusList.length" description="暂无申请" />
      <el-table v-else :data="leaveStatusList" size="small" style="width:100%">
        <el-table-column prop="scheduleId" label="排班ID" width="100" />
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="timeSlot" label="时间段" width="100" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">{{ getLeaveTypeLabel(row.leaveType) }}</template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="160" />
        <el-table-column prop="endTime" label="结束时间" width="160" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.type">{{ row.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="approvalTime" label="审批时间" width="160">
          <template #default="{ row }">{{ row.approvalTime || '-' }}</template>
        </el-table-column>
        <el-table-column prop="approverName" label="审批人" width="100" />
        <el-table-column label="附件" width="120">
          <template #default="{ row }">
            <template v-if="typeof row.attachments==='string' && row.attachments">
              <el-image
                v-if="/^https?:\/\//.test(row.attachments)"
                :src="row.attachments"
                :preview-src-list="[row.attachments]"
                fit="cover"
                style="width:40px;height:40px;border-radius:4px"
              />
              <el-tag v-else type="info">{{ row.attachments }}</el-tag>
            </template>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="理由" />
        <el-table-column label="撤销" width="100">
          <template #default="{ row }">
            <el-button type="danger" size="small" :disabled="row.state!=='pending'" @click="cancelMyLeave(row)">撤销</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, inject } from 'vue'
import dayjs from 'dayjs'
import weekOfYear from 'dayjs/plugin/weekOfYear'
import { ElMessage, ElNotification } from 'element-plus'
import { useAuthStore } from '../../stores/auth'
import { fetchSchedules, fetchDoctorScheduleCalendar } from '../../api/schedules'
import request from '../../api/request'
import { uploadToOSS } from '../../api/upload'
 

const auth = useAuthStore()
const uploadHeaders = computed(() => {
  const token = (typeof sessionStorage !== 'undefined' ? sessionStorage.getItem('auth_token') : null) || localStorage.getItem('auth_token')
  return token ? { Authorization: token } : {}
})
const loading = ref(false)
const rows = ref([])
const expandedDays = reactive({})
const weekCache = reactive({})
const grouped = reactive({})
const carouselRef = ref()

dayjs.extend(weekOfYear)
const today = dayjs().startOf('day')
function mondayOf(d) { const t = dayjs(d); const off = (t.day() + 6) % 7; return t.subtract(off, 'day').startOf('day') }
const startWeek = mondayOf(today)
const endRange = dayjs().add(14, 'day').startOf('day')
const lastWeekStart = mondayOf(endRange)
const totalWeeks = Math.floor(lastWeekStart.diff(startWeek, 'day') / 7) + 1
const weekIndex = ref(0)
const weekLabel = computed(() => `第${mondayOf(startWeek.add(weekIndex.value, 'week')).week()}周`)

const daysByWeek = computed(() => {
  const weeks = []
  for (let i = 0; i < totalWeeks; i++) {
    const start = startWeek.add(i, 'week')
    const arr = []
    for (let d = 0; d < 7; d++) {
      const cur = start.add(d, 'day')
      const wd = ['周日','周一','周二','周三','周四','周五','周六'][cur.day()]
      arr.push({ date: cur.format('YYYY-MM-DD'), label: cur.format('MM-DD'), weekday: wd })
    }
    weeks.push(arr)
  }
  return weeks
})

const calendarWeeks = computed(() => daysByWeek.value)

const uiViewMode = inject('ui_view_mode', ref('card'))
const isCardView = computed(() => (uiViewMode?.value || 'card') === 'card')

const tableRows = computed(() => {
  const m = groupedOf(weekIndex.value)
  const dates = Object.keys(m).sort()
  const out = []
  for (const d of dates) {
    const wd = ['周日','周一','周二','周三','周四','周五','周六'][dayjs(d).day()]
    const arr = m[d] || []
    for (const s of arr) out.push({ ...s, schedule_date: d, weekday: wd })
  }
  return out
})

function toChineseTimeSlot(v) {
  if (v === 'MORNING') return '上午'
  if (v === 'AFTERNOON') return '下午'
  if (v === 'EVENING') return '晚上'
  return v
}

function normalize(list) {
  return (list || []).map(r => ({
    ...r,
    id: r.id ?? r.schedule_id ?? r.scheduleId,
    schedule_date: r.schedule_date ?? r.scheduleDate,
    time_slot: toChineseTimeSlot(r.time_slot ?? r.timeSlot),
    department_name: r.department_name ?? r.departmentName,
    schedule_type: r.schedule_type ?? r.scheduleType,
    start_time: r.start_time ?? r.startTime,
    end_time: r.end_time ?? r.endTime,
    room_number: r.room_number ?? r.roomNumber,
    doctor_name: r.doctor_name ?? r.doctorName
  }))
}

async function loadData() {
  loading.value = true
  try {
    await loadWeek(weekIndex.value)
    const next = weekIndex.value + 1
    if (next < totalWeeks) await loadWeek(next)
  } catch (e) {
    ElMessage.error(e?.msg || e?.message || '加载排班失败')
  } finally {
    loading.value = false
  }
}

function weekKey(i) {
  const s = startWeek.add(i, 'week')
  const e = s.add(6, 'day')
  return `${s.format('YYYY-MM-DD')}|${e.format('YYYY-MM-DD')}`
}

async function loadWeek(i) {
  const key = weekKey(i)
  if (weekCache[key]) return
  const s = startWeek.add(i, 'week').format('YYYY-MM-DD')
  const e = startWeek.add(i, 'week').add(6, 'day').format('YYYY-MM-DD')
  let list = []
  try {
    const resp = await fetchDoctorScheduleCalendar(auth.user?.id, s, e)
    const data = Array.isArray(resp?.records) ? resp.records : (Array.isArray(resp) ? resp : [])
    list = normalize(data)
  } catch {
    const all = await fetchSchedules({ doctor_id: auth.user?.id, page: 1, limit: 500 })
    const arr = Array.isArray(all?.records) ? all.records : (Array.isArray(all) ? all : [])
    list = normalize(arr).filter(x => x.schedule_date >= s && x.schedule_date <= e)
  }
  const map = {}
  const weekDays = daysByWeek.value[i] || []
  for (const d of weekDays) map[d.date] = []
  for (const r of list) if (map[r.schedule_date]) map[r.schedule_date].push(r)
  weekCache[key] = map
}

function groupedOf(i) { const key = weekKey(i); return weekCache[key] || {} }

function displaySchedules(i, date) {
  const arr = groupedOf(i)[date] || []
  if (expandedDays[date]) return arr
  return arr.slice(0, 3)
}

function toggleDay(date) { expandedDays[date] = !expandedDays[date] }

function getStatusType(status) {
  const map = { AVAILABLE: 'success', FULL: 'warning', CANCELLED: 'info' }
  return map[status] || ''
}

function getStatusLabel(status) {
  const map = { AVAILABLE: '可预约', FULL: '已满', CANCELLED: '已取消' }
  return map[status] || status
}

function getScheduleTypeLabel(v) {
  const map = { normal: '普通号', expert: '专家号' }
  return map[v] || v || ''
}

function prevWeek() { if (weekIndex.value === 0) return; weekIndex.value -= 1; setActiveWeek() }
function nextWeek() { if (weekIndex.value >= totalWeeks - 1) return; weekIndex.value += 1; setActiveWeek() }
function onCarouselChange(i) { weekIndex.value = i; setActiveWeek() }
function setActiveWeek() { if (carouselRef.value) carouselRef.value.setActiveItem(weekIndex.value); loadWeek(weekIndex.value) }

let touchX = 0
function onTouchStart(e) { touchX = e.touches?.[0]?.clientX || 0 }
function onTouchMove(e) {}
function onTouchEnd(e) {
  const dx = (e.changedTouches?.[0]?.clientX || 0) - touchX
  if (Math.abs(dx) > 40) { dx < 0 ? nextWeek() : prevWeek() }
  touchX = 0
}

const leaveVisible = ref(false)
const leaveSubmitting = ref(false)
const leaveFormRef = ref()
const leaveForm = reactive({ scheduleId: null, date: '', startTime: '', endTime: '', timeSlot: '', reason: '', leaveType: 'personal', attachments: '' })
const activeSchedule = ref(null)
const uploadRef = ref()
const uploadingAttachment = ref(false)
function validateTimeRange(_, __, cb) {
  const s = leaveForm.startTime
  const e = leaveForm.endTime
  if (!s || !e) return cb()
  const fmt = /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/
  if (!fmt.test(s) || !fmt.test(e)) return cb(new Error('时间格式不正确'))
  if (dayjs(e).isBefore(dayjs(s))) return cb(new Error('结束时间需晚于开始时间'))
  const sch = activeSchedule.value
  if (sch) {
    const d = sch.schedule_date
    const start = sch.start_time ? dayjs(`${d} ${sch.start_time}`) : null
    const end = sch.end_time ? dayjs(`${d} ${sch.end_time}`) : null
    const sd = dayjs(s)
    const ed = dayjs(e)
    if (start && end) {
      if (sd.isBefore(start) || ed.isAfter(end)) return cb(new Error('请假时间需在排班时间范围内'))
    }
    if (sd.format('YYYY-MM-DD') !== d || ed.format('YYYY-MM-DD') !== d) return cb(new Error('请选择同一天的时间'))
  }
  cb()
}
const leaveRules = {
  leaveType: [{ required: true, message: '请选择请假类型', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }, { validator: validateTimeRange, trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }, { validator: validateTimeRange, trigger: 'change' }],
  reason: [{ required: true, message: '请输入请假原因', trigger: 'blur' }]
}

function canLeave(s) { return s.status !== 'CANCELLED' }

function openLeave(s) {
  const start = s.start_time ? dayjs(`${s.schedule_date} ${s.start_time}`).format('YYYY-MM-DD HH:mm:ss') : ''
  const end = s.end_time ? dayjs(`${s.schedule_date} ${s.end_time}`).format('YYYY-MM-DD HH:mm:ss') : ''
  activeSchedule.value = s
  Object.assign(leaveForm, { scheduleId: s.id, date: s.schedule_date, startTime: start, endTime: end, timeSlot: s.time_slot, reason: '', leaveType: 'personal', attachments: '' })
  uploadingAttachment.value = false
  leaveVisible.value = true
}

async function handleUpload({ file, onSuccess, onError }) {
  try {
    uploadingAttachment.value = true
    const data = await uploadToOSS(file, 'leave')
    leaveForm.attachments = (data.url || data.path || '')
    onSuccess && onSuccess(data)
    uploadingAttachment.value = false
  } catch (e) {
    uploadingAttachment.value = false
    onError && onError(e)
  }
}

function handleRemove() { leaveForm.attachments = '' }

 

const ws = ref(null)
const wsState = ref('idle')
let hbTimer = null
let reconnectTimer = null
const wsLogs = ref([])
function pushLog(type, data) { wsLogs.value.push({ ts: dayjs().format('HH:mm:ss'), type, data }); if (wsLogs.value.length > 100) wsLogs.value.shift() }
 
const wsStateText = computed(() => wsState.value === 'connecting' ? '连接中' : wsState.value === 'open' ? '已连接' : wsState.value === 'closed' ? '已关闭' : wsState.value === 'error' ? '错误' : '空闲')
const wsStateType = computed(() => wsState.value === 'open' ? 'success' : wsState.value === 'connecting' ? 'warning' : wsState.value === 'error' ? 'danger' : 'info')
let sendWindowStart = 0
let sendCount = 0
function sendSafe(obj) { const now = Date.now(); if (now - sendWindowStart > 1000) { sendWindowStart = now; sendCount = 0 } if (sendCount >= 10) { ElMessage.error('发送频率过高'); return } if (ws.value && ws.value.readyState === 1) { ws.value.send(JSON.stringify(obj)); sendCount += 1; pushLog('send', obj) } }
function sendTest() { if (!ws.value || ws.value.readyState !== 1) { ElMessage.error('连接未建立'); return } sendSafe({ type: 'TEST_ECHO', ts: Date.now(), from: 'doctor' }) }

function wsUrl() {
  const origin = window.location.origin
  const isHttps = origin.startsWith('https')
  const base = isHttps ? origin.replace('https', 'wss') : origin.replace('http', 'ws')
  const token = encodeURIComponent(String((typeof sessionStorage !== 'undefined' ? sessionStorage.getItem('auth_token') : localStorage.getItem('auth_token')) || ''))
  let rawUser = null
  try { rawUser = (typeof sessionStorage !== 'undefined' ? sessionStorage.getItem('auth_user') : localStorage.getItem('auth_user')) } catch {}
  let userId = ''
  try { const parsed = rawUser && JSON.parse(rawUser); userId = encodeURIComponent(String(parsed?.id || parsed?.userId || '')) } catch {}
  return `${base}/ws?role=doctor&userId=${userId}&token=${token}`
}

function connectWS() {
  try {
    wsState.value = 'connecting'
    ws.value = new WebSocket(wsUrl())
    ws.value.onopen = () => {
      wsState.value = 'open'
      pushLog('open', {})
      if (hbTimer) clearInterval(hbTimer)
      hbTimer = setInterval(() => { if (ws.value && ws.value.readyState === 1) { ws.value.send(JSON.stringify({ type: 'PING', ts: Date.now() })); pushLog('ping', { ts: Date.now() }) } }, 25000)
    }
    ws.value.onclose = () => { wsState.value = 'closed'; pushLog('close', {}); scheduleReconnect() }
    ws.value.onerror = () => { wsState.value = 'error'; pushLog('error', {}); scheduleReconnect() }
    ws.value.onmessage = (evt) => {
      try {
        const msg = JSON.parse(evt.data)
        pushLog('message', msg)
        if (msg && msg.type === 'PONG') {}
        if (msg && msg.type === 'LEAVE_STATUS_UPDATE') {
          const id = msg.scheduleId
          if (id) {
            const prev = leaveStatus[id] || {}
            const st = msg.status === 'APPROVED' ? 'success' : msg.status === 'REJECTED' ? 'error' : msg.status === 'WITHDRAWN' ? 'withdrawn' : 'pending'
            leaveStatus[id] = { ...prev, state: st, reason: msg.reason || '' }
            const label = st === 'success' ? '已批准' : st === 'error' ? (msg.reason ? `已拒绝 · ${msg.reason}` : '已拒绝') : st === 'withdrawn' ? '已撤销' : '待审批'
            const type = st === 'success' ? 'success' : st === 'error' ? 'error' : st === 'withdrawn' ? 'warning' : 'info'
            ElNotification({ title: '请假审批结果', message: `排班ID：${id} · ${label}`, type, duration: 5000, position: 'top-right' })
          }
        }
      } catch {}
    }
  } catch {}
}

function scheduleReconnect() {
  if (reconnectTimer) return
  reconnectTimer = setTimeout(() => { reconnectTimer = null; connectWS() }, 3000)
}

const leaveStatus = reactive({})
function leaveStatusText(s) { if (s.state === 'success') return '已批准'; if (s.state === 'error') return s.reason ? `已拒绝 · ${s.reason}` : '已拒绝'; if (s.state === 'withdrawn') return '已撤销'; return '待审批' }
function getLeaveTypeLabel(v) { const m = { personal: '事假', sick: '病假', annual: '年假', other: '其他' }; return m[v] || v || '-' }
const leaveStatusList = computed(() => {
  const out = []
  for (const id of Object.keys(leaveStatus)) {
    const s = leaveStatus[id] || {}
    const label = s.state === 'success' ? '已批准' : s.state === 'error' ? '已拒绝' : s.state === 'withdrawn' ? '已撤销' : '待审批'
    const type = s.state === 'success' ? 'success' : s.state === 'error' ? 'danger' : s.state === 'withdrawn' ? 'warning' : 'info'
    out.push({
      scheduleId: Number(id) || id,
      date: s.date || '-',
      timeSlot: s.timeSlot || '-',
      leaveType: s.leaveType || '-',
      startTime: s.startTime || '-',
      endTime: s.endTime || '-',
      approvalTime: s.approvalTime || '',
      approverName: s.approverName || '',
      attachments: s.attachments || '',
      state: s.state || 'pending',
      label,
      type,
      reason: s.reason || ''
    })
  }
  return out
})

async function loadMyLeaves() {
  try {
    const resp = await request.get('/leave', { params: { doctorId: auth.user?.id } })
    const list = Array.isArray(resp?.records) ? resp.records : (Array.isArray(resp) ? resp : [])
    for (const r of list) {
      const st = r.status === 'APPROVED' ? 'success' : r.status === 'REJECTED' ? 'error' : r.status === 'WITHDRAWN' ? 'withdrawn' : 'pending'
      leaveStatus[r.scheduleId] = {
        state: st,
        date: r.date,
        timeSlot: toChineseTimeSlot(r.timeSlot),
        leaveType: r.leaveType,
        startTime: r.startTime ? dayjs(r.startTime).format('YYYY-MM-DD HH:mm:ss') : '',
        endTime: r.endTime ? dayjs(r.endTime).format('YYYY-MM-DD HH:mm:ss') : '',
        approvalTime: r.approvalTime ? dayjs(r.approvalTime).format('YYYY-MM-DD HH:mm:ss') : '',
        approverName: r.approverName,
        reason: r.reason,
        attachments: r.attachments
      }
    }
  } catch {}
}

async function cancelMyLeave(row) {
  try {
    await request.post('/leave/cancel', { doctorId: auth.user?.id, scheduleId: row.scheduleId })
    if (ws.value && ws.value.readyState === 1) {
      ws.value.send(JSON.stringify({ type: 'LEAVE_STATUS_UPDATE', scheduleId: row.scheduleId, doctorId: auth.user?.id, status: 'WITHDRAWN' }))
    }
    const prev = leaveStatus[row.scheduleId] || {}
    leaveStatus[row.scheduleId] = { ...prev, state: 'withdrawn' }
    ElMessage.success('已撤销申请')
  } catch (e) {
      ElMessage.error(e?.msg || e?.message || '撤销失败')
    }
}

function submitLeave() {
  if (!leaveFormRef.value) return
  leaveFormRef.value.validate(async (valid) => {
    if (!valid) return
    leaveSubmitting.value = true
    try {
      const payload = {
        type: 'LEAVE_REQUEST',
        doctorId: auth.user?.id,
        scheduleId: leaveForm.scheduleId,
        date: leaveForm.date,
        leaveType: leaveForm.leaveType,
        startTime: leaveForm.startTime,
        endTime: leaveForm.endTime,
        timeSlot: leaveForm.timeSlot,
        reason: leaveForm.reason,
        attachments: leaveForm.attachments,
        timestamp: dayjs().toISOString()
      }
      await request.post('/leave/apply', payload)
      if (ws.value && ws.value.readyState === 1) {
        wsState.value = 'sending'
        ws.value.send(JSON.stringify(payload))
        leaveStatus[leaveForm.scheduleId] = {
          state: 'pending',
          date: leaveForm.date,
          timeSlot: leaveForm.timeSlot,
          leaveType: leaveForm.leaveType,
          startTime: leaveForm.startTime,
          endTime: leaveForm.endTime,
          reason: leaveForm.reason,
          attachments: leaveForm.attachments
        }
        ElMessage.success('申请已提交')
        leaveVisible.value = false
      } else {
        ElMessage.error('连接未建立，稍后重试')
      }
    } catch (e) {
      ElMessage.error(e?.msg || e?.message || '提交失败')
    } finally {
      leaveSubmitting.value = false
    }
  })
}

onMounted(async () => { connectWS(); await loadData(); await loadMyLeaves(); await nextTick(); setActiveWeek() })
</script>

<style scoped>
.calendar-wrap { width:100% }
.week-grid { display:grid; grid-template-columns:repeat(7,minmax(0,1fr)); gap:12px }
.day-cell { border:1px solid #ebeef5; border-radius:8px; padding:8px; min-height:140px; display:flex; flex-direction:column }
.day-header { display:flex; justify-content:space-between; font-weight:600; margin-bottom:8px }
.day-date { font-size:14px }
.day-sub { font-size:12px; color:#909399 }
.empty-day { color:#c0c4cc; font-size:12px; text-align:center; padding:24px 0 }
.sched-item { margin-bottom:8px }
.sched-line { font-size:13px }
.sched-actions { display:flex; align-items:center; gap:8px; margin-top:6px }
/* 响应式按钮适配 */
.toolbar-buttons { display:flex; flex-wrap:wrap; gap:8px }
.toolbar-buttons :deep(.el-button) { min-width:88px; white-space:nowrap; padding:6px 12px }
.sched-actions { flex-wrap: wrap }
.day-cell :deep(.el-button) { width:auto; max-width:100%; min-width:96px; white-space:nowrap; padding:6px 12px }
.sched-actions :deep(.el-tag) { flex-shrink:0 }
@media (max-width:480px){
  .day-cell :deep(.el-button){ min-width:88px; padding:6px 10px }
  .toolbar-buttons :deep(.el-button){ min-width:88px }
}
@media (max-width:768px){
  .day-cell :deep(.el-button){ min-width:92px }
}
@media (min-width:1280px){
  .day-cell :deep(.el-button){ min-width:100px }
}
@media (max-width:900px){ .week-grid{ grid-template-columns:repeat(1,minmax(0,1fr)) } }
</style>
