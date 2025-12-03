import request from './request'

export function fetchDoctorOrders(params) {
  return request.get('/orders', { params })
}

export function fetchAdminOrders(params) {
  return request.get('/orders/list', { params })
}

export function callOrder(id, payload) {
  return request.post(`/orders/${id}/call`, payload)
}

export function skipOrder(id, payload) {
  return request.post(`/orders/${id}/skip`, payload)
}

export function recallOrder(id, payload) {
  return request.post(`/orders/${id}/recall`, payload)
}

export function startVisit(id, payload) {
  return request.post(`/orders/${id}/start`, payload)
}

export function endVisit(id, payload) {
  return request.post(`/orders/${id}/end`, payload)
}

export function updateQueueStatus(id, status, extra = {}) {
  return request.patch(`/orders/${id}/queue-status`, null, { params: { status, ...extra } })
}

