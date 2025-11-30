<template>
  <el-card>
    <template #header>角色权限</template>
    <div v-if="loading" style="padding:24px"><el-skeleton :rows="4" animated /></div>
    <div v-else>
      <el-row :gutter="16">
        <el-col :span="6">
          <el-card shadow="never">
            <div style="margin-bottom:8px; font-weight:500">角色</div>
            <el-radio-group v-model="activeRoleId" size="small">
              <el-radio-button v-for="r in roles" :key="r.id" :label="r.id">{{ r.name }}</el-radio-button>
            </el-radio-group>
            <div style="margin-top:12px">
              <el-button size="small" type="primary" :disabled="!hasPerm('roles:assign')" @click="save">保存</el-button>
              <el-button size="small" @click="selectAll(true)">全选</el-button>
              <el-button size="small" @click="selectAll(false)">清空</el-button>
              <el-button size="small" :disabled="!hasPerm('roles:assign')" @click="syncToUser">同步到当前用户</el-button>
            </div>
          </el-card>
        </el-col>
        <el-col :span="18">
          <el-table :data="modules" style="width:100%">
            <el-table-column prop="name" label="权限模块" width="160" />
            <el-table-column label="权限项">
              <template #default="{ row }">
                <el-space wrap>
                  <el-checkbox
                    v-for="a in row.actions"
                    :key="a"
                    :label="labelOf(a)"
                    :model-value="isChecked(row.key, a)"
                    @update:modelValue="val => setChecked(row.key, a, val)"
                  />
                </el-space>
              </template>
            </el-table-column>
            <el-table-column label="快捷">
              <template #default="{ row }">
                <el-space>
                  <el-button size="small" @click="toggleModule(row, true)">全选模块</el-button>
                  <el-button size="small" @click="toggleModule(row, false)">清空模块</el-button>
                </el-space>
              </template>
            </el-table-column>
          </el-table>
        </el-col>
      </el-row>
    </div>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchRolesPage, assignRolePermissions } from '../api/roles'
import { fetchPermissions } from '../api/permissions'
import { useAuthStore } from '../stores/auth'

const loading = ref(false)
const auth = useAuthStore()
const hasPerm = (code) => auth.hasPerm(code)
const roles = ref([])
const activeRoleId = ref(null)
const rolePerms = ref({})
const codeToId = ref({})

const modules = ref([])
function codeOf(p) { return p?.code ?? p?.permCode ?? p?.permissionCode ?? p?.key ?? p?.name ?? p?.permission }
function buildModules(list) {
  const map = {}
  list.forEach(p => {
    const code = codeOf(p)
    const grp = p?.group || (code && code.split(':')[0]) || 'misc'
    const act = code && code.includes(':') ? code.split(':')[1] : 'view'
    const name = p?.group || grp
    if (!map[grp]) map[grp] = { key: grp, name, actions: [] }
    if (act && !map[grp].actions.includes(act)) map[grp].actions.push(act)
  })
  modules.value = Object.values(map)
}

function labelOf(a) {
  if (a === 'view') return '查看'
  if (a === 'create') return '新增'
  if (a === 'update') return '编辑'
  if (a === 'delete') return '删除'
  return a
}

function k(m, a) { return `${m}:${a}` }

function ensureRole(role) {
  if (!rolePerms.value[role]) rolePerms.value[role] = []
}

function isChecked(m, a) {
  const role = activeRoleId.value
  if (!role) return false
  const arr = rolePerms.value[role] || []
  return arr.includes(k(m, a))
}

function setChecked(m, a, val) {
  const role = activeRoleId.value
  if (!role) return
  ensureRole(role)
  const arr = rolePerms.value[role]
  const key = k(m, a)
  if (val) {
    if (!arr.includes(key)) arr.push(key)
  } else {
    rolePerms.value[role] = arr.filter(i => i !== key)
  }
}

function toggleModule(row, val) {
  const role = activeRoleId.value
  if (!role) return
  ensureRole(role)
  const arr = rolePerms.value[role]
  const keys = row.actions.map(a => k(row.key, a))
  if (val) {
    keys.forEach(key => { if (!arr.includes(key)) arr.push(key) })
  } else {
    rolePerms.value[role] = arr.filter(i => !keys.includes(i))
  }
}

function selectAll(val) {
  const role = activeRoleId.value
  if (!role) return
  ensureRole(role)
  const keys = modules.value.flatMap(m => m.actions.map(a => k(m.key, a)))
  if (val) {
    rolePerms.value[role] = Array.from(new Set([...(rolePerms.value[role] || []), ...keys]))
  } else {
    rolePerms.value[role] = []
  }
}

async function save() {
  const roleId = activeRoleId.value
  if (!roleId) return
  const codes = rolePerms.value[roleId] || []
  const ids = codes.map(c => codeToId.value[c]).filter(Boolean)
  try {
    await assignRolePermissions(roleId, ids, [])
    ElMessage.success('已保存到后端')
  } catch (e) {
    ElMessage.error(e?.msg || e?.message || '保存失败')
  }
}

function syncToUser() {
  const role = activeRoleId.value
  if (!role) return
  const raw = localStorage.getItem('auth_user')
  let user = null
  try { user = JSON.parse(raw || 'null') } catch { user = null }
  if (!user) return
  const perms = rolePerms.value[role] || []
  user.permissions = perms
  localStorage.setItem('auth_user', JSON.stringify(user))
  ElMessage.success('已同步到当前用户')
}

function defaultPerms(list) {
  const all = modules.value.flatMap(m => m.actions.map(a => k(m.key, a)))
  const data = {}
  list.forEach(r => { data[r.id] = r.name === 'ADMIN' ? all : ['schedules:view','registrations:view','orders:view'] })
  return data
}

async function init() {
  loading.value = true
  try {
    const page = await fetchRolesPage(1, 100)
    const records = Array.isArray(page?.records) ? page.records : []
    roles.value = records.map(r => ({ id: r.id, name: r.roleName || r.name || r.role }))
    if (!roles.value.length) roles.value = [{ id: 1, name: 'ADMIN' }, { id: 2, name: 'DOCTOR' }]
  } catch {
    roles.value = [{ id: 1, name: 'ADMIN' }, { id: 2, name: 'DOCTOR' }]
  }
  try {
    const permPage = await fetchPermissions({ page: 1, size: 1000 })
    const list = Array.isArray(permPage?.records) ? permPage.records : []
    const map = {}
    const getCode = (p) => p?.code ?? p?.permCode ?? p?.permissionCode ?? p?.key ?? p?.name ?? p?.permission
    const getId = (p) => p?.id ?? p?.permissionId ?? p?.permId
    list.forEach(p => {
      const code = getCode(p)
      const id = getId(p)
      if (code && id != null) map[code] = id
    })
    codeToId.value = map
    buildModules(list)
  } catch {}
  const saved = localStorage.getItem('auth_role_permissions')
  let mapSaved = null
  try { mapSaved = JSON.parse(saved || 'null') } catch { mapSaved = null }
  rolePerms.value = mapSaved && typeof mapSaved === 'object' ? mapSaved : defaultPerms(roles.value)
  activeRoleId.value = roles.value[0]?.id || null
  loading.value = false
}

onMounted(init)
</script>

<style scoped>
.el-radio-group { display: flex; flex-wrap: wrap; gap: 8px }
</style>
