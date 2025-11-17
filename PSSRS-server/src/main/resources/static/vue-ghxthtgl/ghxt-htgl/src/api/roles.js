import request from './request'

export function fetchRoles() {
  return request.get('/roles')
}


