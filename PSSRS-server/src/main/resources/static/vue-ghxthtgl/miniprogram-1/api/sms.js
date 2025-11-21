// api/sms.js
// 短信验证码相关API

const request = require('../utils/request.js')
const config = require('../utils/config.js')

/**
 * 发送注册验证码
 * @param {String} phone 手机号
 */
function sendRegisterCode(phone) {
  return request.post(config.API.SEND_REGISTER_CODE + '?phone=' + phone, {}, {
    loadingText: '发送中...'
  })
}

/**
 * 发送登录验证码
 * @param {String} phone 手机号
 */
function sendLoginCode(phone) {
  return request.post(config.API.SEND_LOGIN_CODE + '?phone=' + phone, {}, {
    loadingText: '发送中...'
  })
}

/**
 * 发送重置密码验证码
 * @param {String} phone 手机号
 */
function sendResetPasswordCode(phone) {
  return request.post(config.API.SEND_RESET_PWD_CODE + '?phone=' + phone, {}, {
    loadingText: '发送中...'
  })
}

module.exports = {
  sendRegisterCode,
  sendLoginCode,
  sendResetPasswordCode
}
