// pages/change-password/change-password.js
const userApi = require('../../api/user.js')

Page({
  /**
   * 页面的初始数据
   */
  data: {
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
    isLoading: false
  },

  // 输入框变化事件
  onCurrentPasswordInput(e) {
    this.setData({
      currentPassword: e.detail.value.trim()
    })
  },

  onNewPasswordInput(e) {
    this.setData({
      newPassword: e.detail.value.trim()
    })
  },

  onConfirmPasswordInput(e) {
    this.setData({
      confirmPassword: e.detail.value.trim()
    })
  },

  // 处理修改密码
  handleChangePassword() {
    const { currentPassword, newPassword, confirmPassword } = this.data

    // 表单验证
    if (!currentPassword) {
      wx.showToast({
        title: '请输入当前密码',
        icon: 'none'
      })
      return
    }

    if (!newPassword) {
      wx.showToast({
        title: '请输入新密码',
        icon: 'none'
      })
      return
    }

    if (newPassword.length < 6 || newPassword.length > 16) {
      wx.showToast({
        title: '密码长度为6-16位',
        icon: 'none'
      })
      return
    }

    if (newPassword !== confirmPassword) {
      wx.showToast({
        title: '两次输入的新密码不一致',
        icon: 'none'
      })
      return
    }

    // 调用修改密码API
    this.setData({ isLoading: true })
    
    userApi.changePassword({
      oldPassword: currentPassword,
      newPassword: newPassword
    })
      .then(res => {
        wx.showToast({
          title: '密码修改成功',
          icon: 'success',
          duration: 1500,
          success: () => {
            // 返回上一页
            setTimeout(() => {
              wx.navigateBack()
            }, 1500)
          }
        })
      })
      .catch(err => {
        console.error('修改密码失败:', err)
        wx.showToast({
          title: err.msg || '修改密码失败',
          icon: 'none',
          duration: 2000
        })
      })
      .finally(() => {
        this.setData({ isLoading: false })
      })
  }
})