// index.js
Page({
  data: {
    
  },

  onLoad() {
    
  },

  onShow() {
    
  },

  // 页面导航
  navigateTo(e) {
    const url = e.currentTarget.dataset.url
    if (url) {
      wx.navigateTo({
        url: url
      })
    }
  },

  // 退出登录
  logout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          // 清除用户信息
          wx.removeStorageSync('userInfo')
          
          wx.showToast({
            title: '已退出登录',
            icon: 'success'
          })

          // 跳转到登录页
          setTimeout(() => {
            wx.redirectTo({
              url: '/pages/login/login'
            })
          }, 1500)
        }
      }
    })
  },

  // 下拉刷新
  onPullDownRefresh() {
    // 这里可以根据需要重新请求首页数据
    // 模拟刷新 800ms 后结束
    setTimeout(() => {
      wx.stopPullDownRefresh()
    }, 800)
  }
})
