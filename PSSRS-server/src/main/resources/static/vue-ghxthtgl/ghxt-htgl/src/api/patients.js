import request from './request'

export function fetchPatients(params) {
  return request.get('/patients', { params })
}

export function fetchPatientDetail(id) {
  return request.get(`/patients/${id}`)
}

export function createPatient(payload) {
  return request.post('/patients', payload)
}

export function updatePatient(id, payload) {
  return request.put(`/patients/${id}`, payload)
}

export function deletePatient(id) {
  return request.delete(`/patients/${id}`)
}


