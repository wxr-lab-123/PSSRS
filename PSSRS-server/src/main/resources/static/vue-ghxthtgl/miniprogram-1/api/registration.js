// api/registration.js
// 挂号相关API

const request = require('../utils/request.js')
const config = require('../utils/config.js')

/**
 * 创建挂号
 * @param {Object} data 挂号信息
 */
function createRegistration(data) {
  return request.post(config.API.CREATE_REGISTRATION, {
    doctorId: data.doctorId,
    departmentId: data.departmentId,
    registrationDate: data.registrationDate,
    timeSlot: data.timeSlot,
    scheduleType: data.scheduleType,
    scheduleId: data.scheduleId,
    patientPhone: data.patientPhone
  })
}

/**
 * 获取挂号列表
 * @param {Number} page 页码
 * @param {Number} pageSize 每页数量
 * @param {String} status 状态 (可选)
 */
function getRegistrationList(page = 1, pageSize = 10, status) {
  return request.get(config.API.GET_REGISTRATION_LIST, {
    page,
    pageSize,
    status
  })
}

/**
 * 获取挂号详情
 * @param {Number} id 挂号ID
 */
function getRegistrationInfo(id) {
  return request.get(config.API.GET_REGISTRATION_INFO, { id })
}

/**
 * 取消挂号
 * @param {Number} id 预约ID
 * @param {String} orderNo 订单号
 */
function cancelRegistration(id, orderNo) {
  const data = {
    id,
    orderNo
  }
  console.log('取消挂号参数:', data)
  return request.post(config.API.CANCEL_REGISTRATION, data)
}

module.exports = {
  createRegistration,
  getRegistrationList,
  getRegistrationInfo,
  cancelRegistration
}
