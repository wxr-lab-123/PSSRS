import axios from 'axios'

const instance = axios.create({
  baseURL: '/api/admin',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

function genId() {
  const s4 = () => Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(1)
  return `${Date.now().toString(16)}-${s4()}-${s4()}-${s4()}`
}

const ERROR_CODE_MAP = {
  400: '参数错误',
  401: '未认证或登录已过期',
  403: '无权限',
  404: '资源不存在',
  409: '业务冲突',
  422: '参数校验失败',
  429: '请求过于频繁',
  500: '服务器错误',
  E_PERMISSION_DENIED: '无权限',
  E_SCHEDULE_FULL: '号源已满',
  E_PAYMENT_SIGNATURE_INVALID: '支付签名无效',
  E_PAYMENT_TIMEOUT: '支付超时',
  E_IDEMPOTENT_REPLAY: '重复请求',
  E_ORDER_NOT_FOUND: '订单不存在'
}

function mapMsg(code, msg) {
  const key = code == null ? undefined : (typeof code === 'number' ? code : String(code))
  const std = ERROR_CODE_MAP[key]
  if (std) return std
  return msg || '请求失败'
}

instance.interceptors.request.use((config) => {
  const token = (typeof sessionStorage !== 'undefined' ? sessionStorage.getItem('auth_token') : null) || localStorage.getItem('auth_token')
  if (token) {
    config.headers.Authorization = token
  }
  if (['post','put','delete'].includes(String(config.method).toLowerCase())) {
    const reqId = genId()
    config.headers['X-Request-Id'] = reqId
    config.headers['Idempotency-Key'] = reqId
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
        localStorage.removeItem('auth_token')
        window.location.href = '/login'
        return Promise.reject({ code: 401, msg: mapMsg(401, res.msg) })
      }
      return Promise.reject({ code: res.code, msg: mapMsg(res.code, res.msg), data: res.data })
    }
    return res
  },
  (error) => {
    const status = error?.response?.status
    if (status === 401) {
      localStorage.removeItem('auth_token')
      window.location.href = '/login'
    }
    return Promise.reject({
      httpStatus: status,
      msg: mapMsg(status, error?.response?.data?.msg || error.message),
      raw: error
    })
  }
)

export default instance


