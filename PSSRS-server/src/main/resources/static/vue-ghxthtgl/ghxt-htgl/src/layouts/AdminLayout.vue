<template>
  <el-container class="app-wrapper">
    <el-aside width="240px" class="app-sidebar">
      <div class="brand">
        <el-icon class="brand-logo" :size="24"><FirstAidKit /></el-icon>
        <span class="brand-text">PSSRS 管理后台</span>
      </div>
      <el-menu 
        :default-active="active" 
        router 
        class="sidebar-menu"
        :unique-opened="true"
      >
        <div class="menu-group-title">系统管理</div>
        <el-menu-item index="/admin">
          <el-icon><HomeFilled /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        
        <div class="menu-group-title">业务管理</div>
        <el-menu-item v-if="hasPerm('registrations:view')" index="/admin/registrations">
          <el-icon><Tickets /></el-icon>
          <span>挂号管理</span>
        </el-menu-item>
        <el-menu-item v-if="hasPerm('orders:view')" index="/admin/orders">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/leave-approvals">
          <el-icon><Stamp /></el-icon>
          <span>请假审批</span>
        </el-menu-item>
        <el-menu-item index="/admin/refunds">
          <el-icon><Money /></el-icon>
          <span>退款审批</span>
        </el-menu-item>

        <div class="menu-group-title">资源管理</div>
        <el-menu-item v-if="hasPerm('patients:view')" index="/admin/patients">
          <el-icon><User /></el-icon>
          <span>患者管理</span>
        </el-menu-item>
        <el-menu-item v-if="hasPerm('doctors:view')" index="/admin/doctors">
          <el-icon><UserFilled /></el-icon>
          <span>医生管理</span>
        </el-menu-item>
        <el-menu-item v-if="hasPerm('departments:view')" index="/admin/departments">
          <el-icon><OfficeBuilding /></el-icon>
          <span>科室管理</span>
        </el-menu-item>
        <el-menu-item v-if="hasPerm('schedules:view')" index="/admin/schedules">
          <el-icon><Timer /></el-icon>
          <span>排班管理</span>
        </el-menu-item>

        <div class="menu-group-title">设置</div>
        <el-menu-item v-if="hasPerm('admins:view')" index="/admin/admins">
          <el-icon><Avatar /></el-icon>
          <span>管理员管理</span>
        </el-menu-item>
        <el-menu-item v-if="hasPerm('roles:view')" index="/admin/roles">
          <el-icon><Lock /></el-icon>
          <span>角色权限</span>
        </el-menu-item>
        <el-menu-item v-if="hasPerm('permissions:view')" index="/admin/permissions">
          <el-icon><Key /></el-icon>
          <span>权限资源</span>
        </el-menu-item>
        <el-menu-item index="/admin/settings">
          <el-icon><Tools /></el-icon>
          <span>个人设置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="main-container">
      <el-header class="app-header">
        <div class="header-left">
          <div class="page-breadcrumb">
            <span class="breadcrumb-item">首页</span>
            <el-icon class="separator"><ArrowRight /></el-icon>
            <span class="breadcrumb-item active">{{ route.meta.title }}</span>
          </div>
        </div>
        <div class="header-right">
          <div class="action-item" @click="toggleViewMode" :title="viewMode === 'card' ? '切换为列表视图' : '切换为卡片视图'">
            <el-icon :size="18">
              <Operation v-if="viewMode === 'card'" />
              <Grid v-else />
            </el-icon>
          </div>
          <div class="user-profile">
            <el-dropdown trigger="click" @command="handleCommand">
              <div class="avatar-wrapper">
                <el-avatar :size="32" :icon="UserFilled" class="user-avatar" />
                <span class="username">{{ auth.user?.username || '管理员' }}</span>
                <el-icon><CaretBottom /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>

      <el-main class="app-main">
        <div class="content-wrapper" :class="viewMode">
          <router-view v-slot="{ Component }">
            <transition name="fade-transform" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </el-main>

      <el-dialog v-model="notifyVisible" title="新的请假申请" width="500px" align-center class="notify-dialog">
        <div v-if="notifyItem" class="notify-content">
          <div class="notify-info">
            <div class="info-row">
              <span class="label">申请医生：</span>
              <span class="value highlight">{{ nameOfDoctor(notifyItem.doctorId) }}</span>
            </div>
            <div class="info-row">
              <span class="label">请假类型：</span>
              <el-tag size="small" :type="getLeaveTypeTag(notifyItem.leaveType)">{{ notifyItem.leaveType }}</el-tag>
            </div>
            <div class="info-row">
              <span class="label">请假时间：</span>
              <span class="value">{{ notifyItem.date }} {{ notifyItem.timeSlot }}</span>
            </div>
            <div class="info-row">
              <span class="label">起止时间：</span>
              <span class="value">{{ notifyItem.startTime }} ~ {{ notifyItem.endTime }}</span>
            </div>
            <div class="info-row full">
              <span class="label">申请理由：</span>
              <span class="value">{{ notifyItem.reason }}</span>
            </div>
          </div>
          <div v-if="typeof notifyItem.attachments==='string' && /^https?:\/\//.test(notifyItem.attachments)" class="notify-attachment">
            <el-image 
              :src="notifyItem.attachments" 
              :preview-src-list="[notifyItem.attachments]" 
              fit="cover" 
              class="attachment-img"
            />
            <span class="attachment-label">附件凭证</span>
          </div>
        </div>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="notifyVisible=false">稍后处理</el-button>
            <el-button type="primary" @click="goApprovals">立即审批</el-button>
          </div>
        </template>
      </el-dialog>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref, onMounted, provide } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElNotification } from 'element-plus'
