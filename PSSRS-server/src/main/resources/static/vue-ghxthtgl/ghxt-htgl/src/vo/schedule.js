/**
 * 排班视图对象
 */

/**
 * 格式化排班状态
 * @param {string} status - 状态值
 * @returns {Object} 状态显示信息
 */
export function formatScheduleStatus(status) {
  const statusMap = {
    'AVAILABLE': { label: '可预约', type: 'success', color: '#67C23A' },
    'FULL': { label: '已满', type: 'warning', color: '#E6A23C' },
    'CANCELLED': { label: '已取消', type: 'info', color: '#909399' }
  }
  return statusMap[status] || { label: '未知', type: 'info', color: '#909399' }
}

/**
 * 格式化时间段
 * @param {string} timeSlot - 时间段
 * @returns {Object} 时间段显示信息
 */
export function formatTimeSlot(timeSlot) {
  const slotMap = {
    '上午': { label: '上午', icon: '☀️', period: 'morning' },
    '下午': { label: '下午', icon: '🌤️', period: 'afternoon' },
    '晚上': { label: '晚上', icon: '🌙', period: 'evening' }
  }
  return slotMap[timeSlot] || { label: timeSlot, icon: '⏰', period: 'unknown' }
}

/**
 * 排班列表 VO
 * @param {Object} schedule - 排班数据
 * @returns {Object} 格式化后的排班数据
 */
export function scheduleListVO(schedule) {
  return {
    id: schedule.id,
    doctor_id: schedule.doctor_id,
    doctor_name: schedule.doctor_name || '',
    department_id: schedule.department_id,
    department_name: schedule.department_name || '',
    schedule_date: schedule.schedule_date,
    time_slot: schedule.time_slot,
    time_slot_info: formatTimeSlot(schedule.time_slot),
    start_time: schedule.start_time,
    end_time: schedule.end_time,
    time_range: `${schedule.start_time} - ${schedule.end_time}`,
    max_appointments: schedule.max_appointments,
    current_appointments: schedule.current_appointments || 0,
    available_appointments: schedule.available_appointments || schedule.max_appointments,
    status: schedule.status,
    status_info: formatScheduleStatus(schedule.status),
    schedule_type: schedule.schedule_type || schedule.scheduleType || 'normal',
    room_number: schedule.room_number || '',
    notes: schedule.notes || '',
    created_at: schedule.created_at,
    updated_at: schedule.updated_at,
    // 计算预约进度百分比
    appointment_progress: schedule.max_appointments > 0 
      ? Math.round((schedule.current_appointments || 0) / schedule.max_appointments * 100)
      : 0,
    // 是否可预约
    is_available: schedule.status === 'AVAILABLE' && 
                  (schedule.available_appointments || schedule.max_appointments) > 0
  }
}

/**
 * 排班详情 VO
 * @param {Object} schedule - 排班数据
 * @returns {Object} 格式化后的排班详情
 */
export function scheduleDetailVO(schedule) {
  return {
    ...scheduleListVO(schedule),
    created_by: schedule.created_by,
    updated_by: schedule.updated_by,
    created_by_name: schedule.created_by_name || '',
    updated_by_name: schedule.updated_by_name || ''
  }
}

/**
 * 排班日历 VO
 * @param {Array} schedules - 排班数据数组
 * @returns {Object} 按日期分组的排班数据
 */
export function scheduleCalendarVO(schedules) {
  const calendar = {}
  
  schedules.forEach(schedule => {
    const date = schedule.schedule_date
    if (!calendar[date]) {
      calendar[date] = {
        date: date,
        schedules: [],
        total_appointments: 0,
        available_appointments: 0
      }
    }
    
    const formattedSchedule = scheduleListVO(schedule)
    calendar[date].schedules.push(formattedSchedule)
    calendar[date].total_appointments += schedule.max_appointments || 0
    calendar[date].available_appointments += schedule.available_appointments || 0
  })
  
  return calendar
}

/**
 * 排班统计 VO
 * @param {Object} statistics - 统计数据
 * @returns {Object} 格式化后的统计数据
 */
export function scheduleStatisticsVO(statistics) {
  return {
    total_schedules: statistics.total_schedules || 0,
    available_schedules: statistics.available_schedules || 0,
    full_schedules: statistics.full_schedules || 0,
    cancelled_schedules: statistics.cancelled_schedules || 0,
    total_appointments: statistics.total_appointments || 0,
    current_appointments: statistics.current_appointments || 0,
    available_appointments: statistics.available_appointments || 0,
    appointment_rate: statistics.total_appointments > 0
      ? Math.round((statistics.current_appointments || 0) / statistics.total_appointments * 100)
      : 0
  }
}

/**
 * 时间段选项
 */
export const TIME_SLOT_OPTIONS = [
  { value: '上午', label: '上午', icon: '☀️' },
  { value: '下午', label: '下午', icon: '🌤️' }
]

/**
 * 状态选项
 */
export const STATUS_OPTIONS = [
  { value: 'AVAILABLE', label: '可预约', type: 'success' },
  { value: 'FULL', label: '已满', type: 'warning' },
  { value: 'CANCELLED', label: '已取消', type: 'info' }
]
