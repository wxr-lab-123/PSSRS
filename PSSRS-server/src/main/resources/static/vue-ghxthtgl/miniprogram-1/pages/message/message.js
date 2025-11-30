const i18n = require('../../utils/i18n.js')
const request = require('../../utils/request.js')
const { API } = require('../../utils/config.js')

Page({
  data: {
    messages: [],
    refresherTriggered: false
  },

  onLoad() {
    i18n.init()
  },

  onShow() {
    this.loadMessages()
  },

  onRefresh() {
    if (this.data.refresherTriggered) return
    this.setData({ refresherTriggered: true })
    this.loadMessages()
  },

  async loadMessages() {
    let msgs = []
    try {
      const res = await request.get(API.GET_MESSAGES, { page: 1, size: 50 }, { showLoading: false })
      msgs = Array.isArray(res?.data?.records) ? res.data.records : (Array.isArray(res?.data?.list) ? res.data.list : (res?.data || []))
    } catch (e) {
      msgs = wx.getStorageSync('messages') || []
    }
    const list = msgs.map(m => {
        let raw = {}
        try { raw = JSON.parse(m.content || '{}') } catch(e) { raw = {} }
        raw.type = raw.type || m.messageType
        raw.timestamp = raw.timestamp || m.timestamp
        raw.createTime = m.createTime || raw.createTime
        raw.status = raw.status || m.status
        raw.doctorId = raw.doctorId || m.doctorId
        raw.messageId = m.messageId
        const isCancelled = raw.type === 'APPOINTMENT_CANCELLED_BY_DOCTOR'
        if (isCancelled) {
          const subtitleParts = []
          if (raw.scheduleDate) subtitleParts.push(`时间 ${raw.scheduleDate}`)
          if (raw.timeSlot) subtitleParts.push(raw.timeSlot)
          const range = (raw.startTime && raw.endTime) ? `${raw.startTime}-${raw.endTime}` : ''
          if (range) subtitleParts.push(range)
          if (raw.doctorName) subtitleParts.push(`医生 ${raw.doctorName}`)
          return {
            id: m.messageId || m.id || `${raw.scheduleId || ''}-${raw.timestamp || ''}`,
            title: raw.message || '你挂的号由于医生原因已取消',
            subtitle: subtitleParts.join(' '),
            statusText: '已取消',
            statusClass: 'cancelled',
            timeText: (raw.timeText || raw.timestamp || raw.createTime || m.createTime || ''),
            raw,
            read: String(m.status || raw.read) === '已读'
          }
        }
        const approved = String(raw.status) === 'APPROVED'
        const statusText = approved ? '已批准' : (String(raw.status) === 'REJECTED' ? '已拒绝' : '已更新')
        return {
          id: m.messageId || m.id || `${raw.scheduleId || ''}-${raw.timestamp || ''}`,
          title: '请假审批结果',
          subtitle: `排班 ${raw.scheduleId || ''}，医生 ${raw.doctorId || ''}`,
          statusText,
          statusClass: approved ? 'approved' : 'rejected',
          timeText: (raw.timeText || raw.timestamp || raw.createTime || m.createTime || ''),
          raw,
          read: String(m.status || raw.read) === '已读'
        }
      }).sort((a,b)=> String(b.raw.timestamp||b.raw.createTime||'').localeCompare(String(a.raw.timestamp||a.raw.createTime||'')))
    this.setData({ messages: list, refresherTriggered: false })
    try {
      const cnt = await request.get(API.UNREAD_COUNT, {}, { showLoading: false })
      const unread = Number(cnt?.data || 0)
      if (unread > 0) wx.setTabBarBadge({ index: 1, text: String(unread) })
      else wx.removeTabBarBadge({ index: 1 })
    } catch(e) {}
  },

  async openMsg(e) {
    const item = e.currentTarget.dataset.item
    const msgId = (item.raw && item.raw.messageId) || item.messageId
    if (msgId) {
      try { await request.put(API.MARK_MESSAGE_READ(msgId), {}, { showLoading: false }) } catch(e) {}
    }
    const isCancelled = item.raw.type === 'APPOINTMENT_CANCELLED_BY_DOCTOR'
    if (isCancelled) {
      const content = `${item.raw.message || '你挂的号由于医生原因已取消'}\n时间：${item.raw.scheduleDate || ''} ${item.raw.timeSlot || ''} ${(item.raw.startTime||'')}-${(item.raw.endTime||'')}\n医生：${item.raw.doctorName || ''}`
      wx.showModal({ title: '号源取消通知', content, confirmText: '知道了', showCancel: false })
    } else {
      const content = `排班：${item.raw.scheduleId || ''}\n医生：${item.raw.doctorId || ''}\n状态：${item.statusText}\n理由：${item.raw.reason || ''}`
      wx.showModal({ title: '审批结果', content, confirmText: '知道了', showCancel: false })
    }
    this.loadMessages()
  },

  clearAll() {
    wx.showModal({
      title: '清空消息',
      content: '确定清空所有消息吗？',
      success: (res) => {
        if (res.confirm) {
          wx.removeStorageSync('messages')
          this.loadMessages()
        }
      }
    })
  }
})
