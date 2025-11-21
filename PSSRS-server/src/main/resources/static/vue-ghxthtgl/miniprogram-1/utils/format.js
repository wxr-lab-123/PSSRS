// utils/format.js
// 数据格式化工具函数

/**
 * 手机号脱敏
 * 中间4位显示为星号
 * 例如：13800138000 -> 138****8000
 * @param {String} phone 手机号
 * @returns {String} 脱敏后的手机号
 */
function maskPhone(phone) {
  if (!phone) return ''
  const phoneStr = String(phone)
  if (phoneStr.length !== 11) return phoneStr
  return phoneStr.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

/**
 * 身份证号脱敏
 * 后6位显示为星号
 * 例如：130823201205235689 -> 130823201205******
 * @param {String} idCard 身份证号
 * @returns {String} 脱敏后的身份证号
 */
function maskIdCard(idCard) {
  if (!idCard) return ''
  const idCardStr = String(idCard)
  if (idCardStr.length < 6) return idCardStr
  return idCardStr.replace(/(\d+)\d{6}$/, '$1******')
}

/**
 * 性别代码转换为文字
 * @param {String} gender 性别代码 0-女 1-男
 * @returns {String} 性别文字
 */
function formatGender(gender) {
  if (gender === '1' || gender === 1) return '女'
  if (gender === '0' || gender === 0) return '男'
  return '未设置'
}

/**
 * 格式化日期时间
 * @param {String} dateTime 日期时间字符串
 * @returns {String} 格式化后的日期时间
 */
function formatDateTime(dateTime) {
  if (!dateTime) return ''
  // 如果是ISO格式，转换为本地格式
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}`
}

module.exports = {
  maskPhone,
  maskIdCard,
  formatGender,
  formatDateTime
}
