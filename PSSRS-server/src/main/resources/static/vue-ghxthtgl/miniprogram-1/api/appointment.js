// api/appointment.js
// 预约相关API

const request = require('../utils/request.js')
const config = require('../utils/config.js')

/**
 * 创建预约
 * @param {Object} data 预约信息
 */
function createAppointment(data) {
  return request.post(config.API.CREATE_APPOINTMENT, {
    doctorId: data.doctorId,
    departmentId: data.departmentId,
    appointmentDate: data.appointmentDate,
    timeSlot: data.timeSlot,
    patientPhone: data.patientPhone
  })
}

/**
 * 获取预约列表
 * @param {Number} page 页码
 * @param {Number} pageSize 每页数量
 * @param {String} status 状态 (可选)
 */
function getAppointmentList(page = 1, pageSize = 10, status) {
  return request.get(config.API.GET_APPOINTMENT_LIST, {
    page,
    pageSize,
    status
  })
}

/**
 * 获取预约详情
 * @param {Number} id 预约ID
 */
function getAppointmentInfo(id) {
  return request.get(config.API.GET_APPOINTMENT_INFO, { id })
}

/**
 * 取消预约
 * @param {Number} id 预约ID
 */
function cancelAppointment(id) {
  return request.post(config.API.CANCEL_APPOINTMENT, { id })
}

/**
 * 预约取号
 * @param {String} registrationNo 挂号单号
 */
function takeNumber(registrationNo) {
  return request.post(config.API.TAKE_NUMBER, {
    registrationNo
  })
}

module.exports = {
  createAppointment,
  getAppointmentList,
  getAppointmentInfo,
  cancelAppointment,
  takeNumber
}
