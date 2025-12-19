import request from './request'

export function fetchStaffMessages(params) {
  return request.get('/messages', { params })
}

export function countUnread() {
  return request.get('/messages/unread-count')
}

export function markRead(id) {
  const sid = encodeURIComponent(String(id))
  return request.put(`/messages/${sid}/read`)
}

export function createAnnouncement(payload) {
  return request.post('/messages/announce', payload)
}

export function reportArrival(payload) {
  return request.post('/patient-arrival', payload)
}