import { 
  HomeFilled, User, OfficeBuilding, UserFilled, Timer, Tickets, List, Setting, Tools,
  FirstAidKit, Stamp, Money, Avatar, Lock, Key, ArrowRight, Operation, Grid, CaretBottom
} from '@element-plus/icons-vue'
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

function handleCommand(command) {
  if (command === 'logout') {
    logout()
  } else if (command === 'profile') {
    goProfile()
  }
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
            message: `医生：${nameOfDoctor(notifyItem.value.doctorId)} · 申请审批`,
            type: 'info',
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

function getLeaveTypeTag(type) {
  const map = { '事假': 'info', '病假': 'warning', '调休': 'success' }
  return map[type] || ''
}

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
.app-wrapper {
  height: 100vh;
  background-color: var(--color-bg-page);
}

.app-sidebar {
  background-color: var(--color-bg-sidebar);
  border-right: none;
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
  box-shadow: 2px 0 8px rgba(0,0,0,0.05);
  z-index: 10;
}

.brand {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  color: #fff;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}

.brand-logo {
  color: var(--color-primary);
  margin-right: 12px;
}

.brand-text {
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
  background-color: transparent;
  padding-top: 16px;
}

.menu-group-title {
  padding: 12px 20px 8px;
  font-size: 12px;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 1px;
  font-weight: 600;
}

:deep(.el-menu) {
  background-color: transparent;
  border-right: none;
}

:deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  color: #94a3b8;
  margin: 4px 12px;
  border-radius: 8px;
}

:deep(.el-menu-item:hover) {
  background-color: var(--color-bg-sidebar-hover);
  color: #fff;
}

:deep(.el-menu-item.is-active) {
  background-color: var(--color-primary);
  color: #fff;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

:deep(.el-menu-item .el-icon) {
  font-size: 18px;
  margin-right: 10px;
}

.main-container {
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.app-header {
  height: 64px;
  background-color: #fff;
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.03);
  z-index: 9;
}

.header-left {
  display: flex;
  align-items: center;
}

.page-breadcrumb {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: var(--color-text-secondary);
}

.separator {
  margin: 0 8px;
  color: var(--color-text-placeholder);
}

.breadcrumb-item.active {
  color: var(--color-text-primary);
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.action-item {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  cursor: pointer;
  color: var(--color-text-secondary);
  transition: all 0.2s;
}

.action-item:hover {
  background-color: var(--color-bg-page);
  color: var(--color-primary);
}

.user-profile {
  margin-left: 8px;
}

.avatar-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 20px;
  transition: background-color 0.2s;
}

.avatar-wrapper:hover {
  background-color: var(--color-bg-page);
}

.username {
  font-size: 14px;
  color: var(--color-text-primary);
  font-weight: 500;
}

.app-main {
  background-color: var(--color-bg-page);
  padding: 0;
  overflow-y: auto;
}

.content-wrapper {
  max-width: 1600px;
  margin: 0 auto;
  padding: 24px;
  min-height: calc(100vh - 64px);
}

/* Transitions */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

/* Notification Dialog Styles */
.notify-content {
  display: flex;
  gap: 20px;
}

.notify-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.info-row.full {
  align-items: flex-start;
}

.info-row .label {
  color: var(--color-text-secondary);
  width: 70px;
  flex-shrink: 0;
}

.info-row .value {
  color: var(--color-text-primary);
}

.info-row .value.highlight {
  color: var(--color-primary);
  font-weight: 600;
}

.notify-attachment {
  width: 100px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.attachment-img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  border: 1px solid var(--color-border);
}

.attachment-label {
  font-size: 12px;
  color: var(--color-text-secondary);
}
</style>
