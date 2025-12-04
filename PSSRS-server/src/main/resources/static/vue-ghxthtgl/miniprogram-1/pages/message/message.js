const i18n = require('../../utils/i18n.js')
const request = require('../../utils/request.js')
const { API } = require('../../utils/config.js')

Page({
  data: {
    messages: [],
    refresherTriggered: false,
    page: 1,
    size: 10,
    hasMore: true,
    loading: false
  },

  onLoad() {
    i18n.init()
  },

  onShow() {
    this.loadMessages({ reset: true })
  },

  onRefresh() {
    if (this.data.refresherTriggered) return
    this.setData({ refresherTriggered: true })
    this.loadMessages({ reset: true })
  },

  async loadMessages({ reset = false } = {}) {
    if (this.data.loading) return
    if (reset) {
      this.setData({ page: 1, hasMore: true, messages: [] })
    }
    if (!this.data.hasMore && !reset) return
    this.setData({ loading: true })
    let msgs = []
    try {
      const res = await request.get(API.GET_MESSAGES, { page: this.data.page, size: this.data.size }, { showLoading: false })
      msgs = Array.isArray(res?.data?.records) ? res.data.records : (Array.isArray(res?.data?.list) ? res.data.list : (res?.data || []))
      if (Array.isArray(msgs) && msgs.length < this.data.size) {
        this.setData({ hasMore: false })
      }
    } catch (e) {
      if (reset) {
        msgs = wx.getStorageSync('messages') || []
      } else {
        msgs = []
        this.setData({ hasMore: false })
      }
    }
    const mapped = msgs.map(m => {
        let raw = {}
        try { raw = JSON.parse(m.content || '{}') } catch(e) { raw = {} }
        raw.type = raw.type || m.messageType
        raw.timestamp = raw.timestamp || m.timestamp
        raw.createTime = m.createTime || raw.createTime
        raw.status = raw.status || m.status
        raw.doctorId = raw.doctorId || m.doctorId
        raw.messageId = m.messageId

        const t = raw.type
        const isCancelled = t === 'APPOINTMENT_CANCELLED_BY_DOCTOR'
        const isCalled = t === 'ORDER_CALLED' || t === 'ORDER_RECALLED' || t === 'CALL_PATIENT'

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

        if (isCalled) {
          let dep = raw.departmentName || ''
          let docName = raw.doctorName || ''
          let room = raw.roomNumber || ''
          let reg = raw.registrationNo || ''
          if (!dep && !docName && !room && !reg && m.content) {
            const parts = String(m.content).split(',').map(s=>s.trim())
            const norm = (x)=>String(x||'').replace(/^科室[：: ]?/,'').replace(/^医生[：: ]?/,'').replace(/^诊室[：: ]?/,'').replace(/^挂号号[：: ]?/,'')
            dep = norm(parts[0] || dep)
            docName = norm(parts[1] || docName)
            room = norm(parts[2] || room)
            reg = norm(parts[3] || reg)
          }
          raw.departmentName = dep
          raw.doctorName = docName
          raw.roomNumber = room
          raw.registrationNo = reg
          const isRecall = t === 'ORDER_RECALLED'
          const title = isRecall ? '重叫通知' : '叫号通知'
          const subtitle = `科室 ${dep}，医生 ${docName}，诊室 ${room}，挂号号 ${reg}`
          const statusText = isRecall ? '重叫' : '已叫号'
          return {
            id: m.messageId || m.id || `${reg || ''}-${raw.timestamp || ''}`,
            title,
            subtitle,
            statusText,
            statusClass: isRecall ? 'recall' : 'called',
            timeText: (raw.timeText || raw.timestamp || raw.createTime || m.createTime || ''),
            raw,
            read: String(m.status || raw.read) === '已读'
          }
        }

        const approved = String(raw.status) === 'APPROVED'
        const statusText = approved ? '已批准' : (String(raw.status) === 'REJECTED' ? '已拒绝' : '已更新')
        const isLeave = (raw.type || '').startsWith('LEAVE') || raw.type === 'LEAVE_APPROVAL_RESULT' || raw.type === 'LEAVE_STATUS_UPDATE'
        if (isLeave) {
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
        }
        return {
          id: m.messageId || m.id || `${raw.scheduleId || ''}-${raw.timestamp || ''}`,
          title: '系统通知',
          subtitle: (m.content || ''),
          statusText: '已更新',
          statusClass: 'updated',
          timeText: (raw.timeText || raw.timestamp || raw.createTime || m.createTime || ''),
          raw,
          read: String(m.status || raw.read) === '已读'
        }
      })
    const map = new Map(this.data.messages.map(i => [i.id, i]))
    mapped.forEach(i => map.set(i.id, i))
    const list = Array.from(map.values()).sort((a,b)=> String(b.raw.timestamp||b.raw.createTime||'').localeCompare(String(a.raw.timestamp||a.raw.createTime||'')))
    const nextPage = this.data.page + (Array.isArray(msgs) && msgs.length > 0 ? 1 : 0)
    this.setData({ messages: list, refresherTriggered: false, loading: false, page: nextPage })
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
    } else if (item.raw.type === 'ORDER_CALLED' || item.raw.type === 'ORDER_RECALLED' || item.raw.type === 'CALL_PATIENT') {
      const content = `科室：${item.raw.departmentName || ''}\n医生：${item.raw.doctorName || ''}\n诊室：${item.raw.roomNumber || ''}\n挂号号：${item.raw.registrationNo || ''}`
      wx.showModal({ title: item.statusText, content, confirmText: '知道了', showCancel: false })
    } else {
      const content = `排班：${item.raw.scheduleId || ''}\n医生：${item.raw.doctorId || ''}\n状态：${item.statusText}\n理由：${item.raw.reason || ''}`
      wx.showModal({ title: '审批结果', content, confirmText: '知道了', showCancel: false })
    }
    this.loadMessages({ reset: true })
  },

  onScrollToLower() {
    if (this.data.loading || !this.data.hasMore) return
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
