<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="logo">
          <el-icon :size="40" color="#409eff"><FirstAidKit /></el-icon>
        </div>
        <h2 class="title">患者自助挂号系统</h2>
        <p class="subtitle">后台管理中心</p>
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
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            show-password 
            placeholder="请输入密码" 
            :prefix-icon="Lock"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            class="submit-btn" 
            :loading="loading" 
            @click="onSubmit"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <p>&copy; 2024 PSSRS 智慧医疗</p>
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
  background: linear-gradient(135deg, #1c92d2 0%, #f2fcfe 100%);
  position: relative;
  overflow: hidden;
}

.login-container::before {
  content: '';
  position: absolute;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 10%, transparent 10%),
              radial-gradient(circle, rgba(255,255,255,0.1) 10%, transparent 10%);
  background-size: 50px 50px;
  background-position: 0 0, 25px 25px;
  animation: bgMove 60s linear infinite;
  z-index: 0;
}

@keyframes bgMove {
  0% { transform: translate(0, 0); }
  100% { transform: translate(-50px, -50px); }
}

.login-box {
  width: 420px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  position: relative;
  z-index: 1;
  transition: transform 0.3s ease;
}

.login-box:hover {
  transform: translateY(-5px);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo {
  width: 64px;
  height: 64px;
  background: #ecf5ff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.title {
  font-size: 24px;
  color: #303133;
  margin: 0 0 8px;
  font-weight: 600;
}

.subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.login-form {
  margin-bottom: 24px;
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  border-radius: 8px;
  background: linear-gradient(90deg, #409eff, #3a8ee6);
  border: none;
  transition: all 0.3s;
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

.submit-btn:active {
  transform: translateY(0);
}

.login-footer {
  text-align: center;
  color: #c0c4cc;
  font-size: 12px;
}

/* 输入框样式覆盖 */
:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset !important;
  background-color: #f5f7fa;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset !important;
  background-color: #fff;
}
</style>


