// pages/inquiry/inquiry.js
const queryApi = require('../../api/query.js')
const registrationApi = require('../../api/registration.js')

Page({
  data: {
    pendingRecords: [],  // 待就诊记录
    completedRecords: [],  // 已就诊记录
    cancelledRecords: []  // 已取消记录
  },

  // 取消挂号
  cancelAppointment(e) {
    const { id } = e.currentTarget.dataset
    
    // 查找对应的挂号记录
    let record = null
    for (let i = 0; i < this.data.pendingRecords.length; i++) {
      if (this.data.pendingRecords[i].id === id) {
        record = this.data.pendingRecords[i]
        break
      }
    }
    
    if (!record) {
      wx.showToast({ title: '记录不存在', icon: 'none' })
      return
    }
    
    console.log('准备取消挂号，订单信息:', record)
    
    wx.showModal({
      title: '取消挂号',
      content: '确定要取消该挂号吗？',
      confirmColor: '#ff4d4f',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '处理中...' })
          
          // 按 registrationNo 取消挂号
              registrationApi.cancelRegistration(record.registrationNo)
                .then(() => {
                  wx.hideLoading()
                  wx.showToast({ title: '取消成功' })
                  
                  // 直接更新记录状态，提供更好的用户体验
                  // 1. 从待就诊记录中移除
                  const pendingRecords = this.data.pendingRecords.filter(item => item.id !== id)
                  
                  // 2. 创建已取消的记录并添加到已取消列表
                  const cancelledRecord = {
                    ...record,
                    status: 'cancelled',
                    statusText: '已取消'
                  }
                  const cancelledRecords = [...this.data.cancelledRecords, cancelledRecord]
                  
                  // 3. 更新数据
                  this.setData({
                    pendingRecords,
                    cancelledRecords
                  })
                  
                  // 4. 仍然刷新列表以确保数据一致性
                  setTimeout(() => {
                    this.getRegistrationRecords()
                  }, 500)
                })
            .catch(err => {
              wx.hideLoading()
              console.error('取消挂号失败:', err)
              wx.showToast({
                title: err?.msg || err?.message || '取消失败',
                icon: 'none'
              })
            })
        }
      }
    })
  },

  // 查看挂号详情
  viewDetails(e) {
    const { id } = e.currentTarget.dataset
    
    // 查找对应的挂号记录
    let record = null
    
    // 先从待就诊记录中查找
    for (let i = 0; i < this.data.pendingRecords.length; i++) {
      if (this.data.pendingRecords[i].id === id) {
        record = this.data.pendingRecords[i]
        break
      }
    }
    
    // 如果没找到，从已就诊记录中查找
    if (!record) {
      for (let i = 0; i < this.data.completedRecords.length; i++) {
        if (this.data.completedRecords[i].id === id) {
          record = this.data.completedRecords[i]
          break
        }
      }
    }
    
    if (record) {
      // 将记录信息存储到缓存，方便详情页获取
      wx.setStorageSync('currentRecord', record)
      
      // 这里可以导航到详情页面，如果有的话
      // wx.navigateTo({ url: `/pages/record-detail/record-detail?id=${id}` })
      
      // 暂时使用弹窗显示详情
      this.showRecordDetails(record)
    }
  },

  // 显示记录详情弹窗
  showRecordDetails(record) {
    const detailContent = `
      科室：${record.department}\n
      医生：${record.doctor}\n
      就诊时间：${record.time}\n
      挂号费：¥${record.fee}\n
      就诊号：${record.number}\n
      状态：${record.statusText}
    `
    
    wx.showModal({
      title: '挂号详情',
      content: detailContent.trim(),
      showCancel: false,
      confirmText: '确定'
    })
  },

  // 导航到挂号页面
  navigateToRegister() {
    wx.switchTab({
      url: '/pages/index/index'
    })
  },

  onLoad() {
    // 页面加载时获取挂号记录
    this.getRegistrationRecords()
  },

  // 获取挂号记录并分类显示
  getRegistrationRecords() {
    wx.showLoading({ title: '加载中...' })
    
    // 调用查询接口获取挂号记录
    queryApi.getRegistrationRecords()
      .then(res => {
        wx.hideLoading()
        const records = res.data || []
        
        // 格式化数据并分类
        const pendingRecords = []
        const completedRecords = []
        const cancelledRecords = []
        
        records.forEach(item => {
          // 根据新的状态码判断：0-待就诊, 1-已就诊, 2-已取消
          let status = 'pending'
          let statusText = '待就诊'

          if (item.status === '0') {
            status = 'pending'
            statusText = '待就诊'
            pendingRecords.push({
              id: item.id,
              orderNo: item.orderNo,
              registrationNo: item.registrationNo,
              department: item.departmentName,
              doctor: item.doctorName,
              time: `${item.appointmentDate} ${item.timeSlot}`,
              fee: item.fee || 0,
              number: item.visitNumber || '',
              status: status,
              statusText: statusText
            })
          } else if (item.status === '1') {
            status = 'completed'
            statusText = '已就诊'
            completedRecords.push({
              id: item.id,
              orderNo: item.orderNo,
              registrationNo: item.registrationNo,
              department: item.departmentName,
              doctor: item.doctorName,
              time: `${item.appointmentDate} ${item.timeSlot}`,
              fee: item.fee || 0,
              number: item.visitNumber || '',
              status: status,
              statusText: statusText
            })
          } else if (item.status === '2') {
            // 已取消状态
            status = 'cancelled'
            statusText = '已取消'
            cancelledRecords.push({
              id: item.id,
              orderNo: item.orderNo,
              registrationNo: item.registrationNo,
              department: item.departmentName,
              doctor: item.doctorName,
              time: `${item.appointmentDate} ${item.timeSlot}`,
              fee: item.fee || 0,
              number: item.visitNumber || '',
              status: status,
              statusText: statusText
            })
          }
        })
        
        this.setData({
          pendingRecords,
          completedRecords,
          cancelledRecords
        })
      })
      .catch(err => {
        wx.hideLoading()
        console.error('获取挂号记录失败:', err)
        wx.showToast({
          title: err?.msg || err?.message || '加载失败',
          icon: 'none'
        })
      })
  }
})
