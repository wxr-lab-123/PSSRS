<template>
  <el-row :gutter="16">
    <el-col :span="6"><el-card><div class="kpi"><div>今日挂号</div><b>{{ data.today_registrations }}</b></div></el-card></el-col>
    <el-col :span="6"><el-card><div class="kpi"><div>待就诊</div><b>{{ data.today_pending_visits }}</b></div></el-card></el-col>
    <el-col :span="6"><el-card><div class="kpi"><div>医生在线</div><b>{{ data.online_doctors }}</b></div></el-card></el-col>
    <el-col :span="6"><el-card><div class="kpi"><div>退款申请</div><b>{{ data.refund_requests }}</b></div></el-card></el-col>
  </el-row>
  <div style="margin-top:12px">
    <el-button type="primary" @click="load" :loading="loading">刷新</el-button>
    <el-button @click="autoRefresh = !autoRefresh" :type="autoRefresh ? 'success' : 'default'">{{ autoRefresh ? '自动刷新中' : '开启自动刷新' }}</el-button>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import axios from 'axios'

const data = ref({ today_registrations: 0, today_pending_visits: 0, online_doctors: 0, refund_requests: 0 })
const loading = ref(false)
const autoRefresh = ref(false)
let timer = null

function getBaseURL(){
  const t = (typeof sessionStorage !== 'undefined' ? sessionStorage.getItem('auth_token') : null) || localStorage.getItem('auth_token')
  const base = (window.__API_BASE__ || '') || ''
  return { token: t, base: base }
}

async function load(){
  loading.value = true
  try {
    const { token } = getBaseURL()
    const res = await axios.get('/api/admin/dashboard/summary', { headers: { Authorization: token } })
    if (res?.data?.code === 0 || res?.data?.code === 200) {
      data.value = { ...data.value, ...(res.data.data || {}) }
    }
  } catch (e) {
    console.warn('加载仪表盘失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => { load(); timer = setInterval(()=>{ if (autoRefresh.value) load() }, 10000) })
onBeforeUnmount(() => { if (timer) clearInterval(timer) })
</script>

<style scoped>
.kpi { display:flex; flex-direction:column; gap:8px; text-align:center; padding:12px 0 }
.kpi b { font-size:24px }
</style>


