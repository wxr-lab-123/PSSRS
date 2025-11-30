const locales = require('./locales.js')
let lang = ''
function detect() {
  const l = String((wx.getSystemInfoSync().language || '')).toLowerCase()
  if (l.startsWith('zh')) return 'zh'
  if (l.startsWith('en')) return 'en'
  return 'en'
}
function init() {
  lang = wx.getStorageSync('lang') || detect()
  wx.setStorageSync('lang', lang)
}
function setLang(l) {
  lang = l || 'en'
  wx.setStorageSync('lang', lang)
}
function getLang() {
  return lang || detect()
}
function get(obj, path) {
  return path.split('.').reduce((o, k) => (o && o[k] != null) ? o[k] : undefined, obj)
}
function t(key) {
  const bundle = locales[getLang()] || locales.en
  return get(bundle, key) || key
}
function getBundle() {
  return locales[getLang()] || locales.en
}
module.exports = { init, setLang, getLang, t, getBundle }