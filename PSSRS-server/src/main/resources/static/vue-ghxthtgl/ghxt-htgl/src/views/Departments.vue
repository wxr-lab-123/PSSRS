<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <div>科室管理</div>
        <el-space>
          <el-input v-model="query.name" placeholder="科室名称" clearable style="width:160px" @keyup.enter="onSearch" />
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
          <el-button type="primary" @click="onAdd">新增科室</el-button>
        </el-space>
      </div>
    </template>
    <el-table v-if="!isCardView" :data="rows" style="width: 100%" :loading="loading">
      <el-table-column prop="name" label="科室名称" />
      <el-table-column prop="description" label="科室描述" min-width="300" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button type="primary" link @click="onEdit(row)">编辑</el-button>
          <el-button type="danger" link @click="onDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-else class="cards-grid">
      <el-card v-for="row in rows" :key="row.id || row.name" class="dept-card" shadow="hover" @click="onEdit(row)">
        <template #header>
          <div class="card-header">{{ row.name }}</div>
        </template>
        <div class="card-content">
          <div>描述：{{ row.description }}</div>
          <div>创建时间：{{ formatDateTime(row.createTime) }}</div>
        </div>
        <div class="card-actions" @click.stop>
          <el-button type="primary" size="small" @click="onEdit(row)">编辑</el-button>
          <el-button type="danger" size="small" @click="onDelete(row)">删除</el-button>
        </div>
      </el-card>
    </div>

    <div style="display:flex;justify-content:flex-end;margin-top:12px">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :page-sizes="[10,20,50,100]"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="editForm.id ? '编辑科室' : '新增科室'" 
      width="520px"
      @close="resetForm"
    >
      <el-form 
        ref="formRef" 
        :model="editForm" 
        :rules="rules" 
        label-width="88px"
      >
        <el-form-item label="科室名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入科室名称" />
        </el-form-item>
        <el-form-item label="科室描述" prop="description">
          <el-input 
            v-model="editForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入科室描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="onSubmit">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, inject, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { OfficeBuilding } from '@element-plus/icons-vue'
import { fetchDepartments, createDepartment, updateDepartment, deleteDepartment } from '../api/departments'
import { toCreateDepartmentDTO, toUpdateDepartmentDTO } from '../dto/department'
import { formatDateTime } from '../utils/date'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const query = reactive({ name: '' })

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const editForm = reactive({ id: undefined, name: '', description: '' })

const rules = {
  name: [
    { required: true, message: '请输入科室名称', trigger: 'blur' },
    { min: 1, max: 100, message: '科室名称长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 255, message: '科室描述不能超过 255 个字符', trigger: 'blur' }
  ]
}

async function fetchList() {
  loading.value = true
  try {
    const params = { 
      page: page.value, 
      pageSize: pageSize.value,
      ...(query.name ? { name: query.name } : {})
    }
    const data = await fetchDepartments(params)
    
    rows.value = []
    await nextTick()
    
    rows.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    console.error('获取数据失败:', e)
    ElMessage.error(e?.msg || e?.message || '获取数据失败')
  } finally {
    loading.value = false
  }
}

function onSearch() {
  page.value = 1
  fetchList()
}

function onReset() {
  query.name = ''
  page.value = 1
  fetchList()
}

function handlePageChange(p) {
  page.value = p
  fetchList()
}

function handleSizeChange(size) {
  pageSize.value = size
  page.value = 1
  fetchList()
}

function onAdd() {
  resetForm()
  dialogVisible.value = true
}

function onEdit(row) {
  editForm.id = row.id
  editForm.name = row.name
  editForm.description = row.description || ''
  dialogVisible.value = true
}

function resetForm() {
  editForm.id = undefined
  editForm.name = ''
  editForm.description = ''
  formRef.value?.clearValidate()
}

async function onSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (editForm.id) {
        const dto = toUpdateDepartmentDTO(editForm)
        await updateDepartment(editForm.id, dto)
        ElMessage.success('更新成功')
        
        const index = rows.value.findIndex(r => r.id === editForm.id)
        if (index !== -1) {
          rows.value[index] = {
            ...rows.value[index],
            name: editForm.name,
            description: editForm.description || ''
          }
        } else {
          fetchList()
        }
      } else {
        const dto = toCreateDepartmentDTO(editForm)
        await createDepartment(dto)
        ElMessage.success('新增成功')
        fetchList()
      }
      dialogVisible.value = false
    } catch (e) {
      ElMessage.error(e?.msg || e?.message || (editForm.id ? '更新失败' : '新增失败'))
      fetchList()
    } finally {
      submitting.value = false
    }
  })
}

function onDelete(row) {
  ElMessageBox.confirm(
    `确认删除科室「${row.name}」吗？删除后不可恢复！`,
    '提示',
    { type: 'warning' }
  ).then(async () => {
    try {
      await deleteDepartment(row.id)
      ElMessage.success('删除成功')
      fetchList()
    } catch (e) {
      ElMessage.error(e?.msg || e?.message || '删除失败')
    }
  }).catch(() => {})
}

const uiViewMode = inject('ui_view_mode', ref('card'))
const isCardView = computed(() => (uiViewMode?.value || 'card') === 'card')

onMounted(fetchList)
</script>

<style scoped>
.cards-grid { display:grid; grid-template-columns:repeat(4,minmax(0,1fr)); gap:16px }
@media (max-width:1200px){ .cards-grid{ grid-template-columns:repeat(3,minmax(0,1fr)) } }
@media (max-width:900px){ .cards-grid{ grid-template-columns:repeat(2,minmax(0,1fr)) } }
@media (max-width:600px){ .cards-grid{ grid-template-columns:1fr } }
.dept-card { border-radius:8px; transition: all .2s ease }
.dept-card:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,.15) }
.card-header { font-weight:600; font-size:16px }
.card-content { font-size:14px; line-height:1.6; padding: 0 16px; display:flex; flex-direction:column; gap:12px }
.card-actions { display:flex; justify-content:flex-end; gap:8px; padding: 8px 16px }
</style>
