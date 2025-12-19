import request from './request'
import axios from 'axios'

export function getProfile() {
  return request.get('/user/profile')
}

export function updateProfile(data) {
  return request.put('/user/profile', data)
}

export function changePassword(data) {
  return request.post('/user/password', data)
}

export function bindPhone(data) {
  return request.post('/user/phone/bind', data)
}

export function updateAvatar(avatar) {
  return request.put('/user/avatar', { avatar })
}

export function sendPhoneCode(phone) {
  return request.post('/user/phone/sendCode', { phone })
}
