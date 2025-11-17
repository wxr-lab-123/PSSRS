<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <div>管理员管理</div>
        <el-space>
          <el-input v-model="query.username" placeholder="用户名" clearable style="width:160px" />
          <el-select v-model="query.status" placeholder="状态" clearable style="width:140px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
          <el-button type="primary" @click="onAdd">新增管理员</el-button>
        </el-space>
      </div>
    </template>
    <el-table :data="rows" :loading="loading" style="width:100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="姓名" />
      <el-table-column prop="roles" label="角色">
        <template #default="{ row }">{{ (row.roles || []).join(', ') }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="(row.status === 1 || row.status === '1' || row.status === 'ACTIVE') ? 'success' : 'info'">
            {{ (row.status === 1 || row.status === '1' || row.status === 'ACTIVE') ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button type="primary" link @click="onEdit(row)">编辑</el-button>
          <el-button type="danger" link @click="onDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div style="display:flex;justify-content:flex-end;margin-top:12px">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :page-sizes="[10,20,50,100]"
        @current-change="fetchList"
        @size-change="fetchList"
      />
    </div>

    <el-dialog v-model="createVisible" title="新增管理员" width="520px">
      <el-form ref="createFormRef" :model="createForm" :rules="rules" label-width="88px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="createForm.username" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="createForm.name" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="createForm.password" type="password" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="createForm.phone" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="createForm.gender" style="width: 160px">
            <el-option label="男" value="0" />
            <el-option label="女" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色" prop="roles">
          <el-checkbox-group v-model="createForm.roles">
            <el-checkbox v-for="r in roleOptions" :key="r" :label="r">{{ r }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createVisible = false">取消</el-button>
          <el-button type="primary" :loading="createSubmitting" @click="onCreateSubmit">确 定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="editVisible" title="编辑管理员" width="520px">
      <el-form ref="editFormRef" :model="editForm" :rules="rulesEdit" label-width="88px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="editForm.gender" style="width: 160px">
            <el-option label="男" value="0" />
            <el-option label="女" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色" prop="roles">
          <el-checkbox-group v-model="editForm.roles">
            <el-checkbox v-for="r in roleOptions" :key="r" :label="r">{{ r }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="editForm.status">
            <el-radio label="ACTIVE">启用</el-radio>
            <el-radio label="DISABLED">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editVisible = false">取消</el-button>
          <el-button type="primary" :loading="editSubmitting" @click="onEditSubmit">保 存</el-button>
        </span>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchAdmins, createAdmin, updateAdmin, deleteAdmin } from '../api/admins'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const roleOptions = ref([])
const query = reactive({ page: 1, size: 10, username: '', status: '' })

const phonePattern = /^(?:(?:\+?86)?1\d{10})$/

const createVisible = ref(false)
const createSubmitting = ref(false)
const createFormRef = ref()
const createForm = reactive({ username: '', password: '', name: '', phone: '', gender: '0', roles: [] })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  phone: [{ pattern: phonePattern, message: '手机号格式不正确', trigger: ['blur', 'change'] }],
  roles: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const editVisible = ref(false)
const editSubmitting = ref(false)
const editFormRef = ref()
const editForm = reactive({ id: undefined, name: '', phone: '', gender: '0', roles: [], status: 1 })
const rulesEdit = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  roles: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

async function fetchList() {
  loading.value = true
  try {
    const data = await fetchAdmins({ ...query })
    rows.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error(e?.msg || e?.message || '获取列表失败')
  } finally {
    loading.value = false
  }
}

async function fetchRoleOptions() {
  // Use static role options; do not call /api/admin/roles
  roleOptions.value = ['ADMIN', 'DOCTOR']
}

function onSearch() { query.page = 1; fetchList() }
function onReset() { Object.assign(query, { page: 1, size: 10, username: '', status: '' }); fetchList() }
function onAdd() { Object.assign(createForm, { username: '', password: '', name: '', phone: '', gender: '0', roles: [] }); createVisible.value = true }

function onCreateSubmit() {
  if (!createFormRef.value) return
  createFormRef.value.validate(async (valid) => {
    if (!valid) return
    createSubmitting.value = true
    try {
      await createAdmin({ ...createForm, status: 1 })
      ElMessage.success('新增成功')
      createVisible.value = false
      fetchList()
    } catch (e) { ElMessage.error(e?.msg || e?.message || '新增失败') }
    finally { createSubmitting.value = false }
  })
}

function normalizeStatus(v) {
  if (v === 1 || v === '1' || v === 'ACTIVE') return 1
  if (v === 0 || v === '0' || v === 'DISABLED') return 0
  return 1
}

function onEdit(row) {
  Object.assign(editForm, { id: row.id, name: row.name, phone: row.phone, gender: row.gender || '0', roles: row.roles || [], status: normalizeStatus(row.status) })
  editVisible.value = true
}

function onEditSubmit() {
  if (!editFormRef.value) return
  editFormRef.value.validate(async (valid) => {
    if (!valid) return
    editSubmitting.value = true
    try {
      const { id, ...payload } = editForm
      payload.status = Number(payload.status)
      await updateAdmin(id, payload)
      ElMessage.success('保存成功')
      editVisible.value = false
      
      // 直接更新列表中的对应项，使用表单数据
      const index = rows.value.findIndex(r => r.id === id)
      if (index !== -1) {
        rows.value[index] = {
          ...rows.value[index],
          name: payload.name,
          phone: payload.phone,
          gender: payload.gender,
          roles: payload.roles,
          status: payload.status
        }
      } else {
        fetchList()
      }
    } catch (e) { 
      ElMessage.error(e?.msg || e?.message || '保存失败')
      fetchList() // 出错时重新获取列表
    }
    finally { editSubmitting.value = false }
  })
}

function onDelete(row) {
  ElMessageBox.confirm(`确认删除管理员「${row.username}」吗？`, '提示', { type: 'warning' })
    .then(async () => { try { await deleteAdmin(row.id); ElMessage.success('删除成功'); fetchList() } catch (e) { ElMessage.error(e?.msg || e?.message || '删除失败') } })
}

onMounted(() => { fetchRoleOptions(); fetchList() })
</script>

<style scoped>
</style>


