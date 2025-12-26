<template>
  <el-card class="page-container">
    <template #header>
      <div class="page-header">
        <div class="header-title">
          <div class="title-decoration"></div>
          <span>科室管理</span>
        </div>
        <el-space :size="12">
          <el-input 
            v-model="query.name" 
            placeholder="请输入科室名称搜索" 
            clearable 
            style="width: 240px" 
            @keyup.enter="onSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="onSearch" :icon="Search">查询</el-button>
          <el-button @click="onReset" :icon="RefreshRight">重置</el-button>
          <el-button type="primary" class="add-btn" @click="onAdd" :icon="Plus">新增科室</el-button>
        </el-space>
      </div>
    </template>

    <el-table 
      v-if="!isCardView" 
      :data="rows" 
      style="width: 100%" 
      :loading="loading"
      stripe
      highlight-current-row
      header-cell-class-name="table-header"
    >
      <el-table-column prop="name" label="科室名称" min-width="150">
        <template #default="{ row }">
          <span class="dept-name">{{ row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="科室描述" min-width="300" show-overflow-tooltip>
        <template #default="{ row }">
          <span class="text-secondary">{{ row.description || '暂无描述' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="{ row }">
          <span class="date-text">{{ formatDateTime(row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="onEdit(row)" :icon="Edit">编辑</el-button>
          <el-button type="danger" link @click="onDelete(row)" :icon="Delete">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-else class="cards-grid">
      <el-card v-for="row in rows" :key="row.id || row.name" class="dept-card" shadow="hover" @click="onEdit(row)">
        <template #header>
          <div class="card-header">
            <el-icon class="card-icon"><OfficeBuilding /></el-icon>
            <span>{{ row.name }}</span>
          </div>
        </template>
        <div class="card-content">
          <div class="info-item">
            <span class="label">描述：</span>
            <span class="value">{{ row.description || '暂无描述' }}</span>
          </div>
          <div class="info-item">
            <span class="label">时间：</span>
            <span class="value">{{ formatDateTime(row.createTime) }}</span>
          </div>
        </div>
        <div class="card-actions" @click.stop>
          <el-button type="primary" size="small" @click="onEdit(row)" :icon="Edit" plain>编辑</el-button>
          <el-button type="danger" size="small" @click="onDelete(row)" :icon="Delete" plain>删除</el-button>
        </div>
      </el-card>
    </div>

    <div class="pagination-container">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="editForm.id ? '编辑科室' : '新增科室'" 
      width="500px"
      @close="resetForm"
      align-center
      destroy-on-close
      class="custom-dialog"
    >
      <el-form 
        ref="formRef" 
        :model="editForm" 
        :rules="rules" 
        label-width="90px"
        status-icon
      >
        <el-form-item label="科室名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入科室名称" :prefix-icon="OfficeBuilding" />
        </el-form-item>
        <el-form-item label="科室描述" prop="description">
          <el-input 
            v-model="editForm.description" 
            type="textarea" 
            :rows="4"
            placeholder="请输入科室职能、诊疗范围等描述信息..."
            show-word-limit
            maxlength="255"
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
import { OfficeBuilding, Search, RefreshRight, Plus, Edit, Delete } from '@element-plus/icons-vue'
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
.page-container {
  min-height: calc(100vh - 120px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.title-decoration {
  width: 4px;
  height: 18px;
  background: var(--color-primary);
  border-radius: 2px;
  margin-right: 12px;
}

.dept-name {
  font-weight: 500;
  color: var(--color-text-primary);
}

.text-secondary {
  color: var(--color-text-secondary);
  font-size: 13px;
}

.date-text {
  color: var(--color-text-secondary);
  font-family: monospace;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}

/* Card View Styles */
.cards-grid { 
  display: grid; 
  grid-template-columns: repeat(4, minmax(0, 1fr)); 
  gap: 20px;
}

@media (max-width: 1400px) { .cards-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); } }
@media (max-width: 1100px) { .cards-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 700px) { .cards-grid { grid-template-columns: 1fr; } }

.dept-card { 
  border-radius: 12px; 
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid var(--color-border);
}

.dept-card:hover { 
  transform: translateY(-4px); 
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
  border-color: var(--color-primary-soft);
}

.card-header { 
  font-weight: 600; 
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-icon {
  color: var(--color-primary);
  background: var(--color-primary-soft);
  padding: 6px;
  border-radius: 8px;
  font-size: 18px;
}

.card-content { 
  font-size: 14px; 
  line-height: 1.6; 
  padding: 0 4px; 
  display: flex; 
  flex-direction: column; 
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: flex-start;
}

.info-item .label {
  color: var(--color-text-secondary);
  width: 48px;
  flex-shrink: 0;
}

.info-item .value {
  color: var(--color-text-primary);
  flex: 1;
}

.card-actions { 
  display: flex; 
  justify-content: flex-end; 
  gap: 12px; 
  padding: 12px 4px 4px;
  border-top: 1px solid var(--color-border);
  margin-top: 16px;
}

/* Custom Table Header */
:deep(.table-header) {
  background-color: #f8fafc !important;
  color: var(--color-text-primary);
  font-weight: 600;
  height: 50px;
}
</style>
