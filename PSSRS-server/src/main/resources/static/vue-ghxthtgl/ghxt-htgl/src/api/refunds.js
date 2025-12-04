import request from './request'

export function approveRefund(payload) {
  return request.post('/order/refund/approve', payload)
}
