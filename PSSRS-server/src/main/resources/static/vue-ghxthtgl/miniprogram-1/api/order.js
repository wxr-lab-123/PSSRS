// api/order.js
// 订单相关API

const request = require('../utils/request.js')
const config = require('../utils/config.js')

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
function createPayment(orderNo) {
  return request.post(config.API.CREATE_PAYMENT, { orderNo })
}

module.exports = {
  getOrderDetail,
  createPayment
}
