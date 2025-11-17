<template>
  <el-card class="profile-card">
    <template #header>个人信息设置</template>
    <el-tabs v-model="active">
      <el-tab-pane label="头像昵称" name="avatar">
        <div class="avatar-row">
          <el-avatar :size="96" :src="form.avatar" />
          <div class="avatar-actions">
            <el-upload :show-file-list="false" :auto-upload="false" :before-upload="handleAvatarUpload">
              <el-button type="primary">上传新头像</el-button>
            </el-upload>
          </div>
        </div>
        <el-form :model="form" :rules="rules" ref="basicRef" label-width="120px" class="mt16">
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="form.nickname" placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading.basic" @click="submitBasic">保存</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
      <el-tab-pane label="绑定手机号" name="phone">
        <el-form :model="phoneForm" :rules="phoneRules" ref="phoneRef" label-width="120px">
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="phoneForm.phone" placeholder="请输入手机号" maxlength="11" />
          </el-form-item>
          <el-form-item label="验证码" prop="code">
            <el-input v-model="phoneForm.code" placeholder="请输入验证码" maxlength="6" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading.phone" @click="submitPhone">绑定</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
      <el-tab-pane label="更改密码" name="password">
        <el-form :model="pwdForm" :rules="pwdRules" ref="pwdRef" label-width="120px">
          <el-form-item label="当前密码" prop="oldPassword">
            <el-input type="password" show-password v-model="pwdForm.oldPassword" placeholder="请输入当前密码" />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input type="password" show-password v-model="pwdForm.newPassword" placeholder="请输入新密码" />
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirm">
            <el-input type="password" show-password v-model="pwdForm.confirm" placeholder="请再次输入新密码" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading.password" @click="submitPassword">保存</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '../stores/auth'
import { getProfile, updateProfile, changePassword, bindPhone, updateAvatar } from '../api/profile'
import { uploadDoctorAvatar } from '../api/upload'

const active = ref('avatar')
const auth = useAuthStore()
const { user } = storeToRefs(auth)

const form = reactive({
  avatar: '',
  nickname: ''
})

const phoneForm = reactive({
  phone: '',
  code: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirm: ''
})

// Removed profile form

const loading = reactive({ basic: false, phone: false, password: false })

const rules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
}

const phoneRules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirm: [{ required: true, message: '请再次输入新密码', trigger: 'blur' }]
}

const basicRef = ref()
const phoneRef = ref()
const pwdRef = ref()

onMounted(async () => {
  try {
    const data = await getProfile()
    form.avatar = data?.avatar || user.value?.avatar || ''
    form.nickname = data?.nickname || user.value?.nickname || user.value?.username || ''
    phoneForm.phone = data?.phone || ''
  } catch {}
})

function handleAvatarUpload(file) {
  uploadDoctorAvatar(file)
    .then(async (url) => {
      await updateAvatar(url)
      form.avatar = url
      const updated = await getProfile()
      auth.setUser({ ...(user.value || {}), avatar: url, nickname: updated?.nickname || form.nickname })
      ElMessage.success('头像已更新')
    })
    .catch(() => {})
  return false
}

function submitBasic() {
  basicRef.value.validate(async (ok) => {
    if (!ok) return
    loading.basic = true
    try {
      await updateProfile({ nickname: form.nickname })
      const updated = await getProfile()
      auth.setUser({ ...(user.value || {}), nickname: updated?.nickname || form.nickname, avatar: updated?.avatar || form.avatar })
      ElMessage.success('保存成功')
    } finally {
      loading.basic = false
    }
  })
}

function submitPhone() {
  phoneRef.value.validate(async (ok) => {
    if (!ok) return
    loading.phone = true
    try {
      await bindPhone({ phone: phoneForm.phone, code: phoneForm.code })
      ElMessage.success('绑定成功')
    } finally {
      loading.phone = false
    }
  })
}

function submitPassword() {
  if (pwdForm.newPassword !== pwdForm.confirm) {
    ElMessage.error('两次输入的新密码不一致')
    return
  }
  pwdRef.value.validate(async (ok) => {
    if (!ok) return
    loading.password = true
    try {
      await changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
      ElMessage.success('密码已更新')
      pwdForm.oldPassword = ''
      pwdForm.newPassword = ''
      pwdForm.confirm = ''
    } finally {
      loading.password = false
    }
  })
}

// Removed submitProfile
</script>

<style scoped>
.profile-card { width: 800px; margin: 0 auto; }
.avatar-row { display: flex; align-items: center; gap: 16px }
.avatar-actions { display: flex; align-items: center; gap: 8px }
.mt16 { margin-top: 16px }
</style>
