// utils/request.js
// HTTP请求封装

const config = require('./config.js')

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
    
    // 如果有token，添加到请求头
    if (token) {
      header['Authorization'] = token
    }
    
    // 显示loading
    if (options.showLoading !== false) {
      wx.showLoading({
        title: options.loadingText || '加载中...',
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
            handleError(res.data)
            reject(res.data)
          }
        } else if (res.statusCode === 401) {
          // 未授权，跳转到登录页
          handleUnauthorized()
          reject(res.data)
        } else {
          // 其他错误
          handleError(res.data)
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
  const message = data.message || data.msg || '请求失败'
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
    title: '网络连接失败，请检查网络',
    icon: 'none',
    duration: 2000
  })
}

/**
 * 处理未授权
 */
function handleUnauthorized() {
  wx.showToast({
    title: '请先登录',
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
