// api/order.js
// 订单相关API

const request = require('../utils/request.js')
const config = require('../utils/config.js')

function getOrderList(params = {}) {
  const payload = {}
  ;['status', 'orderNo', 'date'].forEach(key => {
    const val = params[key]
    if (val !== undefined && val !== null && val !== '') payload[key] = val
  })
  return request.get(config.API.GET_ORDER_LIST, payload)
}

function applyRefund(orderNo) {
  return request.post(config.API.APPLY_REFUND, { orderNo })
}

function getRefundStatus(orderNo) {
  return request.get(config.API.GET_REFUND_STATUS, { orderNo })
}

/**
 * 创建挂号订单
 * @param {Number} scheduleId 号源ID
 */
function createOrder(scheduleId) {
  // 后端使用 @RequestParam Integer scheduleId 接收参数
  // 这里通过查询参数传递，避免 JSON Body 与 @RequestParam 不匹配
  const url = `${config.API.CREATE_ORDER}?scheduleId=${encodeURIComponent(scheduleId)}`
  return request.post(url)
}

/**
 * 查询订单详情
 * @param {String} orderNo 订单号
 */
function getOrderDetail(orderNo) {
  return request.get(config.API.GET_ORDER_DETAIL, { orderNo })
}

/**
 * 创建支付订单（立即支付）
 * @param {String} orderNo 订单号
 */
function createPayment(orderNo, payWay) {
  const body = { orderNo }
  if (payWay) body.payWay = payWay
  return request.post(config.API.CREATE_PAYMENT, body)
}

module.exports = {
  createOrder,
  getOrderDetail,
  createPayment,
  getOrderList,
  applyRefund
  ,getRefundStatus
}
