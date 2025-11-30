<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <div>请假审批</div>
        <el-space>
          <el-tag :type="wsStateType">{{ wsStateText }}</el-tag>
          <el-button :disabled="wsState==='connecting'" @click="connectWS">重连</el-button>
          <el-button size="small" @click="sendTest">测试连接</el-button>
        </el-space>
      </div>
    </template>

    <el-table :data="requests" :loading="loading" style="width:100%">
      <el-table-column prop="scheduleId" label="排班ID" width="100" />
      <el-table-column label="医生" width="140">
        <template #default="{ row }">{{ nameOfDoctor(row.doctorId) }}</template>
      </el-table-column>
      <el-table-column prop="date" label="日期" width="120" />
      <el-table-column prop="timeSlot" label="时间段" width="100" />
      <el-table-column prop="leaveType" label="类型" width="100" />
      <el-table-column prop="startTime" label="开始时间" width="160" />
      <el-table-column prop="endTime" label="结束时间" width="160" />
      <el-table-column prop="reason" label="理由" min-width="180" />
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
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.state==='approved'?'success':row.state==='rejected'?'danger':row.state==='withdrawn'?'warning':'info'">
            {{ row.state==='approved'?'已批准':row.state==='rejected'?'已拒绝':row.state==='withdrawn'?'已撤销':'待审批' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button :disabled="row.state!=='pending'" type="success" size="small" @click="approve(row)">批准</el-button>
          <el-button :disabled="row.state!=='pending'" type="danger" size="small" @click="openReject(row)">拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="rejectVisible" title="拒绝申请" width="480px">
      <el-form ref="rejectFormRef" :model="rejectForm" label-width="100px">
        <el-form-item label="拒绝理由" prop="reason" :rules="[{ required: true, message: '请输入理由', trigger: 'blur' }]">
          <el-input v-model="rejectForm.reason" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible=false">取消</el-button>
        <el-button type="danger" :loading="rejectSubmitting" @click="submitReject">确认拒绝</el-button>
      </template>
    </el-dialog>

    
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import dayjs from 'dayjs'
import { updateScheduleStatus } from '../api/schedules'
import { fetchDoctors } from '../api/doctors'
import request from '../api/request'

const auth = useAuthStore()
const loading = ref(false)
const requests = ref([])
const doctorMap = ref({})

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
function sendTest() { if (!ws.value || ws.value.readyState !== 1) { ElMessage.error('连接未建立'); return } sendSafe({ type: 'TEST_ECHO', ts: Date.now(), from: 'admin' }) }

function wsUrl() {
  const origin = window.location.origin
  const isHttps = origin.startsWith('https')
  const base = isHttps ? origin.replace('https', 'wss') : origin.replace('http', 'ws')
  const uid = encodeURIComponent(String(auth.user?.id || ''))
  const token = encodeURIComponent(String((typeof sessionStorage !== 'undefined' ? sessionStorage.getItem('auth_token') : null) || localStorage.getItem('auth_token') || ''))
  return `${base}/ws?role=admin&userId=${uid}&token=${token}`
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
        if (msg && msg.type === 'LEAVE_REQUEST') {
          const exists = requests.value.find(r => r.scheduleId === msg.scheduleId)
          const item = {
            scheduleId: msg.scheduleId,
            doctorId: msg.doctorId,
            date: msg.date,
            timeSlot: toChineseTimeSlot(msg.timeSlot),
            leaveType: msg.leaveType || '',
            startTime: msg.startTime || '',
            endTime: msg.endTime || '',
            reason: msg.reason || '',
            attachments: typeof msg.attachments === 'string' ? msg.attachments : (Array.isArray(msg.attachments) ? (msg.attachments[0] || '') : ''),
            state: 'pending',
            timestamp: msg.timestamp || dayjs().toISOString()
          }
          if (exists) Object.assign(exists, item)
          else requests.value.push(item)
        }
      } catch {}
    }
  } catch {}
}

function scheduleReconnect() {
  if (reconnectTimer) return
  reconnectTimer = setTimeout(() => { reconnectTimer = null; connectWS() }, 3000)
}

function toChineseTimeSlot(v) { if (v === 'MORNING') return '上午'; if (v === 'AFTERNOON') return '下午'; if (v === 'EVENING') return '晚上'; return v }

