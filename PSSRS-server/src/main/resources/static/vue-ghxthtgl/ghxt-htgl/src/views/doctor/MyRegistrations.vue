<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center;gap:12px">
        <div style="display:flex;align-items:center;gap:12px">
          <div>诊疗队列</div>
          <el-tag v-if="currentPatient" :type="queueStatusMeta(currentPatient?.queueStatus).type">
            当前：{{ currentPatient?.patientName || '-' }} · {{ queueStatusMeta(currentPatient?.queueStatus).label }}
          </el-tag>
          <el-tag v-if="estimatedWaitText" type="info">预计等待：{{ estimatedWaitText }}</el-tag>
        </div>
        <el-space>
          <el-date-picker v-model="query.date" type="date" placeholder="预约日期" value-format="YYYY-MM-DD" />
          <el-select v-model="query.queueStatus" placeholder="队列状态" clearable style="width:160px">
            <el-option label="等待" value="0" />
            <el-option label="已叫号" value="1" />
            <el-option label="就诊中" value="2" />
            <el-option label="已结束" value="3" />
          </el-select>
          <el-input v-model="avgMinutes" placeholder="平均就诊分钟数" style="width:160px" />
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
          <el-button type="success" @click="autoCallNext" :disabled="calling">一键叫号</el-button>
        </el-space>
      </div>
    </template>
    <el-table v-if="!isCardView" :data="rows" :loading="loading" style="width:100%">
      <el-table-column prop="registrationNo" label="挂号号" width="200" />
      <el-table-column prop="patientName" label="患者" />
      <el-table-column prop="departmentName" label="科室" />
      <el-table-column prop="appointmentDate" label="日期" />
      <el-table-column prop="timeSlot" label="时段" />
      <el-table-column label="队列状态" width="140">
        <template #default="{ row }">
          <el-tag :type="queueStatusMeta(row.queueStatus).type">
            {{ queueStatusMeta(row.queueStatus).label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="时间线" min-width="220">
        <template #default="{ row }">
          <div style="display:flex;gap:8px;flex-wrap:wrap">
            <span v-if="row.callTime">叫号：{{ row.callTime }}</span>
            <span v-if="row.startTime">开始：{{ row.startTime }}</span>
            <span v-if="row.endTime">结束：{{ row.endTime }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="360" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="call(row)" :disabled="calling || row.queueStatus===2">叫号</el-button>
          <el-button type="warning" size="small" @click="skip(row)" :disabled="row.queueStatus!==1">跳号</el-button>
          <el-button type="success" size="small" @click="recall(row)" :disabled="row.queueStatus!==0">重叫</el-button>
          <el-button type="primary" size="small" @click="start(row)" :disabled="row.queueStatus!==1">开始</el-button>
          <el-button type="danger" size="small" @click="finish(row)" :disabled="row.queueStatus!==2">结束</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-else class="cards-grid">
      <el-card v-for="row in rows" :key="row.registrationNo || row.id" class="reg-card" shadow="hover">
        <template #header>
          <div class="card-header">{{ row.registrationNo }}</div>
        </template>
        <div class="card-content">
          <div>患者：{{ row.patientName }}</div>
          <div>科室：{{ row.departmentName }}</div>
          <div>日期/时段：{{ row.appointmentDate }} / {{ row.timeSlot }}</div>
          <div>队列状态：<el-tag :type="queueStatusMeta(row.queueStatus).type">{{ queueStatusMeta(row.queueStatus).label }}</el-tag></div>
          <div v-if="row.callTime">叫号：{{ row.callTime }}</div>
          <div v-if="row.startTime">开始：{{ row.startTime }}</div>
          <div v-if="row.endTime">结束：{{ row.endTime }}</div>
        </div>
        <div class="card-actions" style="display:flex;gap:8px;padding:12px 16px">
          <el-button type="primary" size="small" @click="call(row)" :disabled="calling || row.queueStatus===2">叫号</el-button>
          <el-button type="warning" size="small" @click="skip(row)" :disabled="row.queueStatus!==1">跳号</el-button>
          <el-button type="success" size="small" @click="recall(row)" :disabled="row.queueStatus!==0">重叫</el-button>
          <el-button type="primary" size="small" @click="start(row)" :disabled="row.queueStatus!==1">开始</el-button>
          <el-button type="danger" size="small" @click="finish(row)" :disabled="row.queueStatus!==2">结束</el-button>
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
import { fetchDoctorOrders, callOrder, skipOrder, recallOrder, startVisit, endVisit } from '../../api/orders'
import request from '../../api/request'
import dayjs from 'dayjs'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10, date: '', queueStatus: '' })
const currentPatient = ref(null)
const calling = ref(false)
const avgMinutes = ref('5')
const ws = ref(null)
const wsState = ref('idle')
let jumpTimers = {}

function queueStatusMeta(s) {
  if (s === 1 || s === '1') return { label: '已叫号', type: 'info' }
  if (s === 2 || s === '2') return { label: '就诊中', type: 'success' }
  if (s === 3 || s === '3') return { label: '已结束', type: '' }
  return { label: '等待', type: '' }
}

async function fetchList() {
  loading.value = true
  try {
    const params = { ...query, date: query.date || dayjs().format('YYYY-MM-DD') }
    const data = await fetchDoctorOrders(params)
    rows.value = (data.records || []).map((r, idx) => ({
      ...r,
      queueStatus: r.queueStatus ?? 0,
      callTime: r.callTime || '',
      startTime: r.startTime || '',
      endTime: r.endTime || '',
      _orderIndex: idx
    }))
    total.value = data.total || 0
  } catch (e) { ElMessage.error(e?.msg || e?.message || '获取列表失败') }
  finally { loading.value = false }
}

function onSearch() { query.page = 1; fetchList() }
function onReset() { Object.assign(query, { page: 1, size: 10, date: '', queueStatus: '' }); fetchList() }

const uiViewMode = inject('ui_view_mode', ref('card'))
const isCardView = computed(() => (uiViewMode?.value || 'card') === 'card')

onMounted(fetchList)
function nextWaiting() {
  const arr = rows.value.filter(r => r.queueStatus === 0)
  return arr.length ? arr[0] : null
}
function estimatedWaitTextFn() {
  const arr = rows.value.filter(r => r.queueStatus === 0)
  const k = Number(avgMinutes.value || 5)
  const n = arr.length
  const minutes = Math.max(0, k * n)
  if (!minutes) return ''
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  if (h > 0) return `${h}小时${m}分钟`
  return `${m}分钟`
}
const estimatedWaitText = computed(() => estimatedWaitTextFn())
function connectWS() {
  try {
    wsState.value = 'connecting'
    const origin = window.location.origin
    const isHttps = origin.startsWith('https')
    const base = isHttps ? origin.replace('https', 'wss') : origin.replace('http', 'ws')
    const token = encodeURIComponent(String((typeof sessionStorage !== 'undefined' ? sessionStorage.getItem('auth_token') : localStorage.getItem('auth_token')) || ''))
    let rawUser = null
    try { rawUser = (typeof sessionStorage !== 'undefined' ? sessionStorage.getItem('auth_user') : localStorage.getItem('auth_user')) } catch {}
    let userId = ''
    try { const parsed = rawUser && JSON.parse(rawUser); userId = encodeURIComponent(String(parsed?.id || parsed?.userId || '')) } catch {}
    const url = `${base}/ws?role=doctor&token=${token}&userId=${userId}`
    ws.value = new WebSocket(url)
    ws.value.onopen = () => { wsState.value = 'open' }
    ws.value.onclose = () => { wsState.value = 'closed' }
    ws.value.onerror = () => { wsState.value = 'error' }
  } catch {}
}
function sendNotify(payload) {
  const msg = JSON.stringify({ type: 'CALL_PATIENT', ...payload })
  try {
    if (ws.value && wsState.value === 'open') ws.value.send(msg)
    else request.post('/api/admin/notifications/call', payload)
  } catch {}
}
function call(row) {
  if (calling.value) return
  calling.value = true
  const now = dayjs().format('YYYY-MM-DD HH:mm:ss')
  callOrder(row.id, { callTime: now }).then(() => {
    row.queueStatus = 1
    row.callTime = now
    currentPatient.value = row
    sendNotify({ orderId: row.registrationNo, patientName: row.patientName, doctorName: row.doctorName, departmentName: row.departmentName, callTime: now })
    clearTimeout(jumpTimers[row.id])
    jumpTimers[row.id] = setTimeout(() => { try { skip(row) } catch {} }, 10 * 60 * 1000)
  }).catch(e => { ElMessage.error(e?.msg || e?.message || '叫号失败') }).finally(() => { calling.value = false })
}
function autoCallNext() { const next = nextWaiting(); if (!next) { ElMessage.info('无等待患者'); return } call(next) }
function skip(row) {
  const now = dayjs().format('YYYY-MM-DD HH:mm:ss')
  skipOrder(row.id, { skipTime: now }).then(() => {
    row.queueStatus = 0
    row.callTime = ''
    const idx = rows.value.findIndex(r => r.id === row.id)
    if (idx >= 0) { const item = rows.value.splice(idx, 1)[0]; rows.value.push(item) }
  }).catch(e => { ElMessage.error(e?.msg || e?.message || '跳号失败') })
}
function recall(row) {
  const now = dayjs().format('YYYY-MM-DD HH:mm:ss')
  recallOrder(row.id, { recallTime: now }).then(() => {
    row.queueStatus = 1
    row.callTime = now
    sendNotify({ orderId: row.registrationNo, patientName: row.patientName, doctorName: row.doctorName, departmentName: row.departmentName, callTime: now })
  }).catch(e => { ElMessage.error(e?.msg || e?.message || '重叫失败') })
}
function start(row) {
  const now = dayjs().format('YYYY-MM-DD HH:mm:ss')
  startVisit(row.id, { startTime: now }).then(() => {
    row.queueStatus = 2
    row.startTime = now
  }).catch(e => { ElMessage.error(e?.msg || e?.message || '开始就诊失败') })
}
function finish(row) {
  const now = dayjs().format('YYYY-MM-DD HH:mm:ss')
  endVisit(row.id, { endTime: now }).then(() => {
    row.queueStatus = 3
    row.endTime = now
    clearTimeout(jumpTimers[row.id])
  }).catch(e => { ElMessage.error(e?.msg || e?.message || '结束就诊失败') })
}
onMounted(() => { connectWS() })
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
