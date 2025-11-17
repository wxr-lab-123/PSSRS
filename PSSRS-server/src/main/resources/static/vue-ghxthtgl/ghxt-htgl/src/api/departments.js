import request from './request'
import { fromDepartmentListVO, fromDepartmentTreeVO, fromDepartmentVO } from '../vo/department'

/**
 * 获取科室列表（分页）
 * @param {Object} params - 查询参数
 * @returns {Promise<{records: DepartmentVO[], total: number, page: number, size: number}>}
 */
/**
 * 获取科室列表（分页）
 * @param {Object} params - { page, pageSize }
 * @returns {Promise<{records: DepartmentVO[], total: number, page: number, size: number}>}
 */
export function fetchDepartments(params) {
  console.log('fetchDepartments 请求参数:', params)
  return request.get('/departments', { params }).then(data => {
    // request 拦截器已经提取了 res.data，所以这里的 data 就是后端返回的 data 对象
    // 后端返回: {code: 0, msg: "success", data: {total: 20, records: [...]}}
    // 经过拦截器后: data = {total: 20, records: [...]}
    console.log('fetchDepartments 接收到的原始数据:', data)
    console.log('records 类型:', Array.isArray(data.records) ? '数组' : typeof data.records)
    console.log('records 长度:', data.records?.length || 0)
    
    const records = fromDepartmentListVO(data.records || [])
    const total = data.total || 0
    
    console.log('转换后的 records 长度:', records.length)
    
    return {
      records,
      total,
      page: params.page || 1,
      size: params.pageSize || 10
    }
  })
}

/**
 * 获取科室树形结构
 * @returns {Promise<DepartmentTreeVO[]>}
 */
export function fetchDepartmentsTree() {
  return request.get('/departments', { params: { tree: true } }).then(data =>
    fromDepartmentTreeVO(data)
  )
}

/**
 * 获取科室平铺列表
 * 兼容两种返回：数组 或 {records: []}
 * @param {Object} params 可选过滤参数
 * @returns {Promise<Array<{id:number|string,name:string}>>}
 */
export function fetchDepartmentsList(params = {}) {
  return request.get('/departments/list', { params }).then(data => {
    const list = Array.isArray(data) ? data : (Array.isArray(data?.records) ? data.records : [])
    return list.map(d => ({ id: d.id, name: d.name }))
  })
}

/**
 * 获取科室详情
 * @param {number} id - 科室ID
 * @returns {Promise<DepartmentVO>}
 */
export function fetchDepartmentDetail(id) {
  return request.get(`/departments/${id}`).then(data => fromDepartmentVO(data))
}

/**
 * 创建科室
 * @param {CreateDepartmentDTO} dto - 创建科室 DTO
 * @returns {Promise<DepartmentVO>}
 */
export function createDepartment(dto) {
  return request.post('/departments', dto).then(data => fromDepartmentVO(data))
}

/**
 * 更新科室
 * @param {number} id - 科室ID
 * @param {UpdateDepartmentDTO} dto - 更新科室 DTO
 * @returns {Promise<DepartmentVO>}
 */
export function updateDepartment(id, dto) {
  return request.put(`/departments/${id}`, dto).then(data => fromDepartmentVO(data))
}

/**
 * 删除科室
 * @param {number} id - 科室ID
 * @returns {Promise<void>}
 */
export function deleteDepartment(id) {
  return request.delete(`/departments/${id}`)
}
