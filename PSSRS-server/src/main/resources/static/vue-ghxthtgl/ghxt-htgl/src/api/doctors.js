import request from './request'

export function fetchDoctors(params) {
  // 添加时间戳防止浏览器缓存
  return request.get('/doctors', { 
    params: { 
      ...params, 
      _t: Date.now() 
    } 
  })
}

export function createDoctor(payload) {
  return request.post('/doctors', payload)
}

export function updateDoctor(id, payload) {
  return request.put(`/doctors/${id}`, payload)
}

export function deleteDoctor(id) {
  return request.delete(`/doctors/${id}`)
}

// 根据部门ID获取医生列表
export function fetchDoctorsByDepartment(departmentId) {
  return request.get(`/doctors/department/${departmentId}`)
}

