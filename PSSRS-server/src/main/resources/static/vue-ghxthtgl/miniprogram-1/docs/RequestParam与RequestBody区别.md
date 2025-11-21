# @RequestParam 与 @RequestBody 的区别

## 概述

在Spring Boot中，接收前端参数有两种主要方式：
- `@RequestParam` - 接收URL参数或表单数据
- `@RequestBody` - 接收JSON格式的请求体

## 区别对比

| 特性 | @RequestParam | @RequestBody |
|------|--------------|--------------|
| 数据格式 | URL参数、表单 | JSON |
| Content-Type | application/x-www-form-urlencoded | application/json |
| 请求方式 | GET/POST | POST/PUT |
| 数据位置 | URL或表单 | 请求体 |
| 适用场景 | 简单参数 | 复杂对象 |

## 后端实现

### 方式1：使用 @RequestParam（当前采用）

```java
@PostMapping("/sendRegisterCode")
public Result sendCode(@RequestParam("phone") String phone, HttpSession session) {
    return patientService.sendCode(phone, session);
}
```

**特点**:
- 参数从URL或表单中获取
- 支持GET和POST请求
- 适合简单参数

### 方式2：使用 @RequestBody

```java
@PostMapping("/sendRegisterCode")
public Result sendCode(@RequestBody SmsDTO dto, HttpSession session) {
    return patientService.sendCode(dto.getPhone(), session);
}
```

**特点**:
- 参数从JSON请求体中获取
- 只支持POST/PUT请求
- 适合复杂对象

## 前端调用方式

### 适配 @RequestParam（当前方式）

#### 方式A：使用POST + URL参数（推荐）✅
```javascript
// api/sms.js
function sendRegisterCode(phone) {
  return request.post('/api/sms/sendRegisterCode?phone=' + phone, {})
}
```

**实际请求**:
```
POST /api/sms/sendRegisterCode?phone=13800138000
Content-Type: application/json
```

**说明**: 
- 使用POST请求（符合后端@PostMapping）
- 参数放在URL中（符合@RequestParam）
- 请求体为空对象

#### 方式B：使用GET请求
```javascript
function sendRegisterCode(phone) {
  return request.post('/api/sms/sendRegisterCode', {
    phone: phone
  }, {
    header: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}
```

**实际请求**:
```
POST /api/sms/sendRegisterCode
Content-Type: application/x-www-form-urlencoded

phone=13800138000
```

### 适配 @RequestBody

```javascript
// api/sms.js
function sendRegisterCode(phone) {
  return request.post('/api/sms/sendRegisterCode', {
    phone: phone
  })
}
```

**实际请求**:
```
POST /api/sms/sendRegisterCode
Content-Type: application/json

{"phone":"13800138000"}
```

## 微信小程序中的实现

### 当前实现（适配 @RequestParam）

```javascript
// utils/request.js
function get(url, data = {}, options = {}) {
  // 将data转换为URL参数
  const params = Object.keys(data)
    .map(key => `${key}=${encodeURIComponent(data[key])}`)
    .join('&')
  
  const fullUrl = params ? `${baseURL}${url}?${params}` : `${baseURL}${url}`
  
  return new Promise((resolve, reject) => {
    wx.request({
      url: fullUrl,
      method: 'GET',
      header: {
        'Authorization': 'Bearer ' + token
      },
      success: (res) => {
        if (res.data.code === 200) {
          resolve(res.data)
        } else {
          reject(res.data)
        }
      },
      fail: reject
    })
  })
}
```

### 调用示例

```javascript
// pages/login/login.js
sendRegisterCode() {
  const { registerPhone } = this.data
  
  // 调用API
  smsApi.sendRegisterCode(registerPhone)
    .then(res => {
      wx.showToast({ title: '验证码已发送' })
      this.startCountdown()
    })
    .catch(err => {
      wx.showToast({ 
        title: err.message || '发送失败',
        icon: 'none'
      })
    })
}
```

## 测试验证

### 使用Postman测试

#### 测试 @RequestParam
```
方式1: GET请求
GET http://localhost:8080/api/sms/sendRegisterCode?phone=13800138000

方式2: POST + 表单
POST http://localhost:8080/api/sms/sendRegisterCode
Content-Type: application/x-www-form-urlencoded

phone=13800138000
```

#### 测试 @RequestBody
```
POST http://localhost:8080/api/sms/sendRegisterCode
Content-Type: application/json

{
  "phone": "13800138000"
}
```

## 实际网络请求

### 使用 @RequestParam（GET方式）
```
Request URL: http://localhost:8080/api/sms/sendRegisterCode?phone=13800138000
Request Method: GET
Request Headers:
  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 使用 @RequestBody（POST方式）
```
Request URL: http://localhost:8080/api/sms/sendRegisterCode
Request Method: POST
Request Headers:
  Content-Type: application/json
  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Request Body:
  {"phone":"13800138000"}
```

## 选择建议

### 使用 @RequestParam 的场景
- ✅ 参数简单（1-3个参数）
- ✅ 需要支持GET请求
- ✅ 参数可以暴露在URL中
- ✅ 发送验证码、查询列表等

### 使用 @RequestBody 的场景
- ✅ 参数复杂（多个字段）
- ✅ 只需要POST请求
- ✅ 参数不应暴露在URL中
- ✅ 用户注册、创建订单等

## 当前项目配置

### 验证码接口（使用 @RequestParam）
```java
@PostMapping("/sendRegisterCode")
public Result sendCode(@RequestParam("phone") String phone, HttpSession session)
```

**原因**:
- 只有一个参数（手机号）
- 参数简单
- 支持GET请求更方便

### 注册接口（使用 @RequestBody）
```java
@PostMapping("/register")
public Result register(@RequestBody RegisterDTO dto)
```

**原因**:
- 多个参数（姓名、手机号、验证码、身份证、密码）
- 参数复杂
- 密码等敏感信息不应暴露在URL中

## 总结

**当前项目配置**:
- ✅ 验证码接口：使用 `@RequestParam` + GET请求
- ✅ 注册接口：使用 `@RequestBody` + POST请求
- ✅ 前端已适配，使用 `request.get()` 发送验证码

**优势**:
- 验证码接口更简洁
- 注册接口更安全
- 符合RESTful规范

---

**文档版本**: V1.0  
**更新日期**: 2024-11-03
