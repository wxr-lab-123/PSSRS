/**
 * 排班数据传输对象
 */

/**
 * 创建排班 DTO
 * @param {Object} data - 原始数据
 * @returns {Object} 格式化后的数据
 */
export function createScheduleDTO(data) {
  return {
    doctor_id: Number(data.doctor_id),
    department_id: Number(data.department_id),
    schedule_date: data.schedule_date,
    time_slot: data.time_slot,
    start_time: data.start_time,
    end_time: data.end_time,
    max_appointments: Number(data.max_appointments) || 20,
    room_number: data.room_number || '',
    notes: data.notes || '',
    schedule_type: data.schedule_type || 'normal'
  }
}

/**
 * 批量创建排班 DTO
 * @param {Object} data - 原始数据
 * @returns {Object} 格式化后的数据
 */
export function batchCreateSchedulesDTO(data) {
  return {
    doctor_id: Number(data.doctor_id),
    department_id: Number(data.department_id),
    dates: data.dates || [],
    time_slots: data.time_slots || [],
    max_appointments: Number(data.max_appointments) || 20,
    room_number: data.room_number || '',
    schedule_type: data.schedule_type || 'normal'
  }
}

/**
 * 更新排班 DTO
 * @param {Object} data - 原始数据
 * @returns {Object} 格式化后的数据
 */
export function updateScheduleDTO(data) {
  const dto = {}
  
  if (data.doctor_id !== undefined) {
    dto.doctor_id = Number(data.doctor_id)
  }
  if (data.department_id !== undefined) {
    dto.department_id = Number(data.department_id)
  }
  if (data.schedule_date !== undefined) {
    dto.schedule_date = data.schedule_date
  }
  if (data.time_slot !== undefined) {
    dto.time_slot = data.time_slot
  }
  if (data.start_time !== undefined) {
    dto.start_time = data.start_time
  }
  if (data.end_time !== undefined) {
    dto.end_time = data.end_time
  }
  if (data.max_appointments !== undefined) {
    dto.max_appointments = Number(data.max_appointments)
  }
  if (data.room_number !== undefined) {
    dto.room_number = data.room_number
  }
  if (data.notes !== undefined) {
    dto.notes = data.notes
  }
  if (data.status !== undefined) {
    dto.status = data.status
  }
  if (data.schedule_type !== undefined) {
    dto.schedule_type = data.schedule_type
  }
  
  return dto
}

/**
 * 排班查询参数 DTO
 * @param {Object} params - 查询参数
 * @returns {Object} 格式化后的参数
 */
export function scheduleQueryDTO(params) {
  const dto = {}
  
  if (params.doctor_id) {
    dto.doctor_id = Number(params.doctor_id)
  }
  if (params.department_id) {
    dto.department_id = Number(params.department_id)
  }
  if (params.schedule_date) {
    dto.schedule_date = params.schedule_date
  }
  if (params.time_slot) {
    dto.time_slot = params.time_slot
  }
  if (params.status) {
    dto.status = params.status
  }
  if (params.schedule_type) {
    dto.schedule_type = params.schedule_type
  }
  if (params.start_date) {
    dto.start_date = params.start_date
  }
  if (params.end_date) {
    dto.end_date = params.end_date
  }
  if (params.page) {
    dto.page = Number(params.page)
  }
  if (params.limit) {
    dto.limit = Number(params.limit)
  }
  
  return dto
}

/**
 * 复制排班 DTO
 * @param {Object} data - 原始数据
 * @returns {Object} 格式化后的数据
 */
export function copyScheduleDTO(data) {
  return {
    source_schedule_id: Number(data.source_schedule_id),
    target_dates: data.target_dates || []
  }
}
