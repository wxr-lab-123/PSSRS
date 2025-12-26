<template>
  <div class="login-container">
    <div class="login-content">
      <div class="brand-section">
        <div class="brand-logo-wrapper">
          <el-icon :size="48" color="#fff"><FirstAidKit /></el-icon>
        </div>
        <h1 class="brand-title">PSSRS 智慧医疗</h1>
        <p class="brand-desc">高效 · 专业 · 便捷的医疗服务管理平台</p>
      </div>

      <div class="login-box">
        <div class="login-header">
          <h2 class="title">欢迎登录</h2>
          <p class="subtitle">请使用管理员或医生账号登录系统</p>
        </div>
        
        <el-form 
          ref="formRef"
          :model="form" 
          :rules="rules"
          class="login-form"
          @keyup.enter="onSubmit"
          size="large"
        >
          <el-form-item prop="username">
            <el-input 
              v-model="form.username" 
              placeholder="请输入用户名" 
              :prefix-icon="User"
              class="custom-input"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input 
              v-model="form.password" 
              type="password" 
              show-password 
              placeholder="请输入密码" 
              :prefix-icon="Lock"
              class="custom-input"
            />
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              class="submit-btn" 
              :loading="loading" 
              @click="onSubmit"
              auto-insert-space
            >
              登 录
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="login-footer">
          <p>&copy; 2024 PSSRS Smart Healthcare System</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, FirstAidKit } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import { loginApi, meApi } from '../api/auth'

const router = useRouter()
const auth = useAuthStore()
const formRef = ref()
const form = reactive({ username: '', password: '' })
const loading = ref(false)

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function onSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const data = await loginApi({ username: form.username, password: form.password })
        if (!data || !data.token) {
          throw new Error('登录失败：返回数据格式错误')
        }
        
        // 角色解析逻辑
        let roles = []
        if (data.user?.roles && Array.isArray(data.user.roles)) {
          roles = data.user.roles
        } else if (data.user?.roleList && Array.isArray(data.user.roleList)) {
          roles = data.user.roleList
        } else if (data.roles && Array.isArray(data.roles)) {
          roles = data.roles
        } else {
          try {
            const payload = JSON.parse(atob(data.token.split('.')[1]))
            if (payload.roles && Array.isArray(payload.roles)) {
              roles = payload.roles
            }
          } catch (e) {
            console.warn('Token解析失败', e)
          }
        }
        
        auth.setToken(data.token)
        try {
          const me = await meApi()
          const perms = Array.isArray(me?.permissions) ? me.permissions : []
          auth.setUser({ ...data.user, roles, permissions: perms })
        } catch (e) {
          auth.setUser({ ...data.user, roles })
        }
        
        ElMessage.success('登录成功')
        
        // 路由跳转逻辑
        if (roles.length > 1) {
          router.replace('/role-select')
        } else if (roles.length === 1) {
          const role = roles[0]
          auth.setActiveRole(role)
          if (role === 'ADMIN') router.replace('/admin')
          else if (role === 'DOCTOR') router.replace('/doctor')
          else router.replace('/')
        } else {
          router.replace('/')
        }
      } catch (error) {
        console.error('登录错误:', error)
        ElMessage.error(error?.msg || error?.message || '登录失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f0f2f5;
  background-image: 
    radial-gradient(at 0% 0%, hsla(253,16%,7%,1) 0, transparent 50%), 
    radial-gradient(at 50% 0%, hsla(225,39%,30%,1) 0, transparent 50%), 
    radial-gradient(at 100% 0%, hsla(339,49%,30%,1) 0, transparent 50%);
  background-size: cover;
  position: relative;
  overflow: hidden;
}

.login-content {
  display: flex;
  width: 900px;
  height: 500px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  backdrop-filter: blur(20px);
}

.brand-section {
  flex: 1;
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: white;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.brand-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI0MCIgaGVpZ2h0PSI0MCIgdmlld0JveD0iMCAwIDQwIDQwIj48ZyBmaWxsLXJ1bGU9ImV2ZW5vZGQiPjxnIGZpbGw9IiNmZmYiIGZpbGwtb3BhY2l0eT0iMC4wNSI+PHBhdGggZD0iTTAgMzhoNDB2MmgtNDB6Ii8+PHBhdGggZD0iTTAgMTloNDB2MmgtNDB6Ii8+PHBhdGggZD0iTTAgMGg0MHYyaC00MHoiLz48cGF0aCBkPSJNMCA1OGg0MHYyaC00MHoiLz48L2c+PC9nPjwvc3ZnPg==');
}

.brand-logo-wrapper {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.brand-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 12px;
  letter-spacing: 1px;
}

.brand-desc {
  font-size: 16px;
  opacity: 0.9;
  font-weight: 300;
  max-width: 240px;
  line-height: 1.6;
}

.login-box {
  flex: 1;
  padding: 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: #ffffff;
}

.login-header {
  margin-bottom: 40px;
}

.title {
  font-size: 28px;
  color: #1e293b;
  margin: 0 0 8px;
  font-weight: 700;
}

.subtitle {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.login-form {
  margin-bottom: 24px;
}

.custom-input :deep(.el-input__wrapper) {
  padding: 8px 12px;
  background-color: #f8fafc;
  box-shadow: 0 0 0 1px #e2e8f0 inset !important;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  background-color: #fff;
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.2), 0 0 0 1px #2563eb inset !important;
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 8px;
  background-color: #2563eb;
  border: none;
  font-weight: 600;
  margin-top: 12px;
}

.submit-btn:hover {
  background-color: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.login-footer {
  margin-top: auto;
  text-align: center;
  color: #94a3b8;
  font-size: 12px;
}

@media (max-width: 960px) {
  .login-content {
    width: 90%;
    height: auto;
    flex-direction: column;
  }
  
  .brand-section {
    padding: 32px;
  }
  
  .login-box {
    padding: 32px;
  }
}
</style>


