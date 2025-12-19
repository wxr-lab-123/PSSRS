// pages/edit-info/edit-info.js
const userApi = require('../../api/user.js')
const formatUtil = require('../../utils/format.js')

Page({
  data: {
    nickname: '',
    phone: '',
    createTime: ''
  },

  onLoad() {
    const userInfo = wx.getStorageSync('userInfo')
    if (userInfo) {
      this.setData({
        nickname: userInfo.nickname || userInfo.name || '',
        phone: userInfo.phone || '',
        createTime: formatUtil.formatDateTime(userInfo.createdAt || userInfo.createTime)
      })
    }
  },

  onNicknameInput(e) {
    this.setData({ nickname: e.detail.value })
  },
  onPhoneInput(e) {
    this.setData({ phone: e.detail.value })
  },

  // 保存修改
  async saveInfo() {
    const { nickname, phone } = this.data
    if (!nickname || nickname.trim() === '') { wx.showToast({ title: '请输入昵称', icon: 'none' }); return }
    if (phone && phone.trim() !== '') {
      const phoneReg = /^1[3-9]\d{9}$/
      if (!phoneReg.test(phone.trim())) { wx.showToast({ title: '手机号格式不正确', icon: 'none' }); return }
    }
    wx.showLoading({ title: '保存中...' })
    try {
      await userApi.updateUserProfile({ nickname: nickname.trim(), phone: (phone && phone.trim()) ? phone.trim() : '' })
      wx.hideLoading()
      const userInfo = wx.getStorageSync('userInfo') || {}
      userInfo.nickname = nickname.trim()
      userInfo.name = userInfo.nickname
      if (phone && phone.trim() !== '') userInfo.phone = phone.trim()
      wx.setStorageSync('userInfo', userInfo)
      wx.showToast({ title: '修改成功', icon: 'success' })
      setTimeout(() => { wx.navigateBack() }, 1200)
    } catch (err) {
      wx.hideLoading()
      wx.showToast({ title: err?.msg || err?.message || '修改失败，请重试', icon: 'none' })
    }
  }
})
