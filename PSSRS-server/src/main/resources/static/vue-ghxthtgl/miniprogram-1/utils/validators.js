// utils/validators.js

function isNonEmptyString(str) {
  return typeof str === 'string' && str.trim().length > 0
}

function validatePhone(phone) {
  if (!phone) return false
  const normalized = String(phone).replace(/\s+/g, '')
  return /^1[3-9]\d{9}$/.test(normalized)
}

function validatePhoneIntl(phone) {
  if (!phone) return false
  const s = String(phone).replace(/\s+/g, '')
  return /^\+?[1-9]\d{1,14}$/.test(s)
}

function validatePassport(passport) {
  if (!passport) return false
  const s = String(passport).trim().toUpperCase()
  if (/^[A-Z0-9]{6,20}$/.test(s)) return true
  if (/^[A-Z]{2}\d{7,9}$/.test(s)) return true
  return false
}

function validateIdCard(idCard) {
  if (!idCard) return false
  const normalized = String(idCard).toUpperCase().replace(/\s+/g, '')
  // 粗略校验：18位，前17位数字，最后一位数字或X
  return /^\d{17}[0-9X]$/.test(normalized)
}

function validateGender(gender) {
  if (!gender) return false
  const g = String(gender)
  return ['男', '女', '0', '1', 0, 1].includes(g) || ['男', '女'].includes(g)
}

function validateAge(age) {
  if (age === undefined || age === null || age === '') return true // 非必填
  const n = Number(age)
  return Number.isInteger(n) && n >= 0 && n <= 150
}

/**
 * 校验用户信息并返回各条件判断
 * @param {Object} data
 * @param {string} [data.name]
 * @param {string|number} [data.phone]
 * @param {string} [data.idCard]
 * @param {string|number} [data.gender] // 男/女 或 0/1
 * @param {string|number} [data.age]
 * @returns {{
 *  nameFilled: boolean,
 *  phoneValid: boolean,
 *  idCardValid: boolean,
 *  genderValid: boolean,
 *  ageValid: boolean,
 *  normalized: { name: string, phone: string, idCard: string, gender: any, age: number|undefined },
 *  isValid: boolean
 * }}
 */
function validateUserInfo(data = {}) {
  const name = (data.name || '').trim()
  const phoneRaw = data.phone == null ? '' : String(data.phone)
  const phone = phoneRaw.replace(/\s+/g, '')
  const idCardRaw = data.idCard == null ? '' : String(data.idCard)
  const idCard = idCardRaw.toUpperCase().replace(/\s+/g, '')
  const gender = data.gender
  const age = data.age === '' ? undefined : data.age

  const nameFilled = isNonEmptyString(name)
  const phoneValid = validatePhone(phone)
  const idCardValid = idCard ? validateIdCard(idCard) : true // 非必填则放过
  const genderValid = gender == null || gender === '' ? true : validateGender(gender)
  const ageValid = validateAge(age)

  return {
    nameFilled,
    phoneValid,
    idCardValid,
    genderValid,
    ageValid,
    normalized: { name, phone, idCard, gender, age: age === undefined ? undefined : Number(age) },
    isValid: nameFilled && phoneValid && idCardValid && genderValid && ageValid
  }
}

module.exports = {
  validateUserInfo,
  validatePhoneIntl,
  validatePassport
}
