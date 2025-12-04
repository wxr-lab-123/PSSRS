<template>
  <el-container style="height:100vh">
    <el-aside width="220px">
      <div class="brand">患者自助挂号 - 后台</div>
      <el-menu :default-active="active" router>
        <el-menu-item v-if="hasPerm('admins:view')" index="/admin/admins">
          <el-icon><User /></el-icon>
          <span>管理员管理</span>
        </el-menu-item>
        <el-menu-item index="/admin">
          <el-icon><HomeFilled /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item v-if="hasPerm('patients:view')" index="/admin/patients">
          <el-icon><User /></el-icon>
          <span>患者管理</span>
        </el-menu-item>
        <el-menu-item v-if="hasPerm('departments:view')" index="/admin/departments">
          <el-icon><OfficeBuilding /></el-icon>
          <span>科室管理</span>
        </el-menu-item>
        <el-menu-item v-if="hasPerm('doctors:view')" index="/admin/doctors">
          <el-icon><UserFilled /></el-icon>
          <span>医生管理</span>
        </el-menu-item>
        <el-menu-item v-if="hasPerm('schedules:view')" index="/admin/schedules">
          <el-icon><Timer /></el-icon>
          <span>排班管理</span>
        </el-menu-item>
        <el-menu-item v-if="hasPerm('registrations:view')" index="/admin/registrations">
          <el-icon><Tickets /></el-icon>
          <span>挂号管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/leave-approvals">
          <el-icon><Tickets /></el-icon>
          <span>请假审批</span>
        </el-menu-item>
        <el-menu-item v-if="hasPerm('orders:view')" index="/admin/orders">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/refunds">
          <el-icon><List /></el-icon>
          <span>退款审批</span>
        </el-menu-item>
        <el-menu-item v-if="hasPerm('roles:view')" index="/admin/roles">
          <el-icon><Setting /></el-icon>
          <span>角色权限</span>
        </el-menu-item>
        <el-menu-item v-if="hasPerm('permissions:view')" index="/admin/permissions">
          <el-icon><Setting /></el-icon>
          <span>权限管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/settings">
          <el-icon><Tools /></el-icon>
          <span>个人信息设置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="left">
          <span class="route-title">{{ route.meta.title }}</span>
        </div>
        <div class="right">
          <el-button type="text" @click="goProfile">个人信息</el-button>
          <el-button type="text" @click="logout">退出登录</el-button>
        </div>
      </el-header>
      <el-main>
        <div class="content" :class="viewMode">
          <router-view />
        </div>
      </el-main>
      <el-dialog v-model="notifyVisible" title="新的请假申请" width="520px">
        <div v-if="notifyItem" style="display:flex; gap:12px; align-items:flex-start; justify-content:space-between">
          <div style="flex:1">
            <div>医生：{{ nameOfDoctor(notifyItem.doctorId) }}</div>
            <div>排班ID：{{ notifyItem.scheduleId }}</div>
            <div>日期：{{ notifyItem.date }}</div>
            <div>时间段：{{ notifyItem.timeSlot }}</div>
            <div>类型：{{ notifyItem.leaveType }}</div>
            <div>开始时间：{{ notifyItem.startTime }}</div>
            <div>结束时间：{{ notifyItem.endTime }}</div>
            <div>理由：{{ notifyItem.reason }}</div>
          </div>
          <el-image v-if="typeof notifyItem.attachments==='string' && /^https?:\/\//.test(notifyItem.attachments)" :src="notifyItem.attachments" :preview-src-list="[notifyItem.attachments]" fit="cover" style="width:64px;height:64px;border-radius:6px;margin-left:12px" />
        </div>
        <template #footer>
          <el-button @click="notifyVisible=false">忽略</el-button>
          <el-button type="primary" @click="goApprovals">确认</el-button>
        </template>
      </el-dialog>
      <el-button
        class="view-toggle"
        circle
        :title="viewMode === 'card' ? '切换为列表视图' : '切换为卡片视图'"
        @click="toggleViewMode"
      >
        {{ viewMode === 'card' ? '卡' : '表' }}
      </el-button>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref, onMounted, provide } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { ElMessage, ElNotification } from 'element-plus'
