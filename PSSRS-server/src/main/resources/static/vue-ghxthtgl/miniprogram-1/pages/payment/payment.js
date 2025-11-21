// pages/payment/payment.js
const orderApi = require('../../api/order.js')

Page({
  data: {
    orderInfo: null,
    loading: false
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
    if (!this.data.orderInfo) {
      wx.showToast({
        title: '订单信息异常',
        icon: 'none'
      })
      return
    }

    wx.showLoading({ title: '支付中...' })
    
    // 调用后端创建支付订单（只传订单号）
    orderApi.createPayment(this.data.orderInfo.orderNo)
    .then(res => {
      wx.hideLoading()
      wx.showModal({
        title: '支付成功',
        content: '您已成功支付挂号费用',
        showCancel: false,
        confirmText: '返回首页',
        success: () => {
          wx.switchTab({ url: '/pages/index/index' })
        }
      })
    })
    .catch(err => {
      wx.hideLoading()
      console.error('支付失败:', err)
      wx.showToast({
        title: '支付失败，请重试',
        icon: 'none'
      })
    })
  },

  // 返回上一页
  goBack() {
    wx.navigateBack()
  }
})
