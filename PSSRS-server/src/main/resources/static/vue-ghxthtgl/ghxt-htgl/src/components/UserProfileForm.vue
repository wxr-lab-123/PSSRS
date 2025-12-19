<template>
  <div class="profile-container">
    <el-card class="profile-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon class="header-icon"><User /></el-icon>
            <span class="header-title">个人信息设置</span>
          </div>
        </div>
      </template>
      
      <div class="profile-content">
        <!-- 左侧头像区域 -->
        <div class="avatar-section">
          <div class="avatar-wrapper">
            <el-avatar :size="100" :src="form.avatar" class="profile-avatar">
              <el-icon :size="50" color="#909399"><UserFilled /></el-icon>
            </el-avatar>
            <div class="avatar-overlay" v-if="editMode">
              <el-upload 
                class="avatar-uploader"
                :show-file-list="false" 
                :auto-upload="false" 
                accept="image/*" 
                :on-change="onAvatarChange"
              >
                <el-icon color="#fff" :size="24"><Camera /></el-icon>
              </el-upload>
            </div>
          </div>
          <div class="role-badge" :class="{ 'doctor': isDoctor, 'admin': !isDoctor }">
            {{ isDoctor ? '医生' : '管理员' }}
          </div>
          <div class="user-name-display">{{ form.name || form.username }}</div>
        </div>

        <!-- 右侧信息区域 -->
        <div class="info-section">
          <!-- 查看模式 -->
          <div v-if="!editMode" class="view-mode">
            <el-descriptions :column="2" border size="large" class="custom-descriptions">
              <el-descriptions-item label="用户名">
                <span class="info-text">{{ form.username || '-' }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="昵称">
                <span class="info-text">{{ form.nickname || '-' }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="年龄">
                <span class="info-text">{{ form.age ?? '-' }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="手机号">
                <span class="info-text">{{ form.phone || '-' }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="性别">
                <el-tag :type="form.gender === '0' ? '' : (form.gender === '1' ? 'danger' : 'info')" effect="light" round>
                  {{ genderText }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item v-if="isDoctor" label="职称">
                <el-tag type="warning" effect="plain">{{ form.title || '暂无职称' }}</el-tag>
              </el-descriptions-item>
            </el-descriptions>
            
            <div class="action-bar">
              <el-button :icon="Iphone" @click="phoneDialogVisible = true">绑定手机号</el-button>
              <el-button :icon="Lock" @click="passwordDialogVisible = true">更改密码</el-button>
              <el-button type="primary" :icon="Edit" @click="editMode = true">修改信息</el-button>
            </div>
          </div>

          <!-- 编辑模式 -->
          <div v-else class="edit-mode">
            <el-form :model="form" :rules="rules" ref="basicRef" label-width="80px" size="large">
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="用户名">
                    <el-input v-model="form.username" disabled :prefix-icon="User" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="昵称" prop="nickname">
                    <el-input v-model="form.nickname" placeholder="请输入昵称" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="年龄">
                    <el-input-number v-model="form.age" :min="0" :max="120" style="width: 100%" controls-position="right" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="手机号">
                    <el-input v-model="form.phone" disabled placeholder="请通过绑定功能修改" :prefix-icon="Iphone" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="性别">
                    <el-select v-model="form.gender" style="width:100%" placeholder="请选择性别">
                      <el-option label="男" value="0" />
                      <el-option label="女" value="1" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12" v-if="isDoctor">
                  <el-form-item label="职称">
                    <el-input v-model="form.title" placeholder="请输入职称" />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
            
            <div class="action-bar">
              <el-button @click="cancelEdit">取消</el-button>
              <el-button type="primary" :loading="loading.basic" @click="submitBasic">保存更改</el-button>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 绑定手机号弹窗 -->
    <el-dialog v-model="phoneDialogVisible" title="绑定手机号" width="400px" align-center class="custom-dialog">
      <el-form :model="phoneForm" :rules="phoneRules" ref="phoneRef" label-width="80px" size="large">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="phoneForm.phone" maxlength="11" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div class="code-input-group">
            <el-input v-model="phoneForm.code" maxlength="6" placeholder="验证码" />
            <el-button class="send-btn" :disabled="codeSending || codeCountdown>0" :loading="codeSending" @click="onSendCode">
              {{ codeCountdown>0 ? `${codeCountdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="phoneDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="phoneLoading" @click="submitBindPhone">立即绑定</el-button>
      </template>
    </el-dialog>

    <!-- 更改密码弹窗 -->
    <el-dialog v-model="passwordDialogVisible" title="更改密码" width="400px" align-center class="custom-dialog">
      <el-form :model="pwdForm" :rules="pwdRules" ref="pwdRef" label-width="90px" size="large">
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input type="password" show-password v-model="pwdForm.oldPassword" placeholder="请输入当前密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input type="password" show-password v-model="pwdForm.newPassword" placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirm">
          <el-input type="password" show-password v-model="pwdForm.confirm" placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="passwordLoading" @click="submitPassword">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { storeToRefs } from 'pinia'
import { User, Iphone, Lock, Edit, UserFilled, Camera } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import { getProfile, updateProfile, updateAvatar, bindPhone, changePassword, sendPhoneCode } from '../api/profile'
import { updateDoctor } from '../api/doctors'
import { uploadDoctorAvatar } from '../api/upload'

const editMode = ref(false)
const phoneDialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const auth = useAuthStore()
const { user } = storeToRefs(auth)

const form = reactive({
  avatar: '',
  username: '',
  nickname: '',
  name: '',
  age: undefined,
  gender: undefined,
  phone: '',
  title: ''
})

const loading = reactive({ basic: false })
const phoneLoading = ref(false)
const passwordLoading = ref(false)
const codeCountdown = ref(0)
const codeSending = ref(false)

const rules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
}

const basicRef = ref()
const phoneRef = ref()
const pwdRef = ref()
const phoneForm = reactive({ phone: '', code: '' })
const phoneRules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirm: '' })
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '密码长度至少6位', trigger: 'blur' }],
  confirm: [{ required: true, message: '请再次输入新密码', trigger: 'blur' }]
}

onMounted(async () => {
  try {
    const data = await getProfile()
    {
      const imgRaw = data?.avatar || data?.image || user.value?.avatar || ''
      form.avatar = typeof imgRaw === 'string' ? imgRaw.trim().replace(/^[`'"]+|[`'"]+$/g, '') : ''
    }
    form.username = data?.username || user.value?.username || ''
    form.name = data?.name || user.value?.name || ''
    form.nickname = data?.nickname || form.name || user.value?.nickname || user.value?.username || ''
    form.age = data?.age ?? user.value?.age
    const g = (data?.gender ?? data?.sex ?? user.value?.gender)
    form.gender = (g === 'male' || g === 0 || g === '0') ? '0' : (g === 'female' || g === 1 || g === '1') ? '1' : undefined
    form.phone = data?.phone ?? user.value?.phone
    form.title = data?.title ?? user.value?.title ?? data?.jobTitle ?? user.value?.jobTitle
    phoneForm.phone = form.phone || ''
    phoneForm.code = ''
  } catch {}
})

watch(phoneDialogVisible, (v) => {
  if (!v) {
    codeCountdown.value = 0
    codeSending.value = false
    if (codeTimer) { clearInterval(codeTimer); codeTimer = null }
  }
})

const isAdmin = computed(() => auth.isAdmin)
const isDoctor = computed(() => auth.isDoctor)
const canUpdateDoctor = computed(() => isDoctor.value && typeof auth.hasPerm === 'function' ? auth.hasPerm('doctors:update') : false)
const genderText = computed(() => {
  if (form.gender === '0' || form.gender === 0) return '男'
  if (form.gender === '1' || form.gender === 1) return '女'
  return '-'
})

function onAvatarChange(file) {
  const raw = file?.raw || file
  if (!raw) return
  uploadDoctorAvatar(raw)
    .then(async (url) => {
      const normalized = typeof url === 'string' ? url.trim().replace(/^[`'\"]+|[`'\"]+$/g, '') : ''
      try {
        if (canUpdateDoctor.value && user.value?.id) {
          await updateDoctor(user.value.id, { image: normalized })
        } else {
          await updateAvatar(normalized)
        }
      } catch (e) {
        try { await updateAvatar(normalized) } catch {}
      }
      form.avatar = normalized
      try {
        const updated = await getProfile()
        auth.setUser({ ...(user.value || {}), avatar: normalized, nickname: updated?.nickname || form.nickname })
      } catch {}
      ElMessage.success('头像已更新')
    })
    .catch((e) => { ElMessage.error(e?.msg || '头像更新失败') })
}

function submitBasic() {
  if (!basicRef.value) return
  basicRef.value.validate(async (ok) => {
    if (!ok) return
    loading.basic = true
    try {
      await updateProfile({ nickname: form.nickname, age: form.age, gender: form.gender, title: form.title })
      const updated = await getProfile()
      auth.setUser({ ...(user.value || {}), nickname: updated?.nickname || form.nickname, avatar: updated?.avatar || form.avatar, age: updated?.age ?? form.age, gender: updated?.gender ?? form.gender, title: updated?.title ?? form.title })
      editMode.value = false
      ElMessage.success('保存成功')
    } finally {
      loading.basic = false
    }
  })
}

async function cancelEdit() {
  try {
    const data = await getProfile()
    form.avatar = data?.avatar || form.avatar
    form.nickname = data?.nickname ?? form.nickname
    form.age = data?.age ?? form.age
    const g = data?.gender ?? form.gender
    form.gender = (g === 'male' || g === 0 || g === '0') ? '0' : (g === 'female' || g === 1 || g === '1') ? '1' : undefined
    form.phone = data?.phone ?? form.phone
    form.title = data?.title ?? form.title
  } catch {}
  editMode.value = false
}

function submitBindPhone() {
  if (!phoneRef.value) return
  phoneRef.value.validate(async (ok) => {
    if (!ok) return
    phoneLoading.value = true
    try {
      await bindPhone({ phone: phoneForm.phone, code: phoneForm.code })
      form.phone = phoneForm.phone
      phoneDialogVisible.value = false
      ElMessage.success('绑定成功')
    } finally {
      phoneLoading.value = false
    }
  })
}

let codeTimer = null
async function onSendCode() {
  if (codeSending.value || codeCountdown.value > 0) return
  if (!phoneForm.phone) { ElMessage.error('请输入手机号'); return }
  codeSending.value = true
  try {
    await sendPhoneCode(phoneForm.phone)
    ElMessage.success('验证码已发送')
    codeCountdown.value = 60
    codeTimer = setInterval(() => {
      codeCountdown.value -= 1
      if (codeCountdown.value <= 0) { clearInterval(codeTimer); codeTimer = null }
    }, 1000)
  } finally {
    codeSending.value = false
  }
}

function submitPassword() {
  if (!pwdRef.value) return
  if (pwdForm.newPassword !== pwdForm.confirm) {
    ElMessage.error('两次输入的新密码不一致')
    return
  }
  pwdRef.value.validate(async (ok) => {
    if (!ok) return
    passwordLoading.value = true
    try {
      await changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
      passwordDialogVisible.value = false
      pwdForm.oldPassword = ''
      pwdForm.newPassword = ''
      pwdForm.confirm = ''
      ElMessage.success('密码已更新')
    } finally {
      passwordLoading.value = false
    }
  })
}

onBeforeUnmount(() => { if (codeTimer) { clearInterval(codeTimer); codeTimer = null } })
</script>

<style scoped>
.profile-container {
  max-width: 1000px;
  margin: 20px auto;
  padding: 0 16px;
}

.profile-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 16px rgba(0,0,0,0.06);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  font-size: 20px;
  color: #409eff;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.profile-content {
  display: flex;
  gap: 40px;
  padding: 20px 0;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 180px;
  flex-shrink: 0;
  border-right: 1px solid #ebeef5;
  padding-right: 40px;
}

.avatar-wrapper {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  margin-bottom: 16px;
}

.profile-avatar {
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.role-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  margin-bottom: 8px;
}

.role-badge.doctor {
  background: #ecf5ff;
  color: #409eff;
}

.role-badge.admin {
  background: #fdf6ec;
  color: #e6a23c;
}

.user-name-display {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  text-align: center;
}

.info-section {
  flex: 1;
}

.info-text {
  color: #606266;
  font-size: 14px;
}

.action-bar {
  margin-top: 32px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  border-top: 1px solid #ebeef5;
  padding-top: 20px;
}

.code-input-group {
  display: flex;
  gap: 12px;
}

.send-btn {
  width: 110px;
}

:deep(.custom-descriptions .el-descriptions__label) {
  width: 100px;
  font-weight: 500;
}

:deep(.custom-descriptions .el-descriptions__content) {
  font-size: 15px;
}
</style>