import { HomeFilled, User, OfficeBuilding, UserFilled, Timer, Tickets, List, Setting, Tools } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import { fetchDoctors } from '../api/doctors'

const route = useRoute()
const router = useRouter()
const active = computed(() => route.path)
const auth = useAuthStore()
const hasPerm = (code) => auth.hasPerm(code)

const viewMode = ref(localStorage.getItem('ui_view_mode') || 'card')
function toggleViewMode() {
  viewMode.value = viewMode.value === 'card' ? 'list' : 'card'
  localStorage.setItem('ui_view_mode', viewMode.value)
}
onMounted(() => {
  const saved = localStorage.getItem('ui_view_mode')
  if (saved) viewMode.value = saved
})
provide('ui_view_mode', viewMode)

function logout() {
  auth.logout()
  ElMessage.success('已退出')
  router.replace('/login')
}

function goProfile() {
  router.push('/admin/settings')
}

const notifyVisible = ref(false)
const notifyItem = ref(null)
const ws = ref(null)
const wsState = ref('idle')
let reconnectTimer = null
const doctorMap = ref({})

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
    ws.value.onopen = () => { wsState.value = 'open' }
    ws.value.onclose = () => { wsState.value = 'closed'; scheduleReconnect() }
    ws.value.onerror = () => { wsState.value = 'error'; scheduleReconnect() }
    ws.value.onmessage = (evt) => {
      try {
        const msg = JSON.parse(evt.data)
        if (msg && msg.type === 'LEAVE_REQUEST') {
          notifyItem.value = {
            scheduleId: msg.scheduleId,
            doctorId: msg.doctorId,
            date: msg.date,
            timeSlot: msg.timeSlot,
            leaveType: msg.leaveType || '',
            startTime: msg.startTime || '',
            endTime: msg.endTime || '',
            reason: msg.reason || '',
            attachments: typeof msg.attachments === 'string' ? msg.attachments : (Array.isArray(msg.attachments) ? (msg.attachments[0] || '') : '')
          }
          ElNotification({
            title: '新的请假申请',
            message: `医生：${nameOfDoctor(notifyItem.value.doctorId)} · 排班ID：${notifyItem.value.scheduleId} · 点击查看`,
            duration: 5000,
            position: 'top-right',
            onClick: () => { notifyVisible.value = true }
          })
        }
      } catch {}
    }
  } catch {}
}

function scheduleReconnect() {
  if (reconnectTimer) return
  reconnectTimer = setTimeout(() => { reconnectTimer = null; connectWS() }, 3000)
}

function goApprovals() {
  notifyVisible.value = false
  router.push('/admin/leave-approvals')
}

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

onMounted(() => { connectWS(); loadDoctorMap() })
</script>

<style scoped>
.brand {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  font-weight: 600;
  border-bottom: 1px solid var(--el-border-color)
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--el-border-color)
}
.route-title { font-size: 16px; font-weight: 600 }
.content { max-width: 1200px; margin: 0 auto; padding: 16px; width: 100%; transition: all .3s ease }
.view-toggle {
  position: fixed; top: 72px; right: 24px; z-index: 3000; width: 40px; height: 40px;
  border-radius: 50%; box-shadow: 0 2px 8px rgba(0,0,0,.1); background: #fff; transition: all .3s ease
}
.view-toggle:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,.15) }
:global(:root) { --ui-card-radius: 8px; --ui-card-shadow: 0 2px 8px rgba(0,0,0,.1); --ui-card-padding: 16px; --ui-gap: 12px }
:deep(.el-card) { border-radius: var(--ui-card-radius); box-shadow: var(--ui-card-shadow) }
:deep(.el-card__header) { padding: 16px 16px 8px }
:deep(.el-card__body) { padding: var(--ui-card-padding) }
</style>
