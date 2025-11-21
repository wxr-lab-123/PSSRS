// pages/edit-info/edit-info.js
const userApi = require('../../api/user.js')

Page({
  data: {
    name: '',
    gender: '',
    age: '',
    phone: '',
    idCard: '',
    genderOptions: ['男', '女'],
    genderIndex: -1
  },

  onLoad() {
    // 获取当前用户信息
    const userInfo = wx.getStorageSync('userInfo')
    if (userInfo) {
      // 设置性别索引
      let genderIndex = -1
      if (userInfo.gender === 0 || userInfo.gender === '男') {
        genderIndex = 0
      } else if (userInfo.gender === 1 || userInfo.gender === '女') {
        genderIndex = 1
      }

      this.setData({
        name: userInfo.name || '',
        gender: userInfo.gender === 0 || userInfo.gender === 1 ? userInfo.gender : '',
        age: userInfo.age || '',
        phone: userInfo.phone || '',
        idCard: userInfo.idCard || '',
        genderIndex: genderIndex
      })
    }
  },

  // 姓名输入
  onNameInput(e) {
    this.setData({
      name: e.detail.value
    })
  },

  // 性别选择
  onGenderChange(e) {
    const index = parseInt(e.detail.value)
    this.setData({
      gender: index, // 0为男，1为女
      genderIndex: index
    })
  },

  // 年龄输入
  onAgeInput(e) {
    this.setData({
      age: e.detail.value
    })
  },

  // 手机号输入
  onPhoneInput(e) {
    this.setData({
      phone: e.detail.value
    })
  },

  // 保存修改
  saveInfo() {
    const { name, gender, age, phone } = this.data

    // 验证姓名
    if (!name || name.trim() === '') {
      wx.showToast({
        title: '请输入姓名',
        icon: 'none'
      })
      return
    }

    // 验证年龄
    if (age && (isNaN(age) || age < 0 || age > 150)) {
      wx.showToast({
        title: '请输入有效的年龄',
        icon: 'none'
      })
      return
    }

    // 验证手机号
    if (!phone || phone.trim() === '') {
      wx.showToast({
        title: '请输入手机号',
        icon: 'none'
      })
      return
    }

    const phoneReg = /^1[3-9]\d{9}$/
    if (!phoneReg.test(phone)) {
      wx.showToast({
        title: '手机号格式不正确',
        icon: 'none'
      })
      return
    }

    // 调用更新接口
    wx.showLoading({
      title: '保存中...'
    })

    const updateData = {
      name: name.trim(),
      phone: phone.trim()
    }

    // 如果有性别和年龄，也加入更新数据
    if (gender === 0 || gender === 1) {
      updateData.gender = gender // 0为男，1为女
    }
    if (age) {
      updateData.age = parseInt(age)
    }

    userApi.updateUserInfo(updateData)
      .then(res => {
        wx.hideLoading()
        // 后端返回格式：{code: 0, msg: "success", data: {...}}
        if (res.code === 0 || res.code === 200) {
          // 更新本地存储
          const userInfo = wx.getStorageSync('userInfo')
          userInfo.name = name.trim()
          userInfo.phone = phone.trim()
          if (gender === 0 || gender === 1) userInfo.gender = gender
          if (age) userInfo.age = parseInt(age)
          wx.setStorageSync('userInfo', userInfo)

          wx.showToast({
            title: '修改成功',
            icon: 'success'
          })

          // 延迟返回上一页
          setTimeout(() => {
            wx.navigateBack()
          }, 1500)
        } else {
          wx.showToast({
            title: res.msg || res.message || '修改失败',
            icon: 'none'
          })
        }
      })
      .catch(err => {
        wx.hideLoading()
        console.error('修改信息失败:', err)
        wx.showToast({
          title: '修改失败，请重试',
          icon: 'none'
        })
      })
  }
})
