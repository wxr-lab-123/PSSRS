<template>
  <el-container style="height:100vh">
    <el-aside width="200px">
      <div class="brand">医生工作台</div>
      <el-menu :default-active="active" router>
        <el-menu-item index="/doctor">
          <span>我的排班</span>
        </el-menu-item>
        <el-menu-item index="/doctor/registrations">
          <span>我的挂号</span>
        </el-menu-item>
        <el-menu-item index="/doctor/profile">
          <span>个人信息</span>
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
import { ElMessage } from 'element-plus'
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


