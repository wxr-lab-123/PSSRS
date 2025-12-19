// api/user.js
// 用户相关API

const request = require('../utils/request.js')
const config = require('../utils/config.js')

/**
 * 用户登录
 * @param {String} phone 手机号
 * @param {String} password 密码
 */
function login(phone, password) {
  return request.post(config.API.LOGIN, {
    phone: phone,
    password: password
  })
}

/**
 * 微信登录
 * @param {String} code 微信登录凭证
 */
function wechatLogin(code) {
  return request.post(config.API.WECHAT_LOGIN, {
    code: code
  })
}

/**
 * 用户注册
 * @param {Object} data 注册信息
 */
function register(data) {
  return request.post(config.API.REGISTER, {
    name: data.name,
    phone: data.phone,
    code: data.code,
    idCard: data.idCard,
    password: data.password,
    confirmPassword: data.confirmPassword
  })
}

/**
 * 退出登录
 */
function logout() {
  return request.post(config.API.LOGOUT)
}

/**
 * 获取用户信息
 */
function getUserInfo() {
  return request.get(config.API.GET_USER_INFO)
}

/**
 * 更新用户信息
 * @param {Object} data 用户信息
 */
function updateUserInfo(data) {
  return request.put(config.API.UPDATE_USER_INFO, data)
}

function updateUserProfile(data) {
  return request.put(config.API.UPDATE_USER_PROFILE, data)
}

/**
 * 后端检测用户信息是否完善
 * 后端根据当前登录态识别用户，不需要传 id
 * 期望返回：{ code: 0|200, data: { isValid: boolean, nameFilled: boolean, phoneValid: boolean, normalized: {...} } }
 */
function checkUserInfo() {
  return request.get(config.API.CHECK_USER_INFO)
}

/**
 * 修改密码
 * @param {Object} data 密码信息
 * @param {String} data.oldPassword 旧密码
 * @param {String} data.newPassword 新密码
 */
function changePassword(data) {
  return request.post(config.API.CHANGE_PASSWORD, {
    oldPassword: data.oldPassword,
    newPassword: data.newPassword
  })
}

/**
 * 重置密码
 * @param {Object} data 重置密码信息
 * @param {String} data.phone 手机号
 * @param {String} data.code 验证码
 * @param {String} data.newPassword 新密码
 * @param {String} data.confirmPassword 确认密码
 */
function resetPassword(data) {
  return request.post(config.API.RESET_PASSWORD, {
    phone: data.phone,
    code: data.code,
    newPassword: data.newPassword,
    confirmPassword: data.confirmPassword
  })
}

module.exports = {
  login,
  wechatLogin,
  register,
  logout,
  getUserInfo,
  updateUserInfo,
  updateUserProfile,
  checkUserInfo,
  changePassword,
  resetPassword,
  listPatients: () => request.get(config.API.USER_PATIENTS),
  addPatient: (data) => request.post(config.API.USER_PATIENTS, data),
  updatePatient: (id, data) => request.put(`${config.API.USER_PATIENTS}/${id}`, data),
  deletePatient: (id) => request.del(`${config.API.USER_PATIENTS}/${id}`),
  switchPatient: (patientId) => request.post(config.API.SWITCH_PATIENT, { patientId })
}
