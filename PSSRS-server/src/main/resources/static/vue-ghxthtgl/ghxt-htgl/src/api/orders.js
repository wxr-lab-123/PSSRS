import request from './request'

export function fetchDoctorOrders(params) {
  return request.get('/orders', { params })
}


