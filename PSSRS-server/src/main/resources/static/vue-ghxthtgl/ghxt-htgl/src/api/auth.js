import request from './request'

export function loginApi(payload) {
  return request.post('/auth/login', payload)
}

export function logoutApi() {
  return request.post('/auth/logout')
}


