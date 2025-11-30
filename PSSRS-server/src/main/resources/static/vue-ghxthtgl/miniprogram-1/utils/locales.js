// e:\webHD\myMeaven\PSSRS1\PSSRS-server\src\main\resources\static\vue-ghxthtgl\miniprogram-1\utils\locales.js
module.exports = {
  zh: {
    orders: {
      title: '订单查询',
      filter: { all: '全部', pending: '待支付', paid: '已支付', cancelled: '已取消', refunding: '退款中', refunded: '已退款' },
      selectDate: '选择日期',
      search: '搜索',
      fields: { orderNo: '订单号', createTime: '创建时间', amount: '订单金额', remark: '备注' },
      buttons: { detail: '查看详情', pay: '立即支付', refund: '申请退款' },
      tips: { refunding: '退款处理中...' },
      empty: '暂无订单记录'
    },
    common: { loading: '加载中...', requestFailed: '请求失败', networkError: '网络连接失败，请检查网络', unauthorized: '请先登录', tzLabel: 'CST/GMT+8' }
  },
  en: {
    orders: {
      title: 'Order Query',
      filter: { all: 'All', pending: 'Pending', paid: 'Paid', cancelled: 'Cancelled', refunding: 'Refunding', refunded: 'Refunded' },
      selectDate: 'Select date',
      search: 'Search',
      fields: { orderNo: 'Order No.', createTime: 'Created At', amount: 'Amount', remark: 'Remark' },
      buttons: { detail: 'View Details', pay: 'Pay Now', refund: 'Request Refund' },
      tips: { refunding: 'Refund in progress...' },
      empty: 'No orders'
    },
    common: { loading: 'Loading...', requestFailed: 'Request failed', networkError: 'Network error, please check your connection', unauthorized: 'Please log in', tzLabel: 'CST/GMT+8' }
  }
}