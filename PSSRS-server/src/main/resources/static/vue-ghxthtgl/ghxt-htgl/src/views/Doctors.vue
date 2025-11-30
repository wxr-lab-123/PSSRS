<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <div>医生管理</div>
        <el-space>
          <el-input v-model="query.username" placeholder="姓名" clearable style="width:160px" />
          <el-select v-model="query.departmentId" placeholder="科室" clearable style="width:200px">
            <el-option v-for="d in flatDepts" :key="d.id" :label="d.name" :value="d.id" />
            <template #empty>
              <div style="padding:8px;color:#909399">无数据，接口：GET /api/admin/departments/list</div>
            </template>
          </el-select>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
          <el-button type="primary" @click="onAdd">新增医生</el-button>
        </el-space>
      </div>
    </template>
    <el-table v-if="!isCardView" :data="rows" :loading="loading" style="width:100%">
      <el-table-column label="头像" width="80">
        <template #default="{ row }">
          <el-avatar 
            :src="row.image || row.avatar" 
            :size="50" 
            fit="cover"
            @click="(row.image || row.avatar) && handlePreviewImage(row.image || row.avatar)"
            :style="{ cursor: (row.image || row.avatar) ? 'pointer' : 'default' }"
          >
            <template #default>
              <el-icon><User /></el-icon>
            </template>
          </el-avatar>
        </template>
      </el-table-column>
      <el-table-column prop="username" label="姓名" />
      <el-table-column prop="title" label="职称" />
      <el-table-column prop="departmentName" label="所属科室" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="(row.status === 1 || row.status === '1') ? 'success' : 'info'">
            {{ (row.status === 1 || row.status === '1') ? '启用' : (row.status === 2 || row.status === '2') ? '禁用' : row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button type="primary" link @click="onEdit(row)">编辑</el-button>
          <el-button type="danger" link @click="onDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-else class="cards-grid">
      <el-card v-for="row in rows" :key="row.id" class="doctor-card" shadow="hover" @click="onEdit(row)">
        <template #header>
          <div class="card-header">{{ row.username }}</div>
        </template>
        <div class="card-body">
          <div class="card-image">
            <el-avatar :src="row.image || row.avatar" :size="72" fit="cover">
              <el-icon><User /></el-icon>
            </el-avatar>
          </div>
          <div class="card-content">
            <div>职称：{{ row.title || '-' }}</div>
            <div>科室：{{ row.departmentName || '-' }}</div>
            <div>电话：{{ row.phone || '-' }}</div>
            <div>状态：{{ (row.status === 1 || row.status === '1') ? '启用' : (row.status === 2 || row.status === '2') ? '禁用' : row.status }}</div>
          </div>
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

    <el-dialog v-model="editVisible" :title="editForm.id ? '编辑医生' : '新增医生'" width="640px">
      <el-form ref="editFormRef" :model="editForm" :rules="rules" label-width="100px">
        <el-form-item label="头像" prop="image">
          <div style="display:flex;align-items:center;gap:16px">
            <el-avatar 
              :src="editForm.image" 
              :size="80" 
              fit="cover"
              @click="editForm.image && handlePreviewImage(editForm.image)"
              :style="{ cursor: editForm.image ? 'pointer' : 'default' }"
            >
              <el-icon><User /></el-icon>
            </el-avatar>
            <el-upload
              :action="''"
              :auto-upload="true"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="handleAvatarUpload"
              accept="image/*"
            >
              <el-button size="small" :loading="avatarUploading">上传头像</el-button>
              <template #tip>
                <div style="font-size:12px;color:#909399;margin-top:4px">
                  支持 JPG/PNG 格式，大小不超过 2MB
                </div>
              </template>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="姓名" prop="username">
          <el-input v-model="editForm.username" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="editForm.gender" style="width:160px">
            <el-option label="男" value="0" />
            <el-option label="女" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="职称" prop="title">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="所属科室" prop="departmentId">
          <div style="display:flex;align-items:center;gap:8px">
            <el-select v-model="editForm.departmentId" style="width:240px">
              <el-option v-for="d in flatDepts" :key="d.id" :label="d.name" :value="d.id" />
              <template #empty>
                <div style="padding:8px;color:#909399">无数据，接口：GET /api/admin/departments/list</div>
              </template>
            </el-select>
            <el-button size="small" @click="openDeptPicker">选择科室</el-button>
          </div>
        </el-form-item>
        <el-form-item label="简介" prop="description">
          <el-input v-model="editForm.description" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="editForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="2">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editVisible = false">取消</el-button>
          <el-button type="primary" :loading="editSubmitting" @click="onSave">保 存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 科室单选弹窗 -->
    <el-dialog v-model="deptPickerVisible" title="选择科室" width="520px">
      <el-radio-group v-model="tempDepartmentId" class="dept-radio-group">
        <el-radio v-for="d in flatDepts" :key="d.id" :label="d.id">{{ d.name }}</el-radio>
      </el-radio-group>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deptPickerVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmDeptPick">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, inject, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User } from '@element-plus/icons-vue'
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
.cards-grid { display:grid; grid-template-columns:repeat(4,minmax(0,1fr)); gap:16px }
@media (max-width:1200px){ .cards-grid{ grid-template-columns:repeat(3,minmax(0,1fr)) } }
@media (max-width:900px){ .cards-grid{ grid-template-columns:repeat(2,minmax(0,1fr)) } }
@media (max-width:600px){ .cards-grid{ grid-template-columns:1fr } }
.doctor-card { border-radius:8px; transition: all .2s ease }
.doctor-card:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,.15) }
.card-header { font-weight:600; font-size:16px }
.card-body { display:flex; gap:12px; padding: 0 16px }
.card-image { display:flex; align-items:center }
.card-content { font-size:14px; line-height:1.6; flex:1; display:flex; flex-direction:column; gap:12px }
.card-actions { display:flex; justify-content:flex-end; gap:8px; padding: 8px 16px }
</style>
