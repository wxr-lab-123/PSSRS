// pages/login/login.js
const userApi = require('../../api/user.js')

Page({
  data: {
    fromPage: ''
  },

  onLoad(options) {
    if (options && options.from) {
      this.setData({ fromPage: options.from })
    }
  },

  wechatLogin() {
    wx.showLoading({ title: '登录中...' })
    wx.login({
      success: (res) => {
        if (res.code) {
          userApi.wechatLogin(res.code)
            .then(result => {
              wx.hideLoading()
              const token = result.data
              if (!token) {
                wx.showToast({ title: '登录失败：未获取到token', icon: 'none' })
                return
              }
              wx.setStorageSync('token', token)
              return userApi.getUserInfo()
            })
            .then(userRes => {
              const userInfo = {
                loginTime: new Date().getTime(),
                loginType: 'wechat',
                ...userRes.data
              }
              wx.setStorageSync('userInfo', userInfo)
              try {
                const ui = wx.getStorageSync('userInfo') || {}
                const nick = ui.nickname || ui.nickName || ''
                if ((ui.avatarUrl && ui.avatarUrl.length > 0) || (nick && nick.length > 0)) {
                  userApi.updateUserProfile({ avatarUrl: ui.avatarUrl || '', nickname: nick })
                    .catch(() => {})
                }
              } catch (e) {}
              wx.showToast({ title: '登录成功', icon: 'success' })
              setTimeout(() => {
                wx.switchTab({ url: '/pages/mine/mine' })
              }, 1500)
            })
            .catch(err => {
              wx.hideLoading()
              console.error('微信登录失败:', err)
              wx.showToast({
                title: (err && (err.msg || err.message)) || '微信登录失败',
                icon: 'none'
              })
            })
        } else {
          wx.hideLoading()
          wx.showToast({ title: '获取微信授权失败', icon: 'none' })
        }
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({ title: '微信登录失败', icon: 'none' })
      }
    })
  },
  authorizeAndLogin() {
    wx.getUserProfile({
      desc: '用于完善资料',
      success: (profile) => {
        const ui = wx.getStorageSync('userInfo') || {}
        ui.avatarUrl = (profile && profile.userInfo && profile.userInfo.avatarUrl) || ui.avatarUrl || ''
        ui.nickName = (profile && profile.userInfo && profile.userInfo.nickName) || ui.nickName || ''
        ui.nickname = ui.nickname || ui.nickName || ''
        wx.setStorageSync('userInfo', ui)
        try { userApi.updateUserProfile({ avatarUrl: ui.avatarUrl, nickname: ui.nickname }).catch(() => {}) } catch (e) {}
      },
      complete: () => { this.wechatLogin() }
    })
  },
  getPhoneNumber(e) {
    try {
      const ui = wx.getStorageSync('userInfo') || {}
      const pn = e && e.detail && e.detail.phoneNumber
      const code = e && e.detail && e.detail.code
      if (pn) {
        ui.phone = pn
        wx.setStorageSync('userInfo', ui)
        userApi.updateUserProfile({ phone: pn }).catch(() => {})
      } else if (code) {
        userApi.updateUserProfile({ phoneCode: code }).catch(() => {})
      }
    } catch(_) {}
    this.authorizeAndLogin()
  }
})
