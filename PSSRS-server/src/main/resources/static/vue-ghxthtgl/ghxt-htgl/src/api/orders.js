import request from './request'

export function fetchDoctorOrders(params) {
  return request.get('/orders', { params })
}

export function fetchAdminOrders(params) {
  return request.get('/orders/list', { params })
}

