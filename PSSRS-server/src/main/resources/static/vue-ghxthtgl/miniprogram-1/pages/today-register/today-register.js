// pages/today-register/today-register.js
const departmentApi = require('../../api/department.js')
const doctorApi = require('../../api/doctor.js')
const registrationApi = require('../../api/registration.js')
const orderApi = require('../../api/order.js')
const request = require('../../utils/request.js')
const { validateUserInfo } = require('../../utils/validators.js')
const userApi = require('../../api/user.js')

Page({
  data: {
    currentDate: '',
    selectedDept: null,
    selectedDeptName: '',
    departments: [],
    doctors: [],
    showModal: false,
    selectedDoctor: null,
    patients: [],
    selectedPatientId: null,
    selectedPatientName: null
  },

  onLoad() {
    this.setCurrentDate()
    this.loadDepartments()
  },

  onShow() {
    if (this.data.selectedDept) {
      this.loadDoctors(this.data.selectedDept)
    }
    this.loadPatients()
  },

  // 设置当前日期
  setCurrentDate() {
    const date = new Date()
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
    const weekDay = weekDays[date.getDay()]
    
    this.setData({
      currentDate: `${year}年${month}月${day}日 ${weekDay}`
    })
  },

  // 加载科室列表
  loadDepartments() {
    departmentApi.getDepartments()
      .then(res => {
        this.setData({
          departments: res.data || []
        })
      })
      .catch(err => {
        console.error('获取科室列表失败:', err)
      })
  },

  // 选择科室
  selectDepartment(e) {
    const deptId = e.currentTarget.dataset.id
    const deptName = e.currentTarget.dataset.name
    this.setData({
      selectedDept: deptId,
      selectedDeptName: deptName
    })
    this.loadDoctors(deptId)
  },

  // 加载医生列表
  loadDoctors(deptId) {
    const today = new Date().toISOString().split('T')[0]
    
    doctorApi.getDoctors(deptId, today)
      .then(res => {
        this.setData({
          doctors: res.data || []
        })
      })
      .catch(err => {
        console.error('获取医生列表失败:', err)
      })
  },

  // 加载就诊人列表
  loadPatients() {
    const token = wx.getStorageSync('token')
    if (!token) return
    userApi.listPatients()
      .then(res => {
        const list = Array.isArray(res?.data) ? res.data : []
        const selected = this.data.selectedPatientId || (list[0]?.id || null)
        const selectedName = selected ? ((list.find(p=>p.id===selected) || {}).name || null) : null
        this.setData({ patients: list, selectedPatientId: selected, selectedPatientName: selectedName })
      })
      .catch(() => {})
  },

  choosePatient() {
    const list = this.data.patients
    if (!list || list.length === 0) {
      wx.showModal({ title: '请先添加就诊人', content: '需要先绑定至少一个就诊人', confirmText: '去添加', success: r=>{ if (r.confirm) wx.navigateTo({ url: '/pages/patients/patients' }) } })
      return
    }
    const names = list.map(p=>`${p.name}${p.relation?`(${p.relation})`:''}`)
    wx.showActionSheet({ itemList: names, success: r=>{ const idx=r.tapIndex; const item=list[idx]; const pid=item?.id; if (pid) this.setData({ selectedPatientId: pid, selectedPatientName: item?.name || null }) } })
  },

  goManage() { wx.navigateTo({ url: '/pages/patients/patients' }) },

  // 选择医生，显示详情弹窗
  selectDoctor(e) {
    const doctor = e.currentTarget.dataset.doctor
    const scheduleId = doctor.id // 排班ID
    
    // 显示加载中
    wx.showLoading({
      title: '加载中...'
    })
    
    // 根据ID查询排班详情
    doctorApi.getScheduleDetail(scheduleId)
      .then(res => {
        wx.hideLoading()
        // 合并列表数据和详情数据
        const detailData = res.data || doctor
        this.setData({
          selectedDoctor: detailData,
          showModal: true
        })
      })
      .catch(err => {
        wx.hideLoading()
        console.error('获取排班详情失败:', err)
        // 如果接口失败，使用列表中的数据
        this.setData({
          selectedDoctor: doctor,
          showModal: true
        })
      })
  },

  // 关闭弹窗
  closeModal() {
    this.setData({
      showModal: false,
      selectedDoctor: null
    })
  },

  // 阻止事件冒泡
  stopPropagation() {
    // 空函数，用于阻止点击弹窗内容时关闭弹窗
  },

  // 确认挂号
  confirmRegister(e) {
    // 从 data 中获取 selectedDoctor，而不是从事件参数
    const doctor = this.data.selectedDoctor
    let doctorData = null
    
    if (this._submitting) {
      return
    }

    this._submitting = true

    if (!doctor || doctor.status !== 'AVAILABLE') {
      wx.showToast({
        title: '该时段已满',
        icon: 'none'
      })
      this._submitting = false
      return
    }

    userApi.checkUserInfo()
      .then(resp => {
        const data = resp && resp.data ? resp.data : {}
        const isValid = data.isValid === true || data.valid === true

        if (!isValid) {
          wx.showModal({
            title: '完善资料',
            content: '请先完善个人信息（姓名与手机号）后再预约挂号',
            confirmText: '去完善',
            cancelText: '取消',
            success: (res) => {
              if (res.confirm) {
                wx.navigateTo({ url: '/pages/edit-info/edit-info' })
              }
            }
          })
          this._submitting = false
          return Promise.reject({ message: '用户资料未完善' })
        }

        // 使用本地兜底规范化（后端不返回 normalized）
        const rawUserInfo = wx.getStorageSync('userInfo') || {}
        const local = validateUserInfo({
          name: rawUserInfo.name,
          phone: rawUserInfo.phone
        })
        const patientName = local.normalized.name
        const patientPhone = local.normalized.phone

        // 保存 doctor 对象，避免被 closeModal 清空
        doctorData = this.data.selectedDoctor

        // 关闭弹窗后再发起挂号
        this.closeModal()

        console.log('准备发起挂号，doctor对象:', doctorData)
        console.log('doctor对象的所有字段:', Object.keys(doctorData))
        console.log('doctorId字段值:', doctorData.doctorId)
        console.log('doctorId字段类型:', typeof doctorData.doctorId)
        console.log('doctorId是否为null:', doctorData.doctorId === null)
        console.log('doctorId是否为undefined:', doctorData.doctorId === undefined)
        console.log('doctor_id字段值:', doctorData.doctor_id)

        wx.showLoading({ title: '挂号中...' })

        const scheduleId = doctorData.id
        if (!scheduleId && scheduleId !== 0) {
          console.error('排班ID缺失，当前值:', scheduleId)
          throw new Error('排班ID缺失')
        }

        if (!this.data.selectedPatientId) {
          const list = this.data.patients
          if (!list || list.length === 0) {
            wx.showModal({ title: '请先添加就诊人', content: '需要先绑定至少一个就诊人', confirmText: '去添加', success: r=>{ if (r.confirm) wx.navigateTo({ url: '/pages/patients/patients' }) } })
            throw new Error('请选择就诊人')
          }
          const names = list.map(p=>`${p.name} (${p.relation||''})`)
          return new Promise((resolve, reject) => {
            wx.showActionSheet({ itemList: names, success: r=>{ const idx=r.tapIndex; const pid=list[idx]?.id; if (pid){ this.setData({ selectedPatientId: pid }); resolve(orderApi.createOrder(scheduleId, pid)) } else { reject(new Error('未选择就诊人')) } }, fail: ()=> reject(new Error('未选择就诊人')) })
          })
        }
        return orderApi.createOrder(scheduleId, this.data.selectedPatientId)
      })
      .then(res => {
        console.log('挂号接口成功返回，准备显示Toast', res)
        wx.hideLoading()
        
        console.log('确认挂号时，this.data.selectedDoctor:', this.data.selectedDoctor)
        console.log('当前doctor对象:', doctorData)

        // 延迟显示 Toast，避免与 hideLoading 冲突
        setTimeout(() => {
          wx.showToast({
            title: res.data || '挂号成功',
            icon: 'success',
            duration: 2000
          })
        }, 300)

        // 构建订单数据，使用后端返回的订单信息
        const rawUserInfo = wx.getStorageSync('userInfo') || {}
        const sel = (this.data.patients || []).find(p=>p.id===this.data.selectedPatientId) || {}
        const local = { normalized: { name: sel.name || rawUserInfo.name, phone: sel.phone || rawUserInfo.phone } }
      const data = res.data || {}
      const orderData = {
          orderNo: data.orderNo,
          scheduleId: data.scheduleId,
          amount: data.amount,
          status: data.status,
          expireTime: data.expireTime,
          createTime: data.createTime,
          patientName: local.normalized.name,
          doctorName: data.doctorName || doctorData.doctorName,
          departmentName: data.departmentName || this.data.selectedDeptName,
          registrationDate: doctorData.scheduleDate,
          timeSlot: `${doctorData.timeSlot} ${doctorData.startTime}-${doctorData.endTime}`,
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
        // 已移除审计上报接口调用
        
        // 重新加载医生列表
        this.loadDoctors(this.data.selectedDept)
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
  }
  ,

  onPullDownRefresh() {
    if (this.data.selectedDept) {
      this.loadDoctors(this.data.selectedDept)
    } else {
      this.loadDepartments()
    }
    wx.stopPullDownRefresh()
  }
})
