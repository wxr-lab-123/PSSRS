<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center;gap:12px">
        <div style="display:flex;align-items:center;gap:12px">
          <div>消息通知中心</div>
          <el-tag type="info">今日有 {{ summary.today_registrations || 0 }} 个患者挂号</el-tag>
          <el-tag type="success">在线医生 {{ summary.online_doctors || 0 }}</el-tag>
        </div>
        <el-space>
          <el-select v-model="query.type" placeholder="类型" clearable style="width:160px">
            <el-option label="全部" value="" />
            <el-option label="挂号提醒" value="APPOINTMENT_REMINDER" />
            <el-option label="患者到诊" value="PATIENT_ARRIVAL" />
            <el-option label="系统公告" value="SYSTEM_ANNOUNCEMENT" />
          </el-select>
          <el-select v-model="query.status" placeholder="状态" clearable style="width:140px">
            <el-option label="未读" value="未读" />
            <el-option label="已读" value="已读" />
          </el-select>
          <el-input v-model="query.q" placeholder="搜索标题或内容" style="width:240px" />
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-space>
      </div>
    </template>

    <div class="cards-grid">
      <el-card v-for="m in rows" :key="m.messageId" shadow="hover">
        <template #header>
          <div style="display:flex;justify-content:space-between;align-items:center">
            <div>{{ titleOf(m) }}</div>
            <el-tag :type="m.status==='未读'?'danger':'info'">{{ m.status }}</el-tag>
          </div>
        </template>
        <div class="content" v-if="m.messageType!=='SYSTEM_ANNOUNCEMENT'">{{ contentOf(m) }}</div>
        <div class="content" v-else v-html="contentOf(m)"></div>
        <div class="footer">
          <span>{{ m.createTime }}</span>
          <el-space>
            <el-button size="small" type="primary" @click="open(m)">查看</el-button>
            <el-button size="small" @click="mark(m)" :disabled="m.status==='已读'">标记已读</el-button>
          </el-space>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchStaffMessages, markRead, countUnread } from '../../api/messages'
import request from '../../api/request'

const summary = reactive({ today_registrations: 0, online_doctors: 0 })
const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10, type: '', status: '', q: '' })
const ws = ref(null)
const wsState = ref('idle')

function titleOf(m){
  if (m.messageType === 'PATIENT_ARRIVAL') return '患者到诊'
  if (m.messageType === 'TAKE_NUMBER_NOTICE') return '取号通知'
  if (m.messageType === 'SYSTEM_ANNOUNCEMENT') return '系统公告'
  if (m.messageType === 'APPOINTMENT_REMINDER') return '挂号提醒'
  return '通知'
}
function contentOf(m){ return m.content || '' }

async function fetchSummary(){
  try { const data = await request.get('/dashboard/summary'); Object.assign(summary, data||{}) } catch {}
}

async function fetchList(){
  loading.value = true
  try {
    const data = await fetchStaffMessages({ ...query })
    rows.value = data.records || []
    total.value = data.total || 0
  } catch (e) { ElMessage.error(e?.msg || e?.message || '加载消息失败') }
  finally { loading.value = false }
}
function onSearch(){ query.page = 1; fetchList() }
function onReset(){ Object.assign(query, { page:1, size:10, type:'', status:'', q:'' }); fetchList() }

function open(m){
  ElMessageBox.alert(contentOf(m), titleOf(m), { confirmButtonText: '知道了' })
}
async function mark(m){
  try { await markRead(m.messageId); m.status = '已读' } catch (e) { ElMessage.error(e?.msg || e?.message || '标记失败') }
}

onMounted(()=>{ fetchSummary(); fetchList() })

function connectWS(){
  try {
    wsState.value = 'connecting'
    const origin = window.location.origin
    const isHttps = origin.startsWith('https')
    const base = isHttps ? origin.replace('https','wss') : origin.replace('http','ws')
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
    ws.value.onmessage = (ev) => {
      try {
        const msg = JSON.parse(ev.data||'{}')
        if (msg.type === 'PATIENT_ARRIVAL' || msg.type === 'TAKE_NUMBER_NOTICE' || msg.type === 'SYSTEM_ANNOUNCEMENT') fetchList()
      } catch {}
    }
  } catch {}
}
onMounted(connectWS)
</script>

<style scoped>
.cards-grid { display:grid; grid-template-columns:repeat(3,minmax(0,1fr)); gap:16px }
@media (max-width:1000px){ .cards-grid{ grid-template-columns:repeat(2,minmax(0,1fr)) } }
@media (max-width:700px){ .cards-grid{ grid-template-columns:1fr } }
.content { padding: 0 8px 12px; min-height: 52px }
.footer { display:flex; justify-content:space-between; align-items:center; padding: 8px }
</style>
