import axios from 'axios'

const instance = axios.create({
  baseURL: '/api/admin',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

instance.interceptors.request.use((config) => {
  const token = (typeof sessionStorage !== 'undefined' ? sessionStorage.getItem('auth_token') : null) || localStorage.getItem('auth_token')
  if (token) {
    config.headers.Authorization = token
  }
  // 禁用 GET 请求缓存
  if (config.method === 'get') {
    config.headers['Cache-Control'] = 'no-cache'
    config.headers['Pragma'] = 'no-cache'
  }
  return config
})

instance.interceptors.response.use(
  (response) => {
    const res = response.data
    console.log('API 响应:', res)
    if (typeof res === 'object' && res !== null && 'code' in res) {
      if (res.code === 0) {
        console.log('API 返回 data:', res.data)
        return res.data
      }
      if (res.code === 401) {
        // 未认证，跳转登录
        localStorage.removeItem('auth_token')
        window.location.href = '/login'
        return Promise.reject({ code: 401, msg: res.msg || '未认证' })
      }
      // 业务错误：不抛 new Error，保留业务字段
      return Promise.reject({ code: res.code, msg: res.msg, data: res.data })
    }
    return res
  },
  (error) => {
    const status = error?.response?.status
    if (status === 401) {
      localStorage.removeItem('auth_token')
      window.location.href = '/login'
    }
    // HTTP/网络错误：向外抛出包含状态与消息的对象
    return Promise.reject({
      httpStatus: status,
      msg: error?.response?.data?.msg || error.message || '网络错误',
      raw: error
    })
  }
)

export default instance


