import request from './request'

export function fetchAdmins(params) {
  return request.get('/admins', { params })
}

export function createAdmin(payload) {
  return request.post('/admins', payload)
}

export function updateAdmin(id, payload) {
  return request.put(`/admins/${id}`, payload)
}

export function deleteAdmin(id) {
  return request.delete(`/admins/${id}`)
}


