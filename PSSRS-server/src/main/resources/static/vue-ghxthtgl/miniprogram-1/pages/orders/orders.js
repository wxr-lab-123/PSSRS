// pages/orders/orders.js
const orderApi = require('../../api/order.js')
const { formatDateTime } = require('../../utils/format.js')
const i18n = require('../../utils/i18n.js')

Page({
  data: {
    orders: [],
    filteredOrders: [],
    currentTab: 'all',
    loading: false,
    refresherTriggered: false,
    search: { date: '' },
    i18n: {},
    langs: ['中文','English','Español','Français','العربية','Deutsch','Русский','日本語','한국어','Português'],
    langCodes: ['zh','en','es','fr','ar','de','ru','ja','ko','pt'],
    langIndex: 0
  },

  onLoad() {
    i18n.init()
    const code = i18n.getLang()
    const idx = Math.max(0, this.data.langCodes.indexOf(code))
    this.setData({ i18n: i18n.getBundle(), langIndex: idx })
    this.loadOrders()
  },

  onShow() {
    // 页面显示时刷新数据
    this.loadOrders()
  },

  // 加载订单数据（单页+筛选+搜索）
  loadOrders() {
    // 检查登录状态
    const token = wx.getStorageSync('token')
    if (!token) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      })
      setTimeout(() => {
        wx.navigateTo({
          url: '/pages/login/login'
        })
      }, 1500)
      return
    }

    if (this.data.loading) return
    this.setData({ loading: true })
    wx.showLoading({ title: '查询中...' })

    const { currentTab, search } = this.data
    const query = {
      status: currentTab === 'all' ? undefined : currentTab,
      date: search.date || undefined
    }

    orderApi.getOrderList(query)
      .then(res => {
        wx.hideLoading()
        console.log('API返回数据:', res) // 添加调试日志
        const data = res.data
        let records = []
        if (Array.isArray(data)) {
          records = data
        } else if (data && Array.isArray(data.list)) {
          records = data.list
        } else if (data && Array.isArray(data.records)) {
          records = data.records
        }
        
        // 检查records是否为数组
        if (!Array.isArray(records)) {
          console.error('返回的数据不是数组:', records)
          wx.showToast({
            title: '数据格式错误',
            icon: 'none'
          })
          this.setData({ loading: false, refresherTriggered: false })
          return
        }
        
        const statusMap = {
          '0': { text: '待支付', class: 'pending-pay' },
          '1': { text: '已支付', class: 'paid' },
          '2': { text: '已取消', class: 'cancelled' },
          '3': { text: '退款中', class: 'refunding' },
          '4': { text: '已退款', class: 'refunded' }
        }

        const orders = records.map(item => {
          const s = statusMap[String(item.status)] || { text: '未知状态', class: 'cancelled' }
          return {
            id: item.id,
            orderNo: item.orderNo,
            createTime: `${formatDateTime(item.createTime || item.createdAt)} (${i18n.t('common.tzLabel')})`,
            amount: Number(item.amount) || 0,
            status: String(item.status),
            statusClass: s.class,
            statusText: s.text,
            remark: item.remark || '',
            products: item.products || [],
            paymentMethod: item.paymentMethod || item.payWay || '',
            operations: item.operations || []
          }
        })

        console.log('解析前的记录:', records)
        console.log('格式化后的订单:', orders)

        this.setData({
          orders: orders,
          filteredOrders: orders,
          loading: false,
          refresherTriggered: false
        })
      })
      .catch(err => {
        wx.hideLoading()
        this.setData({ loading: false })
        console.error('查询订单失败:', err)
        wx.showToast({
          title: err?.msg || err?.message || '查询失败',
          icon: 'none'
        })
      })
  },

  // 切换筛选标签
  switchTab(e) {
    const tab = e.currentTarget.dataset.tab
    this.setData({ currentTab: tab })
    this.loadOrders()
  },

  // 应用筛选条件
  applyFilter() {
    const { orders, currentTab } = this.data
    let filteredOrders = orders

    console.log('筛选前订单数量:', orders.length)
    console.log('当前筛选标签:', currentTab)

    if (currentTab !== 'all') {
      filteredOrders = orders.filter(order => order.status === currentTab)
    }

    console.log('筛选后订单数量:', filteredOrders.length)
    this.setData({ filteredOrders })
  },

  // 查看订单详情
  viewDetail(e) {
    const item = e.currentTarget.dataset.item
    const products = (item.products || []).map(p => `${p.name || ''} x${p.qty || p.quantity || 1}`).join('\n') || '暂无'
    const ops = (item.operations || []).map(op => `${op.time || op.createTime || ''} - ${op.action || op.desc || ''}`).join('\n') || '暂无'
    const detailContent = `订单号：${item.orderNo}\n\n创建时间：${item.createTime || '未知'}\n金额：¥${item.amount}\n状态：${item.statusText}\n\n商品信息：\n${products}\n\n支付方式：${item.paymentMethod || '未知'}\n\n操作记录：\n${ops}`
    wx.showModal({ title: '订单详情', content: detailContent, showCancel: false, confirmText: '确定' })
  },

  // 立即支付
  payNow(e) {
    const item = e.currentTarget.dataset.item
    const payload = { orderNo: item.orderNo, price: item.amount, remark: item.remark || '' }
    const url = '/pages/payment/payment?orderData=' + encodeURIComponent(JSON.stringify(payload))
    wx.navigateTo({ url })
  },

  // 申请退款
  applyRefund(e) {
    const { orderNo } = e.currentTarget.dataset.item
    wx.showModal({
      title: '申请退款',
      content: `确定对订单 ${orderNo} 发起退款申请吗？`,
      confirmColor: '#faad14',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '提交中...' })
          orderApi.applyRefund(orderNo)
            .then(() => {
              wx.hideLoading()
              wx.showToast({ title: '已发起退款申请' })
              this.loadOrders(true)
            })
            .catch(err => {
              wx.hideLoading()
              wx.showToast({ title: err?.msg || err?.message || '申请失败', icon: 'none' })
            })
        }
      }
    })
  },

  // 搜索
  onDateChange(e) { this.setData({ 'search.date': e.detail.value }) },
  doSearch() { this.setData({ refresherTriggered: true }); this.loadOrders() },
  onLangPick(e) {
    const idx = Number(e.detail.value)
    const code = this.data.langCodes[idx]
    i18n.setLang(code)
    this.setData({ i18n: i18n.getBundle(), langIndex: idx })
  }
,
  // 下拉刷新控件（scroll-view）
  onScrollRefresh() {
    if (this.data.loading) { this.setData({ refresherTriggered: false }); return }
    this.setData({ refresherTriggered: true })
    this.loadOrders()
  },

  // 页面下拉刷新（备用）
  onPullDownRefresh() {
    if (this.data.loading) { wx.stopPullDownRefresh(); return }
    this.loadOrders()
  },

  goBack() { wx.navigateBack({ delta: 1 }) }
})
