<template>
  <el-container class="app-wrapper">
    <el-aside width="240px" class="app-sidebar">
      <div class="brand">
        <el-icon class="brand-logo" :size="24"><FirstAidKit /></el-icon>
        <span class="brand-text">医生工作台</span>
      </div>
      <el-menu 
        :default-active="active" 
        router 
        class="sidebar-menu"
      >
        <div class="menu-group-title">业务管理</div>
        <el-menu-item index="/doctor">
          <el-icon><Timer /></el-icon>
          <span>我的排班</span>
        </el-menu-item>
        <el-menu-item index="/doctor/registrations">
          <el-icon><Tickets /></el-icon>
          <span>我的挂号</span>
        </el-menu-item>
        <el-menu-item index="/doctor/notifications">
          <el-icon><Bell /></el-icon>
          <span>消息通知</span>
        </el-menu-item>
        
        <div class="menu-group-title">个人设置</div>
        <el-menu-item index="/doctor/profile">
          <el-icon><User /></el-icon>
          <span>个人信息</span>
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
                <el-avatar :size="32" :src="auth.user?.image || auth.user?.avatar" :icon="UserFilled" class="user-avatar" />
                <span class="username">{{ auth.user?.username || auth.user?.name || '医生' }}</span>
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
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref, onMounted, provide } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  FirstAidKit, Timer, Tickets, Bell, User, UserFilled, 
  ArrowRight, Operation, Grid, CaretBottom 
} from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const active = computed(() => route.path)
const auth = useAuthStore()

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
  router.push('/doctor/profile')
}
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
</style>

