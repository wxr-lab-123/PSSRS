// api/department.js
// 科室相关API

const request = require('../utils/request.js')
const config = require('../utils/config.js')

/**
 * 获取科室列表
 */
function getDepartments() {
  return request.get(config.API.GET_DEPARTMENTS)
}

/**
 * 获取科室详情
 * @param {Number} id 科室ID
 */
function getDepartmentInfo(id) {
  return request.get(config.API.GET_DEPARTMENT_INFO, { id })
}

module.exports = {
  getDepartments,
  getDepartmentInfo
}
