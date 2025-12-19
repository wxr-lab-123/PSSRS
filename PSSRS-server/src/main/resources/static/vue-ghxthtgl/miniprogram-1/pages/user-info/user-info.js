// pages/user-info/user-info.js
const userApi = require('../../api/user.js')
const formatUtil = require('../../utils/format.js')

Page({
  data: {
    userInfo: null
  },

  onLoad() {
    this.loadUserInfo()
  },

  onShow() {
    // 从编辑页面返回时刷新数据
    this.loadUserInfo()
  },

  // 加载用户信息
  loadUserInfo() {
    wx.showLoading({
      title: '加载中...'
    })

    userApi.getUserInfo()
      .then(res => {
        wx.hideLoading()
        // 后端返回格式：{code: 0 | 200, msg: "success", data: {...}}
        if (res.code === 0 || res.code === 200) {
          // 处理显示数据（脱敏）
          const displayInfo = {
            ...res.data,
            nickname: (res.data && (res.data.nickname || res.data.name)) || '',
            phone: formatUtil.maskPhone((res.data && res.data.phone) || ''),
            createTime: formatUtil.formatDateTime(res.data.createdAt || res.data.createTime)
          }
          
          this.setData({
            userInfo: displayInfo
          })
          // 更新本地存储（保存原始数据）
          wx.setStorageSync('userInfo', res.data)
        } else {
          wx.showToast({
            title: res.msg || res.message || '获取信息失败',
            icon: 'none'
          })
        }
      })
      .catch(err => {
        wx.hideLoading()
        console.error('获取用户信息失败:', err)
        // 如果API调用失败，使用本地存储的数据
        const localUserInfo = wx.getStorageSync('userInfo')
        if (localUserInfo) {
          // 处理显示数据（脱敏）
          const displayInfo = {
            ...localUserInfo,
            nickname: (localUserInfo && (localUserInfo.nickname || localUserInfo.name)) || '',
            phone: formatUtil.maskPhone((localUserInfo && localUserInfo.phone) || ''),
            createTime: formatUtil.formatDateTime(localUserInfo.createdAt || localUserInfo.createTime)
          }
          
          this.setData({
            userInfo: displayInfo
          })
        } else {
          wx.showToast({
            title: '获取信息失败',
            icon: 'none'
          })
        }
      })
  },

  // 跳转到编辑页面
  goToEdit() {
    wx.navigateTo({
      url: '/pages/edit-info/edit-info'
    })
  }
})
