<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <div>患者管理</div>
        <el-space>
          <el-input v-model="query.name" placeholder="姓名" clearable style="width:160px" />
          <el-select v-model="query.gender" placeholder="性别" clearable style="width:120px">
            <el-option label="男" value="0" />
            <el-option label="女" value="1" />
          </el-select>
          <el-input v-model="query.phone" placeholder="手机号" clearable style="width:180px" />
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
          <el-button type="primary" @click="onAdd">新增患者</el-button>
        </el-space>
      </div>
    </template>
    <el-table :data="rows" style="width: 100%" :loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="姓名" />
      <el-table-column prop="gender" label="性别" width="80">
        <template #default="{ row }">{{ row.gender === '0' ? '男' : row.gender === '1' ? '女' : '-' }}</template>
      </el-table-column>
      <el-table-column prop="age" label="年龄" width="80" />
      <el-table-column prop="phone" label="联系电话" />
      <el-table-column prop="idCard" label="身份证号" min-width="160" />
      <el-table-column prop="address" label="家庭住址" min-width="180" />
      <el-table-column label="操作" width="160">
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
    <el-dialog v-model="createVisible" title="新增患者" width="520px">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="88px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="createForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="createForm.gender" placeholder="请选择性别" style="width: 160px">
            <el-option label="男" value="0" />
            <el-option label="女" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input v-model.number="createForm.age" placeholder="请输入年龄" type="number" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="createForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="createForm.idCard" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="家庭住址" prop="address">
          <el-input v-model="createForm.address" placeholder="请输入家庭住址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createVisible = false">取消</el-button>
          <el-button type="primary" :loading="createSubmitting" @click="onCreateSubmit">确 定</el-button>
        </span>
      </template>
    </el-dialog>
    <el-dialog v-model="editVisible" title="编辑患者" width="520px">
      <el-form ref="editFormRef" :model="editForm" :rules="createRules" label-width="88px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="editForm.gender" placeholder="请选择性别" style="width: 160px">
            <el-option label="男" value="0" />
            <el-option label="女" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input v-model.number="editForm.age" placeholder="请输入年龄" type="number" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="editForm.idCard" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="家庭住址" prop="address">
          <el-input v-model="editForm.address" placeholder="请输入家庭住址" />
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
import { ref, reactive, onMounted, inject, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { fetchPatients, createPatient, updatePatient, deletePatient } from '../api/patients'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10, name: '', gender: '', phone: '' })

const createVisible = ref(false)
const createSubmitting = ref(false)
const createFormRef = ref()
const createForm = reactive({ name: '', gender: '', age: undefined, phone: '', idCard: '', address: '' })
const phonePattern = /^(?:(?:\+?86)?1\d{10})$/
// 简化身份证校验：18位，前17位数字，最后一位数字或X/x
const idCardPattern = /^(\d{17})([0-9Xx])$/

const createRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  age: [
    { required: true, message: '请输入年龄', trigger: 'blur' },
    { type: 'number', message: '年龄需为数字', trigger: 'blur' },
    {
      validator: (_, value, cb) => {
        if (value === undefined || value === null || value === '') return cb(new Error('请输入年龄'))
        if (value < 0 || value > 120) return cb(new Error('年龄范围 0-120'))
        cb()
      },
      trigger: 'blur'
    }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: phonePattern, message: '手机号格式不正确', trigger: ['blur', 'change'] }
  ]
}

createRules.idCard = [
  { required: true, message: '请输入身份证号', trigger: 'blur' },
  { pattern: idCardPattern, message: '身份证号格式不正确', trigger: ['blur', 'change'] }
]
createRules.address = [
  { required: true, message: '请输入家庭住址', trigger: 'blur' }
]

async function fetchList() {
  loading.value = true
  try {
    const data = await fetchPatients({ ...query })
    rows.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error(e?.msg || e?.message || '获取列表失败')
  } finally {
    loading.value = false
  }
}

function onSearch() {
  query.page = 1
  fetchList()
}

function onReset() {
  query.page = 1
  query.size = 10
  query.name = ''
  query.gender = ''
  query.phone = ''
  fetchList()
}

function onAdd() {
  createForm.name = ''
  createForm.gender = ''
  createForm.age = undefined
  createForm.phone = ''
  createForm.idCard = ''
  createForm.address = ''
  createVisible.value = true
}

function onCreateSubmit() {
  if (!createFormRef.value) return
  createFormRef.value.validate(async (valid) => {
    if (!valid) return
    createSubmitting.value = true
    try {
      await createPatient({ ...createForm })
      ElMessage.success('新增成功')
      createVisible.value = false
      fetchList()
    } catch (e) {
      ElMessage.error(e?.msg || e?.message || '新增失败')
    } finally {
      createSubmitting.value = false
    }
  })
}

const uiViewMode = inject('ui_view_mode', ref('card'))
const isCardView = computed(() => (uiViewMode?.value || 'card') === 'card')

onMounted(fetchList)

// 编辑 & 删除
const editVisible = ref(false)
const editSubmitting = ref(false)
const editFormRef = ref()
const editForm = reactive({ id: undefined, name: '', gender: '', age: undefined, phone: '', idCard: '', address: '' })

function onEdit(row) {
  editForm.id = row.id
  editForm.name = row.name
  editForm.gender = row.gender
  editForm.age = row.age
  editForm.phone = row.phone
  editForm.idCard = row.idCard
  editForm.address = row.address
  editVisible.value = true
}

function onEditSubmit() {
  if (!editFormRef.value) return
  editFormRef.value.validate(async (valid) => {
    if (!valid) return
    editSubmitting.value = true
    try {
      const { id, ...payload } = editForm
      await updatePatient(id, payload)
      ElMessage.success('保存成功')
      editVisible.value = false
      
      // 直接更新列表中的对应项，使用表单数据
      const index = rows.value.findIndex(r => r.id === id)
      if (index !== -1) {
        rows.value[index] = {
          ...rows.value[index],
          name: payload.name,
          gender: payload.gender,
          age: payload.age,
          phone: payload.phone,
          idCard: payload.idCard,
          address: payload.address
        }
      } else {
        // 如果找不到，重新获取列表
        fetchList()
      }
    } catch (e) {
      ElMessage.error(e?.msg || e?.message || '保存失败')
      fetchList() // 出错时重新获取列表
    } finally {
      editSubmitting.value = false
    }
  })
}

function onDelete(row) {
  ElMessageBox.confirm(`确认删除患者「${row.name}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        await deletePatient(row.id)
        ElMessage.success('删除成功')
        fetchList()
      } catch (e) {
        ElMessage.error(e?.msg) // Prefer backend error messages
      }
    })
}
</script>

<style scoped>
</style>


