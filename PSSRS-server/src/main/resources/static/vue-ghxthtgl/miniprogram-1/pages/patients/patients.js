const userApi = require('../../api/user.js')
const smsApi = require('../../api/sms.js')

Page({
  data: {
    patients: [],
    showForm: false,
    form: { id: null, name: '', idCard: '', phone: '', relation: '', code: '' },
    isEdit: false,
    countdownText: '获取验证码',
    isCountingDown: false
  },

  onShow() { this.load() },

  async load() {
    try {
      const res = await userApi.listPatients()
      const list = Array.isArray(res?.data) ? res.data : []
      const formatGender = (g) => {
        if (g === '0' || g === 0) return '男'
        if (g === '1' || g === 1) return '女'
        return '未知'
      }
      list.forEach(p => { 
        p.genderText = formatGender(p.gender)
        if (p.idCard && p.idCard.length > 6) {
          p.idCardMasked = p.idCard.substring(0, p.idCard.length - 6) + '******'
        } else {
          p.idCardMasked = p.idCard
        }
      })
      this.setData({ patients: list })
    } catch(e) { wx.showToast({ title: '加载失败', icon: 'none' }) }
  },

  openAdd() { 
    this.resetCountdown()
    this.setData({ showForm: true, isEdit: false, form: { id:null, name:'', idCard:'', phone:'', relation:'家属', code: '' } }) 
  },
  openEdit(e) { 
    this.resetCountdown()
    const item = e.currentTarget.dataset.item || {}; 
    let maskedIdCard = item.idCard
    if (maskedIdCard && maskedIdCard.length > 6) {
      // 保留前面部分，后6位替换为 *
      const prefix = maskedIdCard.slice(0, -6);
      const suffix = '*'.repeat(6);
      maskedIdCard = prefix + suffix;
    }
    this.setData({ showForm:true, isEdit:true, form: { id:item.id, name:item.name, idCard:maskedIdCard, phone:item.phone, relation:item.relation || '家属', code: '' } }) 
  },
  closeForm() { 
    this.setData({ showForm: false })
    this.resetCountdown()
  },
  resetCountdown() {
    if (this.timer) clearInterval(this.timer)
    this.setData({
      countdownText: '获取验证码',
      isCountingDown: false
    })
  },
  async getVerificationCode() {
    const phone = this.data.form.phone
    if (!phone || !/^1\d{10}$/.test(phone)) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' })
      return
    }
    
    try {
      await smsApi.sendRegisterCode(phone)
      wx.showToast({ title: '验证码已发送', icon: 'success' })
      
      this.setData({ isCountingDown: true })
      let seconds = 60
      this.setData({ countdownText: `${seconds}s` })
      
      this.timer = setInterval(() => {
        seconds--
        if (seconds <= 0) {
          this.resetCountdown()
        } else {
          this.setData({ countdownText: `${seconds}s` })
        }
      }, 1000)
    } catch(e) {
      wx.showToast({ title: e.msg || '发送失败', icon: 'none' })
    }
  },
  stop() {},
  onInput(e) { const key = e.currentTarget.dataset.key; const val = e.detail.value; const form = { ...this.data.form, [key]: val }; this.setData({ form }) },
  onRelationChange(e) { const form = { ...this.data.form, relation: e.detail.value }; this.setData({ form }) },

  async save() {
    const f = this.data.form
    if (!f.name) { wx.showToast({ title: '请输入姓名', icon: 'none' }); return }
    if (!f.idCard) { wx.showToast({ title: '请输入身份证号', icon: 'none' }); return }
    if (!f.phone) { wx.showToast({ title: '请输入手机号', icon: 'none' }); return }
    if (!f.code || !/^\d{6}$/.test(f.code)) { 
      wx.showToast({ title: '请输入6位数字验证码', icon: 'none' }); 
      return 
    }

    try {
      if (this.data.isEdit) { await userApi.updatePatient(f.id, f) } else { await userApi.addPatient(f) }
      wx.showToast({ title: '已保存', icon: 'success' })
      this.setData({ showForm: false })
      this.resetCountdown()
      this.load()
    } catch(e) { wx.showToast({ title: e?.msg || '保存失败', icon: 'none' }) }
  },

  async remove(e) {
    const id = e.currentTarget.dataset.id
    if (!id) return
    wx.showModal({ title: '删除就诊人', content: '确定删除该就诊人？', success: async r => { if (!r.confirm) return; try { await userApi.deletePatient(id); wx.showToast({ title: '已删除' }); this.load() } catch(err) { wx.showToast({ title: err?.msg || '删除失败', icon: 'none' }) } } })
  },

  async choose(e) {
    const id = e.currentTarget.dataset.id
    try { await userApi.switchPatient(id); wx.showToast({ title: '已选择', icon: 'success' }); wx.navigateBack({ delta: 1 }) } catch(err) { wx.showToast({ title: err?.msg || '选择失败', icon: 'none' }) }
  }
})

