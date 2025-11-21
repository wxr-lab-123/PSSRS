// api/query.js
// 查询相关API

const request = require('../utils/request.js')
const config = require('../utils/config.js')

/**
 * 查询挂号记录（不分页）
 */
function getRegistrationRecords() {
  return request.get(config.API.GET_REGISTRATION_RECORDS)
}

/**
 * 查询缴费记录
 * @param {Number} page 页码
 * @param {Number} pageSize 每页数量
 */
function getPaymentRecords(page = 1, pageSize = 10) {
  return request.get(config.API.GET_PAYMENT_RECORDS, {
    page,
    pageSize
  })
}

/**
 * 查询检查报告列表
 * @param {Number} page 页码
 * @param {Number} pageSize 每页数量
 */
function getReportList(page = 1, pageSize = 10) {
  return request.get(config.API.GET_REPORT_LIST, {
    page,
    pageSize
  })
}

/**
 * 查询报告详情
 * @param {Number} id 报告ID
 */
function getReportDetail(id) {
  return request.get(config.API.GET_REPORT_DETAIL, { id })
}

module.exports = {
  getRegistrationRecords,
  getPaymentRecords,
  getReportList,
  getReportDetail
}
