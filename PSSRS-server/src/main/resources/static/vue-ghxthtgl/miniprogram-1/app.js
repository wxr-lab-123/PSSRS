const tryRequire = (p) => { try { return require(p) } catch (e) { return null } }
const raw = tryRequire('./utils/i18n.js') || tryRequire('utils/i18n.js')
function createRuntime(locales){
  let lang = ''
  const detect = () => {
    const l = String((wx.getSystemInfoSync().language || '')).toLowerCase()
    if (l.startsWith('zh')) return 'zh'
    if (l.startsWith('en')) return 'en'
    return 'en'
  }
  const init = () => { lang = wx.getStorageSync('lang') || detect(); wx.setStorageSync('lang', lang) }
  const setLang = (l) => { lang = l || 'en'; wx.setStorageSync('lang', lang) }
  const getLang = () => lang || detect()
  const get = (obj, path) => path.split('.').reduce((o,k)=> (o && o[k]!=null)?o[k]:undefined, obj)
  const t = (key) => { const b = locales[getLang()] || locales.en; return get(b, key) || key }
  const getBundle = () => locales[getLang()] || locales.en
  return { init, setLang, getLang, t, getBundle }
}
let i18n = raw
if (!i18n || typeof i18n.init !== 'function') {
  const locales = (raw && (raw.zh || raw.en)) ? raw : (tryRequire('./utils/locales.js') || tryRequire('utils/locales.js') || {})
  i18n = createRuntime(locales)
}
const cfg = tryRequire('./utils/config.js') || tryRequire('utils/config.js')
function toWs(u){
  const s = String(u || 'http://localhost:8080')
  if (s.startsWith('https')) return s.replace('https','wss')
  return s.replace('http','ws')
}
App({
  globalData: { socket: null, wsConnected: false, reconnectTimer: null },
  onLaunch(){ i18n.init(); this.initWebSocket() },
  initWebSocket(){
    try {
      const token = wx.getStorageSync('token')
      const userInfo = wx.getStorageSync('userInfo') || {}
      const uidRaw = userInfo.patientId || userInfo.id || userInfo.userId
      if (!token || !uidRaw) { this.scheduleReconnect(); return }
      const base = toWs(cfg && cfg.baseURL)
      const uid = encodeURIComponent(String(uidRaw))
      const url = `${base}/ws?token=${encodeURIComponent(String(token))}&role=patient&userId=${uid}`
      const s = wx.connectSocket({ url })
      s.onOpen(()=>{ this.globalData.wsConnected = true })
      s.onClose(()=>{ this.globalData.wsConnected = false; this.scheduleReconnect() })
      s.onError(()=>{ this.globalData.wsConnected = false; this.scheduleReconnect() })
      s.onMessage((res)=>{
        let msg = null
        try { msg = JSON.parse(res.data) } catch {}
        if (!msg) return
        if (msg.type === 'LEAVE_APPROVAL_RESULT' || msg.type === 'LEAVE_STATUS_UPDATE') {
          const list = wx.getStorageSync('messages') || []
          const entry = {
            id: `${msg.scheduleId || ''}-${Date.now()}`,
            scheduleId: msg.scheduleId,
            doctorId: msg.doctorId,
            status: msg.status,
            reason: msg.reason || '',
            timestamp: Date.now()
          }
          list.push(entry)
          wx.setStorageSync('messages', list)
          wx.showModal({ title: '新消息', content: '审批结果已更新，是否查看？', success: (r) => { if (r.confirm) wx.switchTab({ url: '/pages/message/message' }) } })
        } else if (msg.type === 'APPOINTMENT_CANCELLED_BY_DOCTOR') {
          const list = wx.getStorageSync('messages') || []
          const timeRange = (msg.startTime && msg.endTime) ? `${msg.startTime} - ${msg.endTime}` : ''
          const entry = {
            id: `${msg.scheduleId || ''}-${Date.now()}`,
            type: 'APPOINTMENT_CANCELLED_BY_DOCTOR',
            scheduleId: msg.scheduleId,
            doctorId: msg.doctorId,
            doctorName: msg.doctorName || '',
            scheduleDate: msg.scheduleDate || '',
            timeSlot: msg.timeSlot || '',
            startTime: msg.startTime || '',
            endTime: msg.endTime || '',
            timeText: `${msg.scheduleDate || ''} ${msg.timeSlot || ''} ${timeRange}`.trim(),
            message: msg.message || '你挂的号由于医生原因已取消',
            timestamp: Date.now()
          }
          list.push(entry)
          wx.setStorageSync('messages', list)
          wx.showModal({ title: '号源取消通知', content: '你挂的号由于医生原因已取消，是否查看详情？', success: (r) => { if (r.confirm) wx.switchTab({ url: '/pages/message/message' }) } })
        }
      })
      this.globalData.socket = s
    } catch {}
  },
  scheduleReconnect(){
    if (this.globalData.reconnectTimer) return
    this.globalData.reconnectTimer = setTimeout(()=>{ this.globalData.reconnectTimer = null; this.initWebSocket() }, 5000)
  }
})
