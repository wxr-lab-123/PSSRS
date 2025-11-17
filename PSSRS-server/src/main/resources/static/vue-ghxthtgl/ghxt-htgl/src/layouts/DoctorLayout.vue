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
        <div class="content">
          <router-view />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const active = computed(() => route.path)
const auth = useAuthStore()

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
.content { max-width: 1200px; margin: 0 auto; padding: 16px; width: 100% }
</style>


