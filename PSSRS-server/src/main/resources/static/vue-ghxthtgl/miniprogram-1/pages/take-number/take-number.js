// pages/take-number/take-number.js
const queryApi = require('../../api/query.js')
const appointmentApi = require('../../api/appointment.js')

Page({
  data: {
    appointments: [],
    loading: false
  },

  onLoad() {
    // 页面加载时自动加载用户的预约记录
    this.loadAppointments()
  },

  // 加载预约记录
  loadAppointments() {
    this.setData({ loading: true })
    wx.showLoading({ title: '查询中...' })

    // 查询挂号记录（包含预约信息，不分页）
    queryApi.getRegistrationRecords()
      .then(res => {
        wx.hideLoading()
        const records = res.data || []
        
        // 过滤和格式化数据
        const appointments = records.map(item => {
          let status = 'paid'
          let statusText = '待就诊'
          
          // 根据实际状态码判断：0-待就诊, 1-已就诊, 2-已取消
          if (item.status === '0') {
            status = 'paid'
            statusText = '待就诊'
          } else if (item.status === '1') {
            status = 'taken'
            statusText = '已就诊'
          } else if (item.status === '2') {
            status = 'cancelled'
            statusText = '已取消'
          }

          const realOrderNo = item.visitNumber || item.orderNo
          return {
            id: item.id,
            orderNo: realOrderNo,
            displayOrderNo: status === 'paid' ? '请先取号' : realOrderNo,
            registrationNo: item.registrationNo,
            patientName: item.patientName,
            department: item.departmentName,
            doctorName: item.doctorName,
            appointmentTime: `${item.appointmentDate} ${item.timeSlot}`,
            status: status,
            statusText: statusText,
            registrationId: item.registrationId
          }
        })

        this.setData({
          appointments: appointments,
          loading: false
        })
      })
      .catch(err => {
        wx.hideLoading()
        this.setData({ loading: false })
        console.error('查询预约记录失败:', err)
        wx.showToast({
          title: err?.msg || err?.message || '查询失败',
          icon: 'none'
        })
      })
  },

  // 预约取号
  takeNumber(e) {
    const item = e.currentTarget.dataset.item
    
    if (item.status === 'taken') {
      wx.showToast({
        title: '已就诊',
        icon: 'none'
      })
      return
    }

    if (item.status !== 'paid') {
      wx.showToast({
        title: '请先支付后再取号',
        icon: 'none'
      })
      return
    }
    
    wx.showModal({
      title: '确认取号',
      content: `确认取号：${item.department} - ${item.doctorName}\n时间：${item.appointmentTime}`,
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '取号中...' })
          
          // 调用取号API
          appointmentApi.takeNumber(item.registrationNo)
            .then(res => {
              wx.hideLoading()
              wx.showToast({
                title: '取号成功',
                icon: 'success'
              })

              // 只更新当前取号的预约项状态，确保UI即时响应
              const appointments = this.data.appointments.map(appointment => {
                // 使用 registrationNo 进行唯一匹配
                if (appointment.registrationNo === item.registrationNo) {
                  return {
                    ...appointment,
                    status: 'taken',
                    statusText: '已就诊', // 与初始加载时状态码2的映射保持一致
                    displayOrderNo: appointment.orderNo
                  }
                }
                return appointment
              })
              
              // 立即更新UI
              this.setData({ appointments })
            })
            .catch(err => {
              wx.hideLoading()
              console.error('取号失败:', err)
              wx.showToast({
                title: err?.msg || err?.message || '取号失败',
                icon: 'none'
              })
            })
        }
      }
    })
  }
})
