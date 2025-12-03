// utils/request.js
// HTTP请求封装

const config = require('./config.js')
const i18n = require('./i18n.js')
const T = (k) => (i18n && typeof i18n.t === 'function') ? i18n.t(k) : k

const ERROR_CODE_MAP = {
  400: '参数错误',
  401: '未登录或登录过期',
  403: '无权限',
  404: '资源不存在',
  409: '业务冲突',
  422: '参数校验失败',
  429: '请求过于频繁',
  500: '服务器错误',
  E_SCHEDULE_FULL: '号源已满',
  E_PAYMENT_SIGNATURE_INVALID: '支付签名无效',
  E_PAYMENT_TIMEOUT: '支付超时',
  E_IDEMPOTENT_REPLAY: '重复请求',
  E_ORDER_NOT_FOUND: '订单不存在'
}

function genId() {
  const s4 = () => Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(1)
  return `${Date.now().toString(16)}-${s4()}-${s4()}-${s4()}`
}

function mapMsg(code, msg) {
  const key = code == null ? undefined : (typeof code === 'number' ? code : String(code))
  return ERROR_CODE_MAP[key] || msg || T('common.requestFailed')
}

/**
 * 封装的HTTP请求方法
 * @param {Object} options 请求配置
 * @returns {Promise}
 */
function request(options) {
  return new Promise((resolve, reject) => {
    // 获取token
    const token = wx.getStorageSync('token') || ''
    
    // 构建完整URL
    const url = config.baseURL + options.url
    
    // 构建请求头
    const header = {
      'Content-Type': 'application/json',
      ...options.header
    }
    const method = (options.method || 'GET').toUpperCase()
    if (method === 'POST' || method === 'PUT' || method === 'DELETE') {
      const reqId = genId()
      header['X-Request-Id'] = reqId
      header['Idempotency-Key'] = reqId
    }
    
    // 如果有token，添加到请求头
    if (token) {
      header['Authorization'] = token
    }
    
    // 显示loading
    if (options.showLoading !== false) {
      wx.showLoading({
        title: options.loadingText || T('common.loading'),
        mask: true
      })
    }
    
    // 发起请求
    wx.request({
      url: url,
      method: options.method || 'GET',
      data: options.data || {},
      header: header,
      timeout: options.timeout || config.timeout,
      success: (res) => {
        wx.hideLoading()
        
        // 根据状态码处理
        if (res.statusCode === 200) {
          // 业务逻辑成功
          if (res.data.code === 200 || res.data.code === 0) {
            resolve(res.data)
          } else {
            // 业务逻辑失败
            res.data.message = mapMsg(res.data.code, res.data.message || res.data.msg)
            handleError(res.data)
            reject(res.data)
          }
        } else if (res.statusCode === 401) {
          // 未授权，跳转到登录页
          handleUnauthorized()
          reject(res.data)
        } else {
          // 其他错误
          const data = res.data || {}
          data.message = mapMsg(res.statusCode, data.message || data.msg)
          handleError(data)
          reject(res.data)
        }
      },
      fail: (err) => {
        wx.hideLoading()
        handleNetworkError(err)
        reject(err)
      }
    })
  })
}

/**
 * 处理业务错误
 */
function handleError(data) {
  const message = data.message || data.msg || T('common.requestFailed')
  wx.showToast({
    title: message,
    icon: 'none',
    duration: 2000
  })
}

/**
 * 处理网络错误
 */
function handleNetworkError(err) {
  console.error('网络请求失败:', err)
  wx.showToast({
    title: T('common.networkError'),
    icon: 'none',
    duration: 2000
  })
}

/**
 * 处理未授权
 */
function handleUnauthorized() {
  wx.showToast({
    title: T('common.unauthorized'),
    icon: 'none',
    duration: 2000
  })
  
  // 清除本地存储
  wx.removeStorageSync('token')
  wx.removeStorageSync('userInfo')
  
  // 跳转到登录页
  setTimeout(() => {
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1]
    const currentRoute = currentPage.route
    
    // 如果当前不是登录页，则跳转到登录页
    if (currentRoute !== 'pages/login/login') {
      wx.navigateTo({
        url: '/pages/login/login?from=' + currentRoute
      })
    }
  }, 2000)
}

/**
 * GET请求
 */
function get(url, data = {}, options = {}) {
  return request({
    url: url,
    method: 'GET',
    data: data,
    ...options
  })
}

/**
 * POST请求
 */
function post(url, data = {}, options = {}) {
  return request({
    url: url,
    method: 'POST',
    data: data,
    ...options
  })
}

/**
 * PUT请求
 */
function put(url, data = {}, options = {}) {
  return request({
    url: url,
    method: 'PUT',
    data: data,
    ...options
  })
}

/**
 * DELETE请求
 */
function del(url, data = {}, options = {}) {
  return request({
    url: url,
    method: 'DELETE',
    data: data,
    ...options
  })
}

/**
 * 文件上传
 */
function upload(url, filePath, formData = {}, options = {}) {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('token') || ''
    
    wx.showLoading({
      title: '上传中...',
      mask: true
    })
    
    wx.uploadFile({
      url: config.baseURL + url,
      filePath: filePath,
      name: 'file',
      formData: formData,
      header: {
        'Authorization': token
      },
      success: (res) => {
        wx.hideLoading()
        const data = JSON.parse(res.data)
        if (data.code === 200 || data.code === 0) {
          resolve(data)
        } else {
          handleError(data)
          reject(data)
        }
      },
      fail: (err) => {
        wx.hideLoading()
        handleNetworkError(err)
        reject(err)
      }
    })
  })
}

module.exports = {
  request,
  get,
  post,
  put,
  del,
  upload
}
