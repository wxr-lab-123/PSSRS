// pages/payment/payment.js
const orderApi = require('../../api/order.js')

Page({
  data: {
    orderInfo: null,
    loading: false,
    selectedMethod: 'WECHAT',
    payPassword: ''
  },

  onLoad(options) {
    // 接收传递的订单数据（挂号记录）
    if (options.orderData) {
      try {
        const orderInfo = JSON.parse(decodeURIComponent(options.orderData))
        this.setData({ orderInfo })
      } catch (e) {
        console.error('解析订单数据失败:', e)
        wx.showToast({
          title: '订单数据异常',
          icon: 'none'
        })
      }
    } else {
      wx.showToast({
        title: '缺少订单信息',
        icon: 'none'
      })
    }
  },

  // 立即支付
  handlePay() {
    const { orderInfo, selectedMethod } = this.data
    if (!orderInfo) {
      wx.showToast({ title: '订单信息异常', icon: 'none' })
      return
    }
    wx.showModal({
      title: '确认支付',
      content: `确认使用${selectedMethod==='WECHAT'?'微信支付':'支付宝'}支付 ¥${orderInfo.price}？`,
      confirmColor: '#1890ff',
      success: (res) => {
        if (!res.confirm) return
        wx.showLoading({ title: '支付中...' })
        orderApi.createPayment(orderInfo.orderNo, selectedMethod)
          .then(() => {
            wx.hideLoading()
            wx.showToast({ title: '支付成功', icon: 'success' })
            setTimeout(() => { wx.navigateBack({ delta: 1 }) }, 800)
          })
          .catch(err => {
            wx.hideLoading()
            wx.showModal({ title: '支付失败', content: err?.msg || err?.message || '支付失败，请重试', showCancel: true })
          })
      }
    })
  },

  onMethodChange(e) { this.setData({ selectedMethod: e.detail.value }) },
  onPasswordInput(e) { this.setData({ payPassword: e.detail.value }) },
  // 返回上一页
  goBack() { wx.navigateBack() }
})
