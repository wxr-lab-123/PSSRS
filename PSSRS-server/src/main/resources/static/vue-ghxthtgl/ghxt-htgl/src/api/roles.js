import request from './request'

export function fetchRoles() {
  return request.get('/roles')
}

export function fetchRolesPage(page = 1, size = 100) {
  return request.get('/roles', { params: { page, size } })
}

export function assignRolePermissions(roleId, allowIds = [], denyIds = []) {
  return request.post(`/roles/${roleId}/permissions`, { allowIds, denyIds })
}

export function createRole(payload) {
  return request.post('/roles', payload)
}

export function updateRole(id, payload) {
  return request.put(`/roles/${id}`, payload)
}

export function deleteRole(id) {
  return request.delete(`/roles/${id}`)
}

export function fetchPermissions(params) {
  return request.get('/permissions', { params })
}

export function fetchAllPermissions() {
  return request.get('/permissions', { params: { page: 1, size: 1000 } })
}