function nameOfDoctor(id) { const v = doctorMap.value[id]; return v || String(id || '') }

async function loadDoctorMap() {
  try {
    const data = await fetchDoctors({ page: 1, size: 1000 })
    const list = Array.isArray(data?.records) ? data.records : []
    const map = {}
    for (const d of list) { map[d.id] = d.username || d.name || d.realName || d.doctorName || '' }
    doctorMap.value = map
  } catch { doctorMap.value = {} }
}

async function loadAllLeaves() {
  loading.value = true
  try {
    const resp = await request.get('/leave/all')
    const list = Array.isArray(resp?.records) ? resp.records : (Array.isArray(resp) ? resp : [])
    const mapped = list.map(r => ({
      scheduleId: r.scheduleId,
      doctorId: r.doctorId,
      date: r.date,
      timeSlot: toChineseTimeSlot(r.timeSlot),
      leaveType: r.leaveType || '',
      startTime: r.startTime ? dayjs(r.startTime).format('YYYY-MM-DD HH:mm:ss') : '',
      endTime: r.endTime ? dayjs(r.endTime).format('YYYY-MM-DD HH:mm:ss') : '',
      reason: r.reason || '',
      attachments: typeof r.attachments === 'string' ? r.attachments : '',
      state: r.status === 'APPROVED' ? 'approved' : r.status === 'REJECTED' ? 'rejected' : r.status === 'WITHDRAWN' ? 'withdrawn' : 'pending'
    }))
    requests.value = mapped
  } catch (e) {
    ElMessage.error(e?.msg || e?.message || '加载请假申请失败')
  } finally {
    loading.value = false
  }
}
async function approve(row) {
  try {
    // 后端写库：批准请假
    await request.post('/leave/approve', { doctorId: row.doctorId, scheduleId: row.scheduleId })
    // 取消排班（保持既有流程与缓存清理）
    const idNum = Number(row.scheduleId)
    if (!Number.isFinite(idNum)) throw new Error('排班ID无效')
    await updateScheduleStatus(idNum, 'CANCELLED')
    // 通知医生：审批结果
    if (ws.value && ws.value.readyState === 1) {
      const payload = { type: 'LEAVE_STATUS_UPDATE', scheduleId: row.scheduleId, doctorId: row.doctorId, status: 'APPROVED', reason: '' }
      ws.value.send(JSON.stringify(payload))
    }
    row.state = 'approved'
    ElMessage.success('已批准并取消排班')
  } catch (e) {
    ElMessage.error(e?.msg || e?.message || '操作失败')
  }
}

const rejectVisible = ref(false)
const rejectSubmitting = ref(false)
const rejectFormRef = ref()
const rejectForm = reactive({ scheduleId: null, reason: '' })

function openReject(row) {
  rejectForm.scheduleId = row.scheduleId
  rejectForm.reason = ''
  rejectVisible.value = true
}

function submitReject() {
  if (!rejectFormRef.value) return
  rejectFormRef.value.validate(async (valid) => {
    if (!valid) return
    rejectSubmitting.value = true
    try {
      const r = requests.value.find(x => x.scheduleId === rejectForm.scheduleId)
      await request.post('/leave/reject', { doctorId: r?.doctorId, scheduleId: rejectForm.scheduleId, reason: rejectForm.reason })
      const payload = { type: 'LEAVE_STATUS_UPDATE', scheduleId: rejectForm.scheduleId, doctorId: r?.doctorId, status: 'REJECTED', reason: rejectForm.reason }
      if (ws.value && ws.value.readyState === 1) {
        ws.value.send(JSON.stringify(payload))
      }
      const row = requests.value.find(r => r.scheduleId === rejectForm.scheduleId)
      if (row) row.state = 'rejected'
      ElMessage.success('已拒绝')
      rejectVisible.value = false
    } catch (e) {
      ElMessage.error(e?.msg || e?.message || '操作失败')
    } finally {
      rejectSubmitting.value = false
    }
  })
}


onMounted(async () => { connectWS(); await loadAllLeaves() })
onMounted(async () => { await loadDoctorMap() })
</script>

<style scoped>
.el-link + .el-link { margin-left: 8px }
</style>
