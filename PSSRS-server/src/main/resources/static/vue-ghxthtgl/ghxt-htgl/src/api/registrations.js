import request from './request'

export function fetchAdminRegistrations(params) {
  return request.get('/registrations', { params })
}

export function updateRegistration(id, payload) {
  return request.put(`/registrations/${id}`, payload)
}

export function cancelRegistration(id, payload) {
  return request.put(`/registrations/${id}/cancel`, payload)
}
