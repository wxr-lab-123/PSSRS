<template>
  <el-container style="height:100vh">
    <el-aside width="220px">
      <div class="brand">患者自助挂号 - 后台</div>
      <el-menu :default-active="active" router>
        <el-menu-item index="/admin/admins">
          <el-icon><User /></el-icon>
          <span>管理员管理</span>
        </el-menu-item>
        <el-menu-item index="/admin">
          <el-icon><HomeFilled /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/admin/patients">
          <el-icon><User /></el-icon>
          <span>患者管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/departments">
          <el-icon><OfficeBuilding /></el-icon>
          <span>科室管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/doctors">
          <el-icon><UserFilled /></el-icon>
          <span>医生管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/schedules">
          <el-icon><Timer /></el-icon>
          <span>排班管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/registrations">
          <el-icon><Tickets /></el-icon>
          <span>挂号管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/orders">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/roles">
          <el-icon><Setting /></el-icon>
          <span>角色权限</span>
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
import { HomeFilled, User, OfficeBuilding, UserFilled, Timer, Tickets, List, Setting, Tools } from '@element-plus/icons-vue'
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
  router.push('/admin/settings')
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
