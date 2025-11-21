// api/doctor.js
// 医生相关API

const request = require('../utils/request.js')
const config = require('../utils/config.js')

/**
 * 获取医生列表
 * @param {Number} departmentId 科室ID
 * @param {String} date 日期 (可选)
 */
function getDoctors(departmentId, date) {
  return request.get(config.API.GET_DOCTORS, {
    departmentId,
    date
  })
}

/**
 * 获取医生详情
 * @param {Number} id 医生ID
 */
function getDoctorInfo(id) {
  return request.get(config.API.GET_DOCTOR_INFO, { id })
}

/**
 * 获取医生排班
 * @param {Number} doctorId 医生ID
 * @param {String} startDate 开始日期
 * @param {String} endDate 结束日期
 */
function getDoctorSchedule(doctorId, startDate, endDate) {
  return request.get(config.API.GET_DOCTOR_SCHEDULE, {
    doctorId,
    startDate,
    endDate
  })
}

/**
 * 获取排班详情
 * @param {Number} scheduleId 排班ID
 */
function getScheduleDetail(scheduleId) {
  return request.get(config.API.GET_SCHEDULE_DETAIL, {
    id: scheduleId
  })
}

module.exports = {
  getDoctors,
  getDoctorInfo,
  getDoctorSchedule,
  getScheduleDetail
}
