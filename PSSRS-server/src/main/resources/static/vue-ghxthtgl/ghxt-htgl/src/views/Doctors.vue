<template>
  <el-card class="page-container">
    <template #header>
      <div class="page-header">
        <div class="header-title">
          <div class="title-decoration"></div>
          <span>医生管理</span>
        </div>
        <el-space :size="12">
          <el-input 
            v-model="query.username" 
            placeholder="请输入姓名搜索" 
            clearable 
            style="width: 200px" 
            @keyup.enter="onSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-select 
            v-model="query.departmentId" 
            placeholder="选择科室" 
            clearable 
            style="width: 160px"
          >
            <el-option v-for="d in flatDepts" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
          <el-button type="primary" @click="onSearch" :icon="Search">查询</el-button>
          <el-button @click="onReset" :icon="RefreshRight">重置</el-button>
          <el-button type="primary" class="add-btn" @click="onAdd" :icon="Plus">新增医生</el-button>
        </el-space>
      </div>
    </template>

    <el-table 
      v-if="!isCardView" 
      :data="rows" 
      :loading="loading" 
      style="width:100%"
      stripe
      highlight-current-row
      header-cell-class-name="table-header"
    >
      <el-table-column label="头像" width="100" align="center">
        <template #default="{ row }">
          <el-avatar 
            :src="row.image || row.avatar" 
            :size="48" 
            fit="cover"
            @click="(row.image || row.avatar) && handlePreviewImage(row.image || row.avatar)"
            :style="{ cursor: (row.image || row.avatar) ? 'pointer' : 'default' }"
            class="table-avatar"
          >
            <template #default>
              <el-icon><User /></el-icon>
            </template>
          </el-avatar>
        </template>
      </el-table-column>
      <el-table-column prop="username" label="姓名" min-width="120">
        <template #default="{ row }">
          <span class="doctor-name">{{ row.username }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="职称" min-width="120">
        <template #default="{ row }">
          <el-tag size="small" effect="plain">{{ row.title || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="departmentName" label="所属科室" min-width="150" />
      <el-table-column prop="phone" label="手机号" width="150" />
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="(row.status === 1 || row.status === '1') ? 'success' : 'info'" effect="light">
            {{ (row.status === 1 || row.status === '1') ? '启用' : (row.status === 2 || row.status === '2') ? '禁用' : row.status }}
          </el-tag>
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
      <el-card v-for="row in rows" :key="row.id" class="doctor-card" shadow="hover" @click="onEdit(row)">
        <div class="card-content-wrapper">
          <div class="card-left">
            <el-avatar :src="row.image || row.avatar" :size="80" fit="cover" class="card-avatar">
              <el-icon><User /></el-icon>
            </el-avatar>
            <div class="card-status">
              <el-tag size="small" :type="(row.status === 1 || row.status === '1') ? 'success' : 'info'" effect="dark">
                {{ (row.status === 1 || row.status === '1') ? '启用' : '禁用' }}
              </el-tag>
            </div>
          </div>
          <div class="card-right">
            <div class="card-header">
              <span class="name">{{ row.username }}</span>
              <span class="title">{{ row.title || '-' }}</span>
            </div>
            <div class="card-info">
              <div class="info-item">
                <el-icon><OfficeBuilding /></el-icon>
                <span>{{ row.departmentName || '-' }}</span>
              </div>
              <div class="info-item">
                <el-icon><Iphone /></el-icon>
                <span>{{ row.phone || '-' }}</span>
              </div>
            </div>
            <div class="card-actions" @click.stop>
              <el-button type="primary" size="small" @click="onEdit(row)" :icon="Edit" plain round>编辑</el-button>
              <el-button type="danger" size="small" @click="onDelete(row)" :icon="Delete" plain round>删除</el-button>
            </div>
          </div>
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

    <el-dialog 
      v-model="editVisible" 
      :title="editForm.id ? '编辑医生' : '新增医生'" 
      width="600px"
      align-center
      destroy-on-close
      class="custom-dialog"
    >
      <el-form 
        ref="editFormRef" 
        :model="editForm" 
        :rules="rules" 
        label-width="90px"
        status-icon
      >
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="头像" prop="image">
              <div class="avatar-uploader-wrapper">
                <el-avatar 
                  :src="editForm.image" 
                  :size="80" 
                  fit="cover"
                  class="preview-avatar"
                  @click="editForm.image && handlePreviewImage(editForm.image)"
                >
                  <el-icon><User /></el-icon>
                </el-avatar>
                <div class="upload-area">
                  <el-upload
                    :action="''"
                    :auto-upload="true"
                    :show-file-list="false"
                    :before-upload="beforeAvatarUpload"
                    :http-request="handleAvatarUpload"
                    accept="image/*"
                  >
                    <el-button type="primary" plain size="small" :loading="avatarUploading">点击上传</el-button>
                  </el-upload>
                  <div class="upload-tip">支持 JPG/PNG，小于 2MB</div>
                </div>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="username">
              <el-input v-model="editForm.username" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="editForm.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="editForm.gender">
                <el-radio label="0">男</el-radio>
                <el-radio label="1">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职称" prop="title">
              <el-select v-model="editForm.title" placeholder="选择职称" style="width: 100%">
                <el-option label="主任医师" value="主任医师" />
                <el-option label="副主任医师" value="副主任医师" />
                <el-option label="主治医师" value="主治医师" />
                <el-option label="医师" value="医师" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="所属科室" prop="departmentId">
              <el-select 
                v-model="editForm.departmentId" 
                placeholder="请选择科室" 
                style="width: 100%"
                filterable
              >
                <el-option v-for="d in flatDepts" :key="d.id" :label="d.name" :value="d.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="简介" prop="description">
              <el-input 
                v-model="editForm.description" 
                type="textarea" 
                rows="3" 
                placeholder="请输入医生简介..."
                show-word-limit
                maxlength="500"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="状态" prop="status">
              <el-switch
                v-model="editForm.status"
                :active-value="1"
                :inactive-value="2"
                active-text="启用"
                inactive-text="禁用"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editVisible = false">取消</el-button>
          <el-button type="primary" :loading="editSubmitting" @click="onSave">保 存</el-button>
        </span>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, inject, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Search, RefreshRight, Plus, Edit, Delete, OfficeBuilding, Iphone } from '@element-plus/icons-vue'
import { fetchDoctors, createDoctor, updateDoctor, deleteDoctor } from '../api/doctors'
import { fetchDepartmentsList } from '../api/departments'
import { uploadDoctorAvatar } from '../api/upload'

const loading = ref(false)

const rows = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const query = reactive({ username: '', departmentId: null })

const editVisible = ref(false)
const editSubmitting = ref(false)
const editFormRef = ref()
const avatarUploading = ref(false)
const editForm = reactive({ id: null, username: '', phone: '', gender: '0', title: '', departmentId: null, description: '', status: 1, image: '' })

// 科室单选弹窗状态
const deptPickerVisible = ref(false)
const tempDepartmentId = ref()

const phonePattern = /^(?:(?:\+?86)?1\d{10})$/
const rules = {
  username: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ pattern: phonePattern, message: '手机号格式不正确', trigger: ['blur', 'change'] }],
  departmentId: [{ required: true, message: '请选择科室', trigger: 'change' }]
}

