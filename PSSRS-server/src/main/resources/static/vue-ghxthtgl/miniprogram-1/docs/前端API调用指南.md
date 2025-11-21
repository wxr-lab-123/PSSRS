# 前端API调用指南

## 目录结构

```
miniprogram-1/
├── api/                    # API模块目录
│   ├── user.js            # 用户相关API
│   ├── department.js      # 科室相关API
│   ├── doctor.js          # 医生相关API
│   ├── registration.js    # 挂号相关API
│   ├── appointment.js     # 预约相关API
│   └── query.js           # 查询相关API
├── utils/                 # 工具目录
│   ├── config.js          # API配置
│   └── request.js         # HTTP请求封装
└── pages/                 # 页面目录
```

## 配置说明

### 1. 修改服务器地址

编辑 `utils/config.js` 文件：

```javascript
const API_CONFIG = {
  // 开发环境
  development: {
    baseURL: 'http://localhost:8080',  // 修改为你的后端地址
    timeout: 10000
  },
  // 生产环境
  production: {
    baseURL: 'https://api.example.com',  // 修改为生产环境地址
    timeout: 10000
  }
}

// 切换环境
const ENV = 'development'  // 或 'production'
```

### 2. 配置小程序合法域名

在微信小程序后台配置服务器域名：
1. 登录[微信小程序后台](https://mp.weixin.qq.com/)
2. 开发 -> 开发管理 -> 开发设置 -> 服务器域名
3. 添加你的后端域名（必须是HTTPS）

## API调用示例

### 1. 用户登录

```javascript
const userApi = require('../../api/user.js')

// 调用登录API
userApi.login('13800138000', '123456')
  .then(res => {
    // 登录成功
    console.log('用户信息:', res.data)
    
    // 保存token
    wx.setStorageSync('token', res.data.token)
    
    // 保存用户信息
    wx.setStorageSync('userInfo', res.data)
  })
  .catch(err => {
    // 登录失败
    console.error('登录失败:', err)
  })
```

### 2. 用户注册

```javascript
const userApi = require('../../api/user.js')

userApi.register({
  name: '张三',
  phone: '13800138000',
  idCard: '110101199001011234',
  password: '123456'
})
  .then(res => {
    console.log('注册成功')
  })
  .catch(err => {
    console.error('注册失败:', err)
  })
```

### 3. 获取科室列表

```javascript
const departmentApi = require('../../api/department.js')

departmentApi.getDepartments()
  .then(res => {
    // 更新页面数据
    this.setData({
      departments: res.data
    })
  })
  .catch(err => {
    console.error('获取科室列表失败:', err)
  })
```

### 4. 获取医生列表

```javascript
const doctorApi = require('../../api/doctor.js')

// 获取指定科室的医生
doctorApi.getDoctors(1, '2024-11-04')
  .then(res => {
    this.setData({
      doctors: res.data
    })
  })
  .catch(err => {
    console.error('获取医生列表失败:', err)
  })
```

### 5. 创建挂号

```javascript
const registrationApi = require('../../api/registration.js')

registrationApi.createRegistration({
  doctorId: 1,
  departmentId: 1,
  registrationDate: '2024-11-04',
  timeSlot: '08:00',
  patientName: '张三',
  patientPhone: '13800138000'
})
  .then(res => {
    wx.showToast({
      title: '挂号成功',
      icon: 'success'
    })
    console.log('挂号信息:', res.data)
  })
  .catch(err => {
    console.error('挂号失败:', err)
  })
```

### 6. 获取挂号列表

```javascript
const registrationApi = require('../../api/registration.js')

registrationApi.getRegistrationList(1, 10, '待就诊')
  .then(res => {
    this.setData({
      registrations: res.data.list,
      total: res.data.total
    })
  })
  .catch(err => {
    console.error('获取挂号列表失败:', err)
  })
```

### 7. 创建预约

```javascript
const appointmentApi = require('../../api/appointment.js')

appointmentApi.createAppointment({
  doctorId: 1,
  departmentId: 1,
  appointmentDate: '2024-11-05',
  timeSlot: '08:00',
  patientName: '张三',
  patientPhone: '13800138000'
})
  .then(res => {
    wx.showToast({
      title: '预约成功',
      icon: 'success'
    })
  })
  .catch(err => {
    console.error('预约失败:', err)
  })
```

### 8. 预约取号

```javascript
const appointmentApi = require('../../api/appointment.js')

appointmentApi.takeNumber(1, 'YY202411050001')
  .then(res => {
    wx.showToast({
      title: '取号成功',
      icon: 'success'
    })
    console.log('排队号:', res.data.queueNumber)
  })
  .catch(err => {
    console.error('取号失败:', err)
  })
```

### 9. 查询挂号记录

```javascript
const queryApi = require('../../api/query.js')

queryApi.getRegistrationRecords(1, 10)
  .then(res => {
    this.setData({
      records: res.data.list
    })
  })
  .catch(err => {
    console.error('查询失败:', err)
  })
```

## 请求封装说明

### 基础请求方法

`utils/request.js` 提供了以下方法：

```javascript
const request = require('../utils/request.js')

// GET请求
request.get(url, data, options)

// POST请求
request.post(url, data, options)

// PUT请求
request.put(url, data, options)

// DELETE请求
request.del(url, data, options)

// 文件上传
request.upload(url, filePath, formData, options)
```

### 请求选项

```javascript
{
  showLoading: true,          // 是否显示loading，默认true
  loadingText: '加载中...',   // loading文字
  timeout: 10000,             // 超时时间，默认10秒
  header: {}                  // 自定义请求头
}
```

### 示例

```javascript
const request = require('../utils/request.js')

// 不显示loading
request.get('/api/user/info', {}, {
  showLoading: false
})

// 自定义loading文字
request.post('/api/registration/create', data, {
  loadingText: '挂号中...'
})

// 自定义请求头
request.get('/api/data', {}, {
  header: {
    'Custom-Header': 'value'
  }
})
```

## 错误处理

### 统一错误处理

请求封装已经实现了统一的错误处理：

1. **业务错误**：自动显示错误提示
2. **网络错误**：显示"网络连接失败"
3. **401未授权**：自动跳转到登录页

### 自定义错误处理

```javascript
userApi.login(phone, password)
  .then(res => {
    // 成功处理
  })
  .catch(err => {
    // 自定义错误处理
    if (err.code === 1001) {
      wx.showModal({
        title: '提示',
        content: '账号或密码错误'
      })
    }
  })
```

## Token管理

### Token自动添加

请求封装会自动从本地存储读取token并添加到请求头：

```javascript
// 登录成功后保存token
wx.setStorageSync('token', res.data.token)

// 后续请求会自动携带token
```

### Token过期处理

当收到401状态码时，会自动：
1. 清除本地token和用户信息
2. 显示"登录已过期"提示
3. 2秒后跳转到登录页

## 调试技巧

### 1. 开启详细日志

在 `utils/request.js` 中添加日志：

```javascript
success: (res) => {
  console.log('请求成功:', url, res)
  // ...
},
fail: (err) => {
  console.error('请求失败:', url, err)
  // ...
}
```

### 2. 使用微信开发者工具

1. 打开调试器
2. Network标签查看请求详情
3. Console标签查看日志

### 3. 模拟数据

在开发阶段可以先使用模拟数据：

```javascript
// 临时注释掉API调用
// doctorApi.getDoctors(deptId, date)

// 使用模拟数据
this.setData({
  doctors: [
    { id: 1, name: '张医生', ... }
  ]
})
```

## 常见问题

### Q1: 请求失败，提示"不在以下 request 合法域名列表中"

**解决方案**：
1. 开发阶段：开发者工具 -> 详情 -> 本地设置 -> 勾选"不校验合法域名"
2. 生产环境：在小程序后台配置合法域名

### Q2: 请求超时

**解决方案**：
1. 检查后端服务是否启动
2. 检查网络连接
3. 增加timeout时间

### Q3: Token失效

**解决方案**：
1. 检查token是否正确保存
2. 检查后端token验证逻辑
3. 实现token自动刷新机制

### Q4: 跨域问题

**解决方案**：
小程序不存在跨域问题，如果遇到，检查：
1. 后端CORS配置
2. 请求域名是否正确

## 最佳实践

### 1. 统一管理API

将所有API调用集中在 `api/` 目录下，便于维护。

### 2. 错误处理

```javascript
try {
  const res = await userApi.login(phone, password)
  // 处理成功
} catch (err) {
  // 处理错误
  console.error(err)
}
```

### 3. Loading状态

```javascript
this.setData({ loading: true })

userApi.login(phone, password)
  .then(res => {
    // 处理数据
  })
  .finally(() => {
    this.setData({ loading: false })
  })
```

### 4. 数据缓存

```javascript
// 缓存科室列表
const cache = wx.getStorageSync('departments')
if (cache) {
  this.setData({ departments: cache })
} else {
  departmentApi.getDepartments()
    .then(res => {
      this.setData({ departments: res.data })
      wx.setStorageSync('departments', res.data)
    })
}
```

---

**文档版本**: V1.0  
**更新日期**: 2024-11-03
