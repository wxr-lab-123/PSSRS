<template>
  <el-card>
    <template #header>权限管理</template>
    <div class="toolbar">
      <el-input v-model="query.code" placeholder="权限码" style="width:220px" clearable />
      <el-button type="primary" @click="onSearch">查询</el-button>
      <el-button @click="onReset">重置</el-button>
      <el-button type="primary" :disabled="!hasPerm('permissions:create')" @click="onAdd">新增</el-button>
    </div>

    <el-table :data="rows" v-loading="loading" style="width:100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="code" label="权限码" min-width="220" />
      <el-table-column prop="name" label="名称" min-width="180" />
      <el-table-column prop="remark" label="备注" min-width="220" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" :disabled="!hasPerm('permissions:update')" @click="onEdit(row)">编辑</el-button>
          <el-popconfirm title="确定删除该权限吗？" @confirm="onDelete(row)">
            <template #reference>
              <el-button size="small" type="danger" :disabled="!hasPerm('permissions:delete')">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        layout="prev, pager, next, jumper, sizes, total"
        @current-change="fetchList"
        @size-change="fetchList"
      />
    </div>

    <el-dialog v-model="createVisible" title="新增权限" width="520px">
      <el-form ref="createFormRef" :model="createForm" :rules="rules" label-width="88px">
        <el-form-item label="权限码" prop="code"><el-input v-model="createForm.code" /></el-form-item>
        <el-form-item label="名称" prop="name"><el-input v-model="createForm.name" /></el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="createForm.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible=false">取消</el-button>
        <el-button type="primary" :loading="createSubmitting" @click="onCreateSubmit">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editVisible" title="编辑权限" width="520px">
      <el-form ref="editFormRef" :model="editForm" :rules="rules" label-width="88px">
        <el-form-item label="权限码" prop="code"><el-input v-model="editForm.code" /></el-form-item>
        <el-form-item label="名称" prop="name"><el-input v-model="editForm.name" /></el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="editForm.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible=false">取消</el-button>
        <el-button type="primary" :loading="editSubmitting" @click="onEditSubmit">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchPermissions, createPermission, updatePermission, deletePermission } from '../api/permissions'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const hasPerm = (code) => auth.hasPerm(code)

const loading = ref(false)
const rows = ref([])
const total = ref(0)

const query = reactive({ page: 1, size: 10, code: '' })

const createVisible = ref(false)
const createSubmitting = ref(false)
const createFormRef = ref()
const createForm = reactive({ code: '', name: '', remark: '' })

const editVisible = ref(false)
const editSubmitting = ref(false)
const editFormRef = ref()
const editForm = reactive({ id: undefined, code: '', name: '', remark: '' })

const rules = {
  code: [{ required: true, message: '请输入权限码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}

async function fetchList() {
  loading.value = true
  try {
    const data = await fetchPermissions({ page: query.page, size: query.size, code: query.code || undefined })
    const list = Array.isArray(data?.records) ? data.records : []
    rows.value = list
    total.value = data?.total || 0
  } catch (e) {
    ElMessage.error(e?.msg || e?.message || '获取权限列表失败')
  } finally {
    loading.value = false
  }
}

function onSearch() { query.page = 1; fetchList() }
function onReset() { Object.assign(query, { page: 1, size: 10, code: '' }); fetchList() }
function onAdd() { Object.assign(createForm, { code: '', name: '', remark: '' }); createVisible.value = true }
function onEdit(row) { Object.assign(editForm, { id: row.id, code: row.code, name: row.name, remark: row.remark }); editVisible.value = true }

function onDelete(row) {
  deletePermission(row.id).then(() => { ElMessage.success('删除成功'); fetchList() }).catch(e => ElMessage.error(e?.msg || e?.message || '删除失败'))
}

function onCreateSubmit() {
  if (!createFormRef.value) return
  createFormRef.value.validate(async (valid) => {
    if (!valid) return
    createSubmitting.value = true
    try {
      await createPermission({ ...createForm })
      ElMessage.success('新增成功')
      createVisible.value = false
      fetchList()
    } catch (e) { ElMessage.error(e?.msg || e?.message || '新增失败') }
    finally { createSubmitting.value = false }
  })
}

function onEditSubmit() {
  if (!editFormRef.value) return
  editFormRef.value.validate(async (valid) => {
    if (!valid || !editForm.id) return
    editSubmitting.value = true
    try {
      await updatePermission(editForm.id, { code: editForm.code, name: editForm.name, remark: editForm.remark })
      ElMessage.success('保存成功')
      editVisible.value = false
      fetchList()
    } catch (e) { ElMessage.error(e?.msg || e?.message || '保存失败') }
    finally { editSubmitting.value = false }
  })
}

onMounted(() => { fetchList() })
</script>

<style scoped>
.toolbar { display: flex; gap: 8px; margin-bottom: 12px }
.pager { margin-top: 12px; display: flex; justify-content: flex-end }
</style>

