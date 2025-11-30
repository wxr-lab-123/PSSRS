import request from './request'

export function fetchPermissions(params) {
  return request.get('/permissions', { params })
}

export function createPermission(payload) {
  return request.post('/permissions', payload)
}

export function updatePermission(id, payload) {
  return request.put(`/permissions/${id}`, payload)
}

export function deletePermission(id) {
  return request.delete(`/permissions/${id}`)
}

