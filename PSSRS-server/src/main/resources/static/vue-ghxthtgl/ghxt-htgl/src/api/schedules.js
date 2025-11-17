import request from './request'

/**
 * 获取排班列表
 * @param {Object} params - 查询参数
 * @param {number} params.doctor_id - 医生ID
 * @param {number} params.department_id - 科室ID
 * @param {string} params.schedule_date - 排班日期
 * @param {string} params.time_slot - 时间段
 * @param {string} params.status - 状态 (AVAILABLE/FULL/CANCELLED)
 * @param {number} params.page - 页码
 * @param {number} params.limit - 每页数量
 */
export function fetchSchedules(params) {
  return request.get('/schedules', { 
    params: { 
      ...params, 
      _t: Date.now() 
    } 
  })
}

/**
 * 获取单个排班详情
 * @param {number} id - 排班ID
 */
export function fetchScheduleById(id) {
  return request.get(`/schedules/${id}`, {
    params: { _t: Date.now() }
  })
}

/**
 * 创建排班
 * @param {Object} payload - 排班数据
 * @param {number} payload.doctor_id - 医生ID
 * @param {number} payload.department_id - 科室ID
 * @param {string} payload.schedule_date - 排班日期
 * @param {string} payload.time_slot - 时间段
 * @param {string} payload.start_time - 开始时间
 * @param {string} payload.end_time - 结束时间
 * @param {number} payload.max_appointments - 最大预约数
 * @param {string} payload.room_number - 诊室号
 * @param {string} payload.notes - 备注
 */
export function createSchedule(payload) {
  return request.post('/schedules', payload)
}

/**
 * 批量创建排班
 * @param {Object} payload - 批量排班数据
 * @param {number} payload.doctor_id - 医生ID
 * @param {number} payload.department_id - 科室ID
 * @param {Array<string>} payload.dates - 日期数组
 * @param {Array<Object>} payload.time_slots - 时间段配置数组
 * @param {number} payload.max_appointments - 最大预约数
 * @param {string} payload.room_number - 诊室号
 */
export function batchCreateSchedules(payload) {
  return request.post('/schedules/batch', payload)
}

/**
 * 更新排班（请求体携带 id）
 * @param {Object} payload - 更新数据，需包含 id
 * @param {number} payload.id - 排班ID
 */
export function updateSchedule(payload) {
  return request.put('/schedules', payload)
}

/**
 * 删除排班
 * @param {number} id - 排班ID
 */
export function deleteSchedule(id) {
  return request.delete(`/schedules/${id}`)
}

/**
 * 批量删除排班
 * @param {Array<number>} ids - 排班ID数组
 */
export function batchDeleteSchedules(ids) {
  return request.post('/schedules/batch-delete', { ids })
}

/**
 * 更新排班状态
 * @param {number} id - 排班ID
 * @param {string} status - 状态 (AVAILABLE/FULL/CANCELLED)
 */
export function updateScheduleStatus(id, status) {
  return request.patch(`/schedules/${id}/status`, null, { params: { status } })
}

/**
 * 获取医生的排班日历
 * @param {number} doctorId - 医生ID
 * @param {string} startDate - 开始日期
 * @param {string} endDate - 结束日期
 */
export function fetchDoctorScheduleCalendar(doctorId, startDate, endDate) {
  return request.get(`/schedules/doctor/${doctorId}/calendar`, {
    params: { 
      start_date: startDate, 
      end_date: endDate,
      _t: Date.now()
    }
  })
}

/**
 * 获取科室的排班日历
 * @param {number} departmentId - 科室ID
 * @param {string} startDate - 开始日期
 * @param {string} endDate - 结束日期
 */
export function fetchDepartmentScheduleCalendar(departmentId, startDate, endDate) {
  return request.get(`/schedules/department/${departmentId}/calendar`, {
    params: { 
      start_date: startDate, 
      end_date: endDate,
      _t: Date.now()
    }
  })
}

/**
 * 获取可预约的排班列表
 * @param {Object} params - 查询参数
 * @param {number} params.doctor_id - 医生ID
 * @param {number} params.department_id - 科室ID
 * @param {string} params.date - 日期
 */
export function fetchAvailableSchedules(params) {
  return request.get('/schedules/available', {
    params: {
      ...params,
      _t: Date.now()
    }
  })
}

/**
 * 复制排班
 * @param {Object} payload - 复制配置
 * @param {number} payload.source_schedule_id - 源排班ID
 * @param {Array<string>} payload.target_dates - 目标日期数组
 */
export function copySchedule(payload) {
  return request.post('/schedules/copy', payload)
}

/**
 * 获取排班统计信息
 * @param {Object} params - 查询参数
 * @param {number} params.doctor_id - 医生ID
 * @param {number} params.department_id - 科室ID
 * @param {string} params.start_date - 开始日期
 * @param {string} params.end_date - 结束日期
 */
export function fetchScheduleStatistics(params) {
  return request.get('/schedules/statistics', {
    params: {
      ...params,
      _t: Date.now()
    }
  })
}
