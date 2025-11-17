<template>
  <el-dialog v-model="visible" title="选择角色" width="380px" :close-on-click-modal="false" :show-close="false">
    <el-form label-width="0">
      <el-form-item>
        <el-radio-group v-model="selected">
          <el-radio v-for="r in roles" :key="r" :label="r">{{ r }}</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <span>
        <el-button type="primary" :disabled="!selected" @click="confirm">进入</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const roles = computed(() => auth.roles)
const visible = ref(true)
const selected = ref('')

function goHomeByRole(role) {
  if (role === 'ADMIN') return '/admin'
  if (role === 'DOCTOR') return '/doctor'
  return '/'
}

function confirm() {
  if (!selected.value) return
  auth.setActiveRole(selected.value)
  const path = goHomeByRole(selected.value)
  router.replace(path).catch(() => {})
}

onMounted(() => {
  if (roles.value.length === 1) {
    selected.value = roles.value[0]
  }
})
</script>
