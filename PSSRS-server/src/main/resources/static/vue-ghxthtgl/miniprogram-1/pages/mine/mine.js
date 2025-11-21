// pages/mine/mine.js
const userApi = require('../../api/user.js')

Page({
  data: {
    isLogin: false,
    userInfo: null
  },

  onLoad() {
    this.checkLoginStatus()
  },

  onShow() {
    this.checkLoginStatus()
  },

  // 检查登录状态
  checkLoginStatus() {
    const token = wx.getStorageSync('token')
    const userInfo = wx.getStorageSync('userInfo')
    
    if (token && userInfo) {
      this.setData({
        isLogin: true,
        userInfo: userInfo
      })
      // 获取完整用户信息
      this.fetchUserInfo()
    } else {
      this.setData({
        isLogin: false,
        userInfo: null
      })
    }
  },
  
  // 获取完整用户信息
  fetchUserInfo() {
    userApi.getUserInfo()
      .then(res => {
        if (res.code === 0 || res.code === 200) {
          // 合并并保存用户信息
          const userInfo = wx.getStorageSync('userInfo')
          const fullUserInfo = { ...userInfo, ...res.data }
          
          wx.setStorageSync('userInfo', fullUserInfo)
          this.setData({
            userInfo: fullUserInfo
          })
        }
      })
      .catch(err => {
        console.error('获取用户信息失败:', err)
      })
  },

  // 去登录
  goLogin() {
    wx.navigateTo({
      url: '/pages/login/login'
    })
  },

  // 跳转到用户信息页面
  goToUserInfo() {
    wx.navigateTo({
      url: '/pages/user-info/user-info'
    })
  },

  // 页面跳转
  navigateTo(e) {
    const url = e.currentTarget.dataset.url
    
    // 检查是否登录
    if (!this.data.isLogin) {
      wx.showModal({
        title: '提示',
        content: '请先登录',
        success: (res) => {
          if (res.confirm) {
            this.goLogin()
          }
        }
      })
      return
    }
    
    wx.navigateTo({ url })
  },

  // 退出登录
  logout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          // 调用退出登录API
          userApi.logout()
            .then(() => {
              // 清除本地存储
              wx.removeStorageSync('token')
              wx.removeStorageSync('userInfo')
              
              // 更新页面状态
              this.setData({
                isLogin: false,
                userInfo: null
              })
              
              wx.showToast({
                title: '已退出登录',
                icon: 'success'
              })
            })
            .catch(err => {
              console.error('退出登录失败:', err)
              // 即使API调用失败，也清除本地数据
              wx.removeStorageSync('token')
              wx.removeStorageSync('userInfo')
              this.setData({
                isLogin: false,
                userInfo: null
              })
            })
        }
      }
    })
  },

  // 即将推出
  showComingSoon() {
    wx.showToast({
      title: '功能开发中',
      icon: 'none'
    })
  },

  // 关于
  showAbout() {
    wx.showModal({
      title: '关于',
      content: '患者自助挂号系统 V2.0\n\n提供便捷的医院挂号服务',
      showCancel: false
    })
  },

  // 修改密码
  changePassword() {
    // 检查是否登录
    if (!this.data.isLogin) {
      wx.showModal({
        title: '提示',
        content: '请先登录',
        success: (res) => {
          if (res.confirm) {
            this.goLogin()
          }
        }
      })
      return
    }

    // 跳转到修改密码页面
    wx.navigateTo({
      url: '/pages/change-password/change-password'
    })
  }
})
