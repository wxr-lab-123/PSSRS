/**
 * 日期时间格式化工具
 */

/**
 * 格式化日期时间
 * @param {string|Date|number} date - 日期时间（支持字符串、Date对象、时间戳）
 * @param {string} format - 格式化模板，默认 'YYYY-MM-DD HH:mm:ss'
 * @returns {string} 格式化后的日期时间字符串
 */
export function formatDateTime(date, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!date) return '-'
  
  let d
  // 处理数组格式：[2025,11,1,15,26,43]
  if (Array.isArray(date)) {
    const parts = date.map(v => parseInt(v))
    if (parts.length >= 6) {
      d = new Date(parts[0], parts[1] - 1, parts[2], parts[3] || 0, parts[4] || 0, parts[5] || 0)
    } else if (parts.length >= 3) {
      d = new Date(parts[0], parts[1] - 1, parts[2])
    } else {
      return '-'
    }
  } else if (typeof date === 'string') {
    // 处理多种字符串格式
    // 如果包含逗号分隔的数字格式：2025,11,1,15,26,43
    if (date.includes(',')) {
      const parts = date.split(',').map(s => parseInt(s.trim()))
      if (parts.length >= 6) {
        d = new Date(parts[0], parts[1] - 1, parts[2], parts[3] || 0, parts[4] || 0, parts[5] || 0)
      } else if (parts.length >= 3) {
        d = new Date(parts[0], parts[1] - 1, parts[2])
      } else {
        d = new Date(date)
      }
    } else {
      d = new Date(date)
    }
  } else if (typeof date === 'number') {
    d = new Date(date)
  } else if (date instanceof Date) {
    d = date
  } else {
    // 尝试转换为字符串再解析
    try {
      d = new Date(String(date))
    } catch (e) {
      console.warn('无法解析日期:', date)
      return '-'
    }
  }
  
  // 检查是否为有效的 Date 对象
  if (!(d instanceof Date) || isNaN(d.getTime())) {
    return '-'
  }
  
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  
  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 格式化日期（仅日期，不含时间）
 * @param {string|Date|number} date - 日期
 * @returns {string} 格式化后的日期字符串 YYYY-MM-DD
 */
export function formatDate(date) {
  return formatDateTime(date, 'YYYY-MM-DD')
}

/**
 * 格式化时间（仅时间，不含日期）
 * @param {string|Date|number} date - 时间
 * @returns {string} 格式化后的时间字符串 HH:mm:ss
 */
export function formatTime(date) {
  return formatDateTime(date, 'HH:mm:ss')
}

/**
 * 相对时间（如：刚刚、1分钟前）
 * @param {string|Date|number} date - 日期时间
 * @returns {string} 相对时间字符串
 */
export function formatRelativeTime(date) {
  if (!date) return '-'
  
  let d
  // 处理数组格式：[2025,11,1,15,26,43]
  if (Array.isArray(date)) {
    const parts = date.map(v => parseInt(v))
    if (parts.length >= 6) {
      d = new Date(parts[0], parts[1] - 1, parts[2], parts[3] || 0, parts[4] || 0, parts[5] || 0)
    } else if (parts.length >= 3) {
      d = new Date(parts[0], parts[1] - 1, parts[2])
    } else {
      return '-'
    }
  } else if (typeof date === 'string') {
    if (date.includes(',')) {
      const parts = date.split(',').map(s => parseInt(s.trim()))
      if (parts.length >= 6) {
        d = new Date(parts[0], parts[1] - 1, parts[2], parts[3] || 0, parts[4] || 0, parts[5] || 0)
      } else if (parts.length >= 3) {
        d = new Date(parts[0], parts[1] - 1, parts[2])
      } else {
        d = new Date(date)
      }
    } else {
      d = new Date(date)
    }
  } else if (typeof date === 'number') {
    d = new Date(date)
  } else if (date instanceof Date) {
    d = date
  } else {
    try {
      d = new Date(String(date))
    } catch (e) {
      return '-'
    }
  }
  
  if (!(d instanceof Date) || isNaN(d.getTime())) return '-'
  
  const now = new Date()
  const diff = now - d
  
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  
  if (diff < minute) return '刚刚'
  if (diff < hour) return `${Math.floor(diff / minute)}分钟前`
  if (diff < day) return `${Math.floor(diff / hour)}小时前`
  if (diff < 7 * day) return `${Math.floor(diff / day)}天前`
  
  return formatDate(d)
}