const flatDepts = ref([])
async function loadDepartments() {
  try {
    const list = await fetchDepartmentsList()
    flatDepts.value = list || []
  } catch { flatDepts.value = [] }
}

async function fetchList() {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: pageSize.value,  // 后端参数名是 size 不是 pageSize
      ...(query.username ? { username: query.username } : {}),
      ...(query.departmentId !== null && query.departmentId !== undefined && query.departmentId !== '' ? { departmentId: query.departmentId } : {})
    }
    const data = await fetchDoctors(params)
    
    // 清空数组并使用 nextTick 强制重新渲染
    rows.value = []
    await nextTick()
    
    rows.value = (data.records || []).map(r => {
      const mapped = {
        ...r,
        id: r.id || r.userId,  // 确保 id 字段存在
        username: r.username || r.name,  // 兼容 username 和 name 字段
        departmentName: r.departmentName || r.department?.name
      }
      console.log('Mapped doctor row:', mapped)  // 调试日志
      return mapped
    })
    total.value = data.total || 0
  } catch (e) { ElMessage.error(e?.msg || e?.message || '获取列表失败') }
  finally { loading.value = false }
}

function onSearch() { page.value = 1; fetchList() }
function onReset() { Object.assign(query, { username: '', departmentId: null }); page.value = 1; fetchList() }

function handlePageChange(p) {
  page.value = p
  fetchList()
}

function handleSizeChange(size) {
  pageSize.value = size
  page.value = 1
  fetchList()
}
function onAdd() { Object.assign(editForm, { id: null, username: '', phone: '', gender: '0', title: '', departmentId: null, description: '', status: 1, image: '' }); editVisible.value = true }
function onEdit(row) {
  const deptId = row.departmentId ?? (() => {
    const name = row.departmentName || row.department?.name
    const found = (flatDepts.value || []).find(d => d.name === name)
    return found ? found.id : null
  })()
  Object.assign(editForm, {
    id: row.id,
    username: row.username || row.name,
    phone: row.phone,
    gender: row.gender || '0',
    title: row.title,
    departmentId: deptId,
    description: row.description,
    status: row.status ?? 1,
    image: row.image || row.avatar || ''
  })
  editVisible.value = true
}

