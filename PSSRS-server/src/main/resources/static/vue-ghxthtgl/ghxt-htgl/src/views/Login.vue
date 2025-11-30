<template>
  <div class="login-page">
    <div class="content">
      <el-card class="login-card">
      <h2>后台登录</h2>
      <el-form :model="form" @keyup.enter="onSubmit">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="admin" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" show-password placeholder="123456" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onSubmit">登录</el-button>
        </el-form-item>
      </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import { loginApi, meApi } from '../api/auth'

const router = useRouter()
const auth = useAuthStore()
const form = reactive({ username: '', password: '' })
const loading = ref(false)

async function onSubmit() {
  if (loading.value) return
  loading.value = true
  try {
    const data = await loginApi({ username: form.username, password: form.password })
    console.log('登录返回数据:', data)
    console.log('user 对象完整内容:', data.user)
    if (!data || !data.token) {
      ElMessage.error('登录失败：返回数据格式错误')
      return
    }
    // 尝试多种可能的 roles 字段位置
    let roles = []
    if (data.user?.roles && Array.isArray(data.user.roles)) {
      roles = data.user.roles
    } else if (data.user?.roleList && Array.isArray(data.user.roleList)) {
      roles = data.user.roleList
    } else if (data.roles && Array.isArray(data.roles)) {
      roles = data.roles
    } else {
      // 尝试从 JWT token 中解析 roles（如果后端把 roles 放在 token 里）
      try {
        const payload = JSON.parse(atob(data.token.split('.')[1]))
        if (payload.roles && Array.isArray(payload.roles)) {
          roles = payload.roles
          console.log('从 token 解析出角色:', roles)
        }
      } catch (e) {
        console.warn('无法从 token 解析角色:', e)
      }
    }
    console.log('解析出的用户角色:', roles)
    
    auth.setToken(data.token)
    try {
      const me = await meApi()
      const perms = Array.isArray(me?.permissions) ? me.permissions : []
      auth.setUser({ ...data.user, roles, permissions: perms })
    } catch (e) {
      console.warn('获取用户权限失败，后续导航将仅基于角色', e)
      auth.setUser({ ...data.user, roles })
    }
    ElMessage.success('登录成功')
    await new Promise(resolve => setTimeout(resolve, 100))
    if (roles.length > 1) {
      router.replace('/role-select').catch(() => {})
    } else if (roles.length === 1) {
      const role = roles[0]
      auth.setActiveRole(role)
      if (role === 'ADMIN') router.replace('/admin').catch(() => {})
      else if (role === 'DOCTOR') router.replace('/doctor').catch(() => {})
      else router.replace('/').catch(() => {})
    } else {
      ElMessage.warning('未分配角色，默认进入首页')
      router.replace('/').catch(() => {})
    }
  } catch (error) {
    console.error('登录错误:', error)
    ElMessage.error(error?.msg || error?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa
}
.login-card { width: 360px }
h2 { text-align: center; margin-bottom: 16px }
.content { max-width: 1200px; margin: 0 auto; padding: 16px; width: 100% }
</style>


