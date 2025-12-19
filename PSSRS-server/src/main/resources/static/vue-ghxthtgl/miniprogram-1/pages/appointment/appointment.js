// pages/appointment/appointment.js
const departmentApi = require('../../api/department.js')
const doctorApi = require('../../api/doctor.js')
const registrationApi = require('../../api/registration.js')
const orderApi = require('../../api/order.js')
const userApi = require('../../api/user.js')
const { validateUserInfo } = require('../../utils/validators.js')

Page({
  data: {
    selectedDate: null,
    selectedDept: null,
    selectedPatient: null,
    dateList: [],
    departments: [],
    doctors: [],
    patients: [],
    loadingDoctors: false
  },

  onShow() {
    this.loadPatients()
    // 如果已经选择了科室和日期，重新加载医生列表以更新号源状态
    if (this.data.selectedDept && this.data.selectedDate) {
      this.loadDoctors()
    }
  },

  onLoad() {
    this.generateDateList()
    this.loadDepartments()
  },

  // 加载就诊人列表
  loadPatients() {
    userApi.listPatients()
      .then(res => {
        const list = res.data || []
        this.setData({
          patients: list
        })
        
        // 如果没有选中的就诊人，默认选中第一个
        if (!this.data.selectedPatient && list.length > 0) {
          this.setData({
            selectedPatient: list[0]
          })
        }
      })
      .catch(err => {
        console.error('获取就诊人列表失败:', err)
      })
  },

  // 选择就诊人
  selectPatient(e) {
    const item = e.currentTarget.dataset.item
    this.setData({
      selectedPatient: item
    })
  },

  // 跳转添加就诊人
  goToAddPatient() {
    wx.navigateTo({
      url: '/pages/patients/patients'
    })
  },

  // 生成未来14天的日期列表（仅工作日）
  generateDateList() {
    const dateList = []
    const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

    let added = 0
    let offset = 1
    while (added < 14) {
      const date = new Date()
      date.setDate(date.getDate() + offset)
      const weekIndex = date.getDay()
      // 只保留工作日（周一到周五）
      if (weekIndex !== 0 && weekIndex !== 6) {
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        const week = weekDays[weekIndex]
        const dateStr = `${date.getFullYear()}-${month}-${day}`

        dateList.push({
          date: dateStr,
          month: `${month}月`,
          day: Number(day),
          week: week,
          hasSlot: true
        })
        added++
      }
      offset++
    }

    this.setData({
      dateList
    })

    if (dateList.length > 0) {
      this.setData({
        selectedDate: dateList[0].date
      })
    }
  },

  // 加载科室列表
  loadDepartments() {
    departmentApi.getDepartments()
      .then(res => {
        const list = res.data || []
        this.setData({
          departments: list
        })

        if (!this.data.selectedDept && list.length > 0) {
          this.setData({
            selectedDept: list[0].id
          })
          if (this.data.selectedDate) {
            this.loadDoctors()
          }
        }
      })
      .catch(err => {
        console.error('获取科室列表失败:', err)
      })
  },

  // 选择日期
  selectDate(e) {
    const date = e.currentTarget.dataset.date
    const hasSlot = e.currentTarget.dataset.hasslot

    if (hasSlot === false || hasSlot === 'false') {
      wx.showToast({
        title: '该日期暂无可预约号源',
        icon: 'none'
      })
      return
    }

    this.setData({
      selectedDate: date
    })

    if (this.data.selectedDept) {
      this.loadDoctors()
    }
  },

  // 选择科室
  selectDepartment(e) {
    const deptId = e.currentTarget.dataset.id
    this.setData({
      selectedDept: deptId
    })

    if (this.data.selectedDate) {
      this.loadDoctors()
    }
  },

  // 加载医生列表（复用 today-register 的逻辑：按 科室 + 日期 查询）
  loadDoctors() {
    if (!this.data.selectedDept || !this.data.selectedDate) {
      wx.showToast({
        title: '请先选择日期和科室',
        icon: 'none'
      })
      return
    }

    this.setData({
      loadingDoctors: true
    })

    doctorApi.getDoctors(this.data.selectedDept, this.data.selectedDate)
      .then(res => {
        const doctors = (res.data || []).map(item => {
          const timeLabel = item.timeSlot || ''
          const startTime = item.startTime || ''
          const endTime = item.endTime || ''
          const fullSlot = `${timeLabel} ${startTime && endTime ? `${startTime}-${endTime}` : ''}`.trim()

          return {
            id: item.id,
            name: item.doctorName || item.name,
            title: item.title || '',
            specialty: item.notes || item.specialty || '',
            price: item.price || item.fee || 0,
            image: item.image || '',
            scheduleType: item.scheduleType || item.schedule_type || 'normal',
            scheduleDate: item.scheduleDate || this.data.selectedDate,
            raw: item,
            timeSlots: [
              {
                time: fullSlot || `${startTime}-${endTime}` || '就诊时段',
                available: item.status === 'AVAILABLE'
              }
            ]
          }
        })

        this.setData({
          doctors
        })

        const hasSlot = doctors.some(d => d.timeSlots.some(s => s.available))
        const dateList = this.data.dateList.map(d => {
          if (d.date === this.data.selectedDate) {
            return { ...d, hasSlot }
          }
          return d
        })
        this.setData({
          dateList
        })
      })
      .catch(err => {
        console.error('获取医生列表失败:', err)
      })
      .finally(() => {
        this.setData({
          loadingDoctors: false
        })
      })
  },

  // 预约挂号
  bookAppointment(e) {
    const doctor = e.currentTarget.dataset.doctor
    const slot = e.currentTarget.dataset.slot

    if (!this.data.selectedDate || !this.data.selectedDept) {
      wx.showToast({
        title: '请先选择日期和科室',
        icon: 'none'
      })
      return
    }

    if (!this.data.selectedPatient) {
      wx.showToast({
        title: '请选择就诊人',
        icon: 'none'
      })
      return
    }

    if (!slot || !slot.available) {
      wx.showToast({
        title: '该时段已满',
        icon: 'none'
      })
      return
    }

    wx.showModal({
      title: '确认预约',
      content: `预约就诊人：${this.data.selectedPatient.name}\n医生：${doctor.name}\n日期：${this.data.selectedDate}\n时间：${slot.time}\n费用：¥${doctor.price}`,
      success: (res) => {
        if (res.confirm) {
          this.confirmAppointment(doctor, slot)
        }
      }
    })
  },

  // 确认预约：复用今日挂号的接口
  confirmAppointment(doctor, slot) {
    if (this._submitting) {
      return
    }

    this._submitting = true
    const patient = this.data.selectedPatient

    // 这里不再校验当前登录用户的信息，而是校验选中的就诊人信息
    // 假设选择的就诊人信息已经是完整的
    if (!patient || !patient.name || !patient.phone || !patient.idCard) {
       wx.showToast({
         title: '就诊人信息不完整',
         icon: 'none'
       })
       this._submitting = false
       return
    }

    wx.showLoading({ title: '挂号中...' })

    console.log('appointment.js: 准备创建订单，doctor.raw 对象:', doctor.raw)

    // 只传 scheduleId，由后端根据排班和登录用户创建订单并计算金额
    const scheduleId = doctor.raw.id || doctor.id
    if (!scheduleId && scheduleId !== 0) {
      wx.hideLoading()
      this._submitting = false
      wx.showToast({ title: '排班ID缺失', icon: 'none' })
      return
    }

    orderApi.createOrder(scheduleId, patient.id)
      .then(res => {
        wx.hideLoading()
        
        // 延迟显示 Toast，避免与 hideLoading 冲突
        setTimeout(() => {
          wx.showToast({
            title: res.data || '挂号成功',
            icon: 'success',
            duration: 2000
          })
        }, 300)

        // 构建订单数据，使用后端返回的订单信息
        const data = res.data || {}
        const orderData = {
          orderNo: data.orderNo,
          scheduleId: data.scheduleId,
          amount: data.amount,
          status: data.status,
          expireTime: data.expireTime,
          createTime: data.createTime,
          patientName: patient.name,
          doctorName: data.doctorName || doctor.name,
          departmentName: data.departmentName || this.data.departments.find(d => d.id === this.data.selectedDept)?.name || '',
          registrationDate: doctor.raw.scheduleDate || this.data.selectedDate,
          timeSlot: slot.time,
          // 兼容支付页使用的 price 字段，实际金额来自后端 amount
          price: data.amount,
          remark: data.remark
        }
        
        console.log('构建的订单数据:', orderData)
        
        setTimeout(() => {
          wx.navigateTo({
            url: `/pages/payment/payment?orderData=${encodeURIComponent(JSON.stringify(orderData))}`
          })
        }, 1000)
        
        // 不需要手动更新前端状态，onShow 会自动刷新
        // 之前的逻辑会强制将号源设为已满，导致显示错误
      })
      .catch(err => {
        if (err && err.message === '用户资料未完善') return
        wx.hideLoading()
        // 优先使用后端返回的错误信息
        const errorMessage = err?.msg || err?.data?.message || err?.message || '挂号失败'
        wx.showToast({
          title: errorMessage,
          icon: 'none'
        })
        console.error('挂号失败:', err)
      })
      .finally(() => {
        this._submitting = false
      })
  }
})