// 头像上传
function beforeAvatarUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

async function handleAvatarUpload(options) {
  const { file } = options
  avatarUploading.value = true
  try {
    const url = await uploadDoctorAvatar(file)
    editForm.image = url
    ElMessage.success('头像上传成功')
  } catch (e) {
    ElMessage.error(e?.msg || e?.message || '头像上传失败')
  } finally {
    avatarUploading.value = false
  }
}

function handlePreviewImage(url) {
  if (!url) return
  // 使用浏览器原生方式预览图片
  window.open(url, '_blank')
}

function onSave() {
  if (!editFormRef.value) return
  editFormRef.value.validate(async (valid) => {
    if (!valid) return
    editSubmitting.value = true
    try {
      // 准备提交数据，移除 null/undefined 的 id 字段
      const payload = { ...editForm }
      if (!payload.id) {
        delete payload.id
      }
      
      if (editForm.id) {
        await updateDoctor(editForm.id, payload)
        ElMessage.success('保存成功')
        editVisible.value = false
        fetchList() // 重新获取列表数据
      } else {
        await createDoctor(payload)
        ElMessage.success('保存成功')
        editVisible.value = false
        fetchList() // 新增后需要获取ID等数据
      }
    } catch (e) { 
      ElMessage.error(e?.msg || e?.message || '保存失败')
      fetchList() // 出错时重新获取列表
    }
    finally { editSubmitting.value = false }
  })
}

function onDelete(row) {
  console.log('删除医生数据:', row)
  const doctorId = row.id || row.doctorId || row.userId || row.userId
  if (!doctorId) {
    ElMessage.error('无效的医生ID')
    return
  }
  ElMessageBox.confirm(`确认删除医生「${row.username || row.name || '该医生'}」吗？`, '提示', { type: 'warning' })
    .then(async () => { try { await deleteDoctor(doctorId); ElMessage.success('删除成功'); fetchList() } catch (e) { ElMessage.error(e?.msg || e?.message || '删除失败') } })
}

const uiViewMode = inject('ui_view_mode', ref('card'))
const isCardView = computed(() => (uiViewMode?.value || 'card') === 'card')

onMounted(() => { loadDepartments(); fetchList() })
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

.table-avatar {
  border: 2px solid var(--color-border);
  transition: transform 0.3s;
}

.table-avatar:hover {
  transform: scale(1.1);
}

.doctor-name {
  font-weight: 600;
  color: var(--color-text-primary);
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}

/* Card View */
.cards-grid { 
  display: grid; 
  grid-template-columns: repeat(3, minmax(0, 1fr)); 
  gap: 24px;
}

@media (max-width: 1400px) { .cards-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 900px) { .cards-grid { grid-template-columns: 1fr; } }

.doctor-card { 
  border-radius: 16px; 
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid var(--color-border);
  overflow: hidden;
}

.doctor-card:hover { 
  transform: translateY(-4px); 
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
  border-color: var(--color-primary-soft);
}

.card-content-wrapper {
  display: flex;
  gap: 20px;
  padding: 8px;
}

.card-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  min-width: 100px;
}

.card-avatar {
  border: 3px solid var(--color-bg-page);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.card-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.card-header { 
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 4px;
}

.card-header .name {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text-primary);
}

.card-header .title {
  font-size: 13px;
  color: var(--color-primary);
  background: var(--color-primary-soft);
  padding: 2px 8px;
  border-radius: 4px;
}

.card-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--color-text-secondary);
}

.card-actions { 
  display: flex; 
  justify-content: flex-end; 
  gap: 8px; 
  margin-top: 8px;
  border-top: 1px solid var(--color-border);
  padding-top: 12px;
}

/* Upload Styles */
.avatar-uploader-wrapper {
  display: flex;
  align-items: center;
  gap: 24px;
}

.preview-avatar {
  cursor: pointer;
  border: 2px solid var(--color-border);
  transition: all 0.3s;
}

.preview-avatar:hover {
  border-color: var(--color-primary);
  opacity: 0.9;
}

.upload-area {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.upload-tip {
  font-size: 12px;
  color: var(--color-text-secondary);
}

:deep(.table-header) {
  background-color: #f8fafc !important;
  color: var(--color-text-primary);
  font-weight: 600;
  height: 50px;
}
</style>
