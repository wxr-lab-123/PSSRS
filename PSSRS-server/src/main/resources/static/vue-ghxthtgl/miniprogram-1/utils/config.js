// utils/config.js
// API配置文件

// 后端服务器地址配置
const API_CONFIG = {
  // 开发环境
  development: {
    baseURL: 'http://localhost:8080',
    timeout: 10000
  },
  // 测试环境
  test: {
    baseURL: 'http://test.example.com:8080',
    timeout: 10000
  },
  // 生产环境
  production: {
    baseURL: 'https://api.example.com',
    timeout: 10000
  }
}

// 当前环境（可根据实际情况修改）
const ENV = 'development'

// 导出当前环境配置
module.exports = {
  baseURL: API_CONFIG[ENV].baseURL,
  timeout: API_CONFIG[ENV].timeout,
  
  // API路径配置
  API: {
    // 用户相关
    LOGIN: '/api/user/login',                    // 登录
    WECHAT_LOGIN: '/api/user/wechatLogin',       // 微信登录
    REGISTER: '/api/user/register',              // 注册
    LOGOUT: '/api/user/logout',                  // 退出登录
    GET_USER_INFO: '/api/user/info',             // 获取用户信息
    UPDATE_USER_INFO: '/api/user/update',        // 更新用户信息
    UPDATE_USER_PROFILE: '/api/user/profile',
    CHECK_USER_INFO: '/api/user/validate',       // 检测用户信息是否完善（后端校验）
    CHANGE_PASSWORD: '/api/user/changePassword',  // 修改密码
    
    // 短信验证码相关
    SEND_REGISTER_CODE: '/api/sms/sendRegisterCode',  // 发送注册验证码
    SEND_LOGIN_CODE: '/api/sms/sendLoginCode',        // 发送登录验证码
    SEND_RESET_PWD_CODE: '/api/sms/sendResetPwdCode', // 发送重置密码验证码
    RESET_PASSWORD: '/api/user/resetPassword',        // 重置密码
    
    // 科室相关
    GET_DEPARTMENTS: '/api/department/list',     // 获取科室列表
    GET_DEPARTMENT_INFO: '/api/department/info', // 获取科室详情
    
    // 医生相关
    GET_DOCTORS: '/api/user/doctor/list',        // 获取医生列表
    GET_DOCTOR_INFO: '/api/user/doctor/info',         // 获取医生详情
    GET_DOCTOR_SCHEDULE: '/api/user/doctor/schedule', // 获取医生排班
    GET_SCHEDULE_DETAIL: '/api/user/doctor/schedule/detail', // 获取排班详情
    
    // 挂号相关
    CREATE_REGISTRATION: '/api/registration/create',      // 创建挂号
    GET_REGISTRATION_LIST: '/api/registration/list',      // 获取挂号列表
    GET_REGISTRATION_INFO: '/api/registration/info',      // 获取挂号详情
    CANCEL_REGISTRATION: '/api/registration/cancel',      // 取消挂号
    
    // 预约相关
    CREATE_APPOINTMENT: '/api/appointment/create',        // 创建预约
    GET_APPOINTMENT_LIST: '/api/appointment/list',        // 获取预约列表
    GET_APPOINTMENT_INFO: '/api/appointment/info',        // 获取预约详情
    CANCEL_APPOINTMENT: '/api/appointment/cancel',        // 取消预约
    TAKE_NUMBER: '/api/appointment/takeNumber',           // 预约取号
    
    // 查询相关
    GET_REGISTRATION_RECORDS: '/api/query/registrations', // 查询挂号记录
    GET_PAYMENT_RECORDS: '/api/query/payments',           // 查询缴费记录
    GET_REPORT_LIST: '/api/query/reports',                // 查询检查报告
    GET_REPORT_DETAIL: '/api/query/report/detail',        // 查询报告详情
    
    // 订单相关
    GET_ORDER_LIST: '/api/order/list',                   // 获取订单列表（支持查询）
    GET_ORDER_DETAIL: '/api/order/detail',               // 查询订单详情
    CREATE_ORDER: '/api/order/create',                   // 创建挂号订单
    CREATE_PAYMENT: '/api/payment/create',               // 创建支付订单
    APPLY_REFUND: '/api/order/refund/apply'              // 申请退款
    ,GET_REFUND_STATUS: '/api/order/refund/status'       // 退款进度查询
    ,GET_MESSAGES: '/api/user/messages'
    ,UNREAD_COUNT: '/api/user/messages/unread-count'
    ,MARK_MESSAGE_READ: (id) => `/api/user/messages/${id}/read`
    ,CLEAR_MESSAGES: (id) => `/api/user/messages/${id}`
    ,USER_PATIENTS: '/api/user/patients'
    ,SWITCH_PATIENT: '/api/user/switchPatient'
  }
}
