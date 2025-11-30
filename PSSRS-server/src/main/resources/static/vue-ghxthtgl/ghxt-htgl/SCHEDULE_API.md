# 排班管理接口文档

## 基础信息

- **基础路径**: `/api/admin/schedules`
- **认证方式**: Bearer Token (请求头 `Authorization`)
- **数据格式**: JSON

---

## 接口列表

### 1. 获取排班列表

**接口地址**: `GET /api/admin/schedules`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | number | 否 | 页码，默认1 |
| limit | number | 否 | 每页数量，默认10 |
| doctor_id / doctorId | number | 否 | 医生ID（同时支持下划线与驼峰） |
| department_id / departmentId | number | 否 | 科室ID（同时支持下划线与驼峰） |
| schedule_date / scheduleDate | string | 否 | 排班日期，格式：YYYY-MM-DD（同时支持下划线与驼峰） |
| time_slot / timeSlot / period / slot | string | 否 | 时间段：支持中文“上午/下午/晚上”，或英文枚举 MORNING/AFTERNOON/EVENING |
| status | string | 否 | 状态：AVAILABLE/FULL/CANCELLED |
| schedule_type / scheduleType | string | 否 | 号源类型：normal=普通号，expert=专家号 |

> 说明：为兼容不同实现，服务端可同时接收下划线与驼峰命名。时间段参数也支持别名 `period/slot` 与英文枚举值。

**请求示例**:
```http
GET /api/admin/schedules?page=1&limit=10&departmentId=1&scheduleDate=2025-11-10&timeSlot=上午&status=AVAILABLE
Authorization: Bearer <token>
```

**响应示例**:
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "doctorId": 10,
        "doctorName": "张医生",
        "departmentId": 2,
        "departmentName": "内科",
        "scheduleDate": "2025-11-10",
        "timeSlot": "上午", // 也可能返回 MORNING/AFTERNOON/EVENING
        "startTime": "08:00:00",
        "endTime": "12:00:00",
        "maxAppointments": 20,
        "currentAppointments": 5,
        "scheduleType": "normal",
        "status": "AVAILABLE",
        "roomNumber": "101",
        "notes": "正常出诊",
        "createdAt": "2025-11-05 09:00:00",
        "updatedAt": "2025-11-05 09:00:00"
      }
    ],
    "total": 100,
    "page": 1,
    "limit": 10
  }
}
```

---

# 认证与用户

## 登录

- 接口地址: `POST /api/admin/auth/login`
- 请求体: `{ username: string, password: string }`
- 响应: `{ token: string, user: { id, username, roles: string[] } }`

## 退出登录

- 接口地址: `POST /api/admin/auth/logout`

---

# 管理员（用户）管理

基础路径: `/api/admin/admins`

### 1) 管理员列表

**接口地址**: `GET /api/admin/admins`

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | number | 否 | 页码，默认1 |
| size | number | 否 | 每页数量，默认10 |
| username | string | 否 | 用户名模糊匹配 |
| status | number | 否 | 状态：1=启用，0=禁用 |

**响应示例**:
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "username": "admin",
        "name": "系统管理员",
        "phone": "13800000000",
        "gender": "0",
        "roles": ["ADMIN"],
        "status": 1
      }
    ],
    "total": 1,
    "page": 1,
    "size": 10
  }
}
```

### 2) 新增管理员

**接口地址**: `POST /api/admin/admins`

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | string | 是 | 用户名（唯一） |
| password | string | 是 | 初始密码 |
| name | string | 是 | 姓名 |
| phone | string | 否 | 手机号 |
| gender | string | 否 | 性别：0=男，1=女 |
| roles | string[] | 是 | 角色数组，如 ["ADMIN","DOCTOR"] |

**请求示例**:
```json
{
  "username": "manager01",
  "password": "123456",
  "name": "张三",
  "phone": "13800138000",
  "gender": "0",
  "roles": ["ADMIN"]
}
```

**响应示例**:
```json
{ "code": 0, "msg": "创建成功", "data": { "id": 12 } }
```

### 3) 更新管理员

**接口地址**: `PUT /api/admin/admins/{id}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | number | 是 | 管理员ID |

**请求体**:（部分字段可选）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | string | 否 | 姓名 |
| phone | string | 否 | 手机号 |
| gender | string | 否 | 性别：0/1 |
| roles | string[] | 否 | 角色数组 |
| status | number | 否 | 1=启用，0=禁用 |

**请求示例**:
```json
{ "name": "张三(修改)", "roles": ["ADMIN","DOCTOR"], "status": 1 }
```

**响应示例**:
```json
{ "code": 0, "msg": "更新成功", "data": null }
```

### 4) 删除管理员

**接口地址**: `DELETE /api/admin/admins/{id}`

**路径参数**: 同上

**响应示例**:
```json
{ "code": 0, "msg": "删除成功", "data": null }
```

### 5) 启用/禁用管理员（可选）

**接口地址**: `PATCH /api/admin/admins/{id}/status`

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | number | 是 | 1=启用，0=禁用 |

**示例**:
```http
PATCH /api/admin/admins/12/status?status=0
```

### 6) 重置密码（可选）

**接口地址**: `POST /api/admin/admins/{id}/reset-password`

**响应**:
```json
{ "code": 0, "msg": "密码已重置", "data": null }
```

---

# 角色管理

- 获取角色列表：`GET /api/admin/roles`
  - 响应示例：`[{ "roleName": "ADMIN" }, { "roleName": "DOCTOR" }]`

---

# 科室管理

基础路径: `/api/admin/departments`

- 分页列表：`GET /departments`
- 树形：`GET /departments?tree=true`
- 平铺列表：`GET /departments/list`
- 详情：`GET /departments/{id}`
- 新增：`POST /departments`
- 更新：`PUT /departments/{id}`
- 删除：`DELETE /departments/{id}`

---

# 医生管理

基础路径: `/api/admin/doctors`

- 列表：`GET /doctors`
- 新增：`POST /doctors`
- 更新：`PUT /doctors/{id}`
- 删除：`DELETE /doctors/{id}`
- 按科室查询：`GET /doctors/department/{departmentId}`

---

# 患者管理

基础路径: `/api/admin/patients`

- 列表：`GET /patients`
- 详情：`GET /patients/{id}`
- 新增：`POST /patients`
- 更新：`PUT /patients/{id}`
- 删除：`DELETE /patients/{id}`

---

# 订单/挂号

## 管理员挂号列表

- **接口地址**: `GET /api/admin/registrations`

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | number | 否 | 页码，默认 1 |
| size | number | 否 | 每页数量，默认 10 |
| patientName | string | 否 | 患者姓名，模糊匹配 |
| doctorName | string | 否 | 医生姓名，模糊匹配 |
| departmentName | string | 否 | 科室名称，模糊匹配 |
| status | string/number | 否 | 挂号状态：0=待就诊，1=已完成，2=已取消 |
| startDate | string | 否 | 开始日期，格式：YYYY-MM-DD |
| endDate | string | 否 | 结束日期，格式：YYYY-MM-DD |

**响应示例**:

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "records": [
      {
        "orderNo": "GH202511210001",
        "patientName": "张三",
        "doctorName": "李医生",
        "departmentName": "内科",
        "appointmentDate": "2025-11-21",
        "timeSlot": "上午",
        "fee": 20.0,
        "status": 0
      }
    ],
    "total": 100,
    "page": 1,
    "size": 10
  }
}
```

> 服务端可根据需要扩展字段，如就诊卡号、挂号来源（线上/线下）、创建时间等。

---

## 管理员订单列表

- **接口地址**: `GET /api/admin/orders/list`

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | number | 否 | 页码，默认 1 |
| size | number | 否 | 每页数量，默认 10 |
| orderNo | string | 否 | 订单号，精确或模糊匹配（视实现而定） |
| patientName | string | 否 | 患者姓名，模糊匹配 |
| status | string/number | 否 | 支付状态：0=待支付，1=已支付，2=已退款 |
| startDate | string | 否 | 开始日期（支付时间范围），格式：YYYY-MM-DD |
| endDate | string | 否 | 结束日期（支付时间范围），格式：YYYY-MM-DD |

**响应示例**:

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "records": [
      {
        "orderNo": "DD202511210001",
        "patientName": "张三",
        "amount": 20.0,
        "payMethod": "微信支付",
        "payTime": "2025-11-21 09:30:00",
        "status": 1
      }
    ],
    "total": 80,
    "page": 1,
    "size": 10
  }
}
```

> 建议 `status` 与前端显示文案的映射保持一致：0=待支付，1=已支付，2=已退款。如需支持更多支付状态，可在前后端共同扩展。

---

## 医生订单列表

- 医生订单列表：`GET /api/admin/orders`（按需携带查询参数，如医生 ID、日期、状态等）

---

# 个人中心

基础路径: `/api/admin/user`

- 获取资料：`GET /user/profile`
- 更新资料：`PUT /user/profile`
- 修改密码：`POST /user/password`
- 绑定手机号：`POST /user/phone/bind`
- 更新头像：`PUT /user/avatar`（请求体：`{ avatar: string }`）

---

# 文件上传

- 上传文件到 OSS：`POST /api/admin/files/upload`
  - 表单：`file`、可选 `folder`
  - 响应：`{ url: string, path: string }`
  - 用途：如医生头像：前端再写入资料接口

---

### 2. 获取排班详情

**接口地址**: `GET /api/admin/schedules/{id}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | number | 是 | 排班ID |

**请求示例**:
```http
GET /api/admin/schedules/1
Authorization: Bearer <token>
```

**响应示例**:
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": 1,
    "doctor_id": 10,
    "doctor_name": "张医生",
    "department_id": 2,
    "department_name": "内科",
    "schedule_date": "2025-11-10",
    "time_slot": "上午",
    "start_time": "08:00:00",
    "end_time": "12:00:00",
    "max_appointments": 20,
    "current_appointments": 5,
    "available_appointments": 15,
    "status": "AVAILABLE",
    "room_number": "101",
    "notes": "正常出诊",
    "created_at": "2025-11-05 09:00:00",
    "updated_at": "2025-11-05 09:00:00",
    "created_by": 1,
    "updated_by": 1
  }
}
```

---

### 3. 创建排班

**接口地址**: `POST /api/admin/schedules`

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| doctor_id | number | 是 | 医生ID |
| department_id | number | 是 | 科室ID |
| schedule_date | string | 是 | 排班日期，格式：YYYY-MM-DD |
| time_slot | string | 是 | 时间段：上午/下午/晚上 |
| start_time | string | 是 | 开始时间，格式：HH:mm:ss |
| end_time | string | 是 | 结束时间，格式：HH:mm:ss |
| max_appointments | number | 是 | 最大预约数 |
| room_number | string | 否 | 诊室号 |
| notes | string | 否 | 备注 |

**请求示例**:
```json
{
  "doctor_id": 10,
  "department_id": 2,
  "schedule_date": "2025-11-10",
  "time_slot": "上午",
  "start_time": "08:00:00",
  "end_time": "12:00:00",
  "max_appointments": 20,
  "room_number": "101",
  "notes": "正常出诊"
}
```

**响应示例**:
```json
{
  "code": 0,
  "msg": "创建成功",
  "data": {
    "id": 1
  }
}
```

---

### 4. 批量创建排班

**接口地址**: `POST /api/admin/schedules/batch`

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| doctor_id | number | 是 | 医生ID |
| department_id | number | 是 | 科室ID |
| dates | array | 是 | 日期数组，格式：["2025-11-10", "2025-11-11"] |
| time_slots | array | 是 | 时间段配置数组 |
| time_slots[].time_slot | string | 是 | 时间段：上午/下午/晚上 |
| time_slots[].start_time | string | 是 | 开始时间，格式：HH:mm:ss |
| time_slots[].end_time | string | 是 | 结束时间，格式：HH:mm:ss |
| max_appointments | number | 是 | 最大预约数 |
| room_number | string | 否 | 诊室号 |
| schedule_type | string | 否 | 号源类型：normal=普通号，expert=专家号（默认 normal） |

**请求示例**:
```json
{
  "doctor_id": 10,
  "department_id": 2,
  "dates": ["2025-11-10", "2025-11-11", "2025-11-12"],
  "time_slots": [
    {
      "time_slot": "上午",
      "start_time": "08:00:00",
      "end_time": "12:00:00"
    },
    {
      "time_slot": "下午",
      "start_time": "14:00:00",
      "end_time": "18:00:00"
    }
  ],
  "max_appointments": 20,
  "room_number": "101",
  "schedule_type": "normal"
}
```

**响应示例**:
```json
{
  "code": 0,
  "msg": "批量创建成功",
  "data": {
    "created_count": 6,
    "ids": [1, 2, 3, 4, 5, 6]
  }
}
```

---

### 5. 更新排班

**接口地址**: `PUT /api/admin/schedules`

**请求体**:（编辑时需携带 id，其他字段可选，只传需要更新的字段）

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | number | 是 | 排班ID（Long） |
| doctor_id | number | 否 | 医生ID |
| department_id | number | 否 | 科室ID |
| schedule_date | string | 否 | 排班日期 |
| time_slot | string | 否 | 时间段 |
| start_time | string | 否 | 开始时间 |
| end_time | string | 否 | 结束时间 |
| max_appointments | number | 否 | 最大预约数 |
| room_number | string | 否 | 诊室号 |
| notes | string | 否 | 备注 |
| status | string | 否 | 状态 |

**请求示例**:
```json
{
  "id": 1,
  "max_appointments": 25,
  "room_number": "102",
  "notes": "增加预约数"
}
```

**响应示例**:
```json
{
  "code": 0,
  "msg": "更新成功",
  "data": null
}
```

---

### 6. 删除排班

**接口地址**: `DELETE /api/admin/schedules/{id}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | number | 是 | 排班ID |

**请求示例**:
```http
DELETE /api/admin/schedules/1
Authorization: Bearer <token>
```

**响应示例**:
```json
{
  "code": 0,
  "msg": "删除成功",
  "data": null
}
```

---

### 7. 批量删除排班

**接口地址**: `POST /api/admin/schedules/batch-delete`

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ids | array | 是 | 排班ID数组 |

**请求示例**:
```json
{
  "ids": [1, 2, 3, 4, 5]
}
```

**响应示例**:
```json
{
  "code": 0,
  "msg": "批量删除成功",
  "data": {
    "deleted_count": 5
  }
}
```

---

### 8. 更新排班状态

**接口地址**: `PATCH /api/admin/schedules/{id}/status`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | number | 是 | 排班ID |

**查询参数（Query）**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | string | 是 | 状态：AVAILABLE/FULL/CANCELLED |

**请求示例**:
```http
PATCH /api/admin/schedules/1/status?status=CANCELLED
Authorization: Bearer <token>
```

**响应示例**:
```json
{
  "code": 0,
  "msg": "状态更新成功",
  "data": null
}
```

---

### 9. 获取医生排班日历

**接口地址**: `GET /api/admin/schedules/doctor/{doctorId}/calendar`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| doctorId | number | 是 | 医生ID |

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| start_date | string | 是 | 开始日期，格式：YYYY-MM-DD |
| end_date | string | 是 | 结束日期，格式：YYYY-MM-DD |

**请求示例**:
```http
GET /api/admin/schedules/doctor/10/calendar?start_date=2025-11-01&end_date=2025-11-30
Authorization: Bearer <token>
```

**响应示例**:
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "2025-11-10": [
      {
        "id": 1,
        "time_slot": "上午",
        "start_time": "08:00:00",
        "end_time": "12:00:00",
        "max_appointments": 20,
        "current_appointments": 5,
        "available_appointments": 15,
        "status": "AVAILABLE",
        "room_number": "101"
      },
      {
        "id": 2,
        "time_slot": "下午",
        "start_time": "14:00:00",
        "end_time": "18:00:00",
        "max_appointments": 20,
        "current_appointments": 18,
        "available_appointments": 2,
        "status": "AVAILABLE",
        "room_number": "101"
      }
    ],
    "2025-11-11": [
      {
        "id": 3,
        "time_slot": "上午",
        "start_time": "08:00:00",
        "end_time": "12:00:00",
        "max_appointments": 20,
        "current_appointments": 20,
        "available_appointments": 0,
        "status": "FULL",
        "room_number": "101"
      }
    ]
  }
}
```

---

### 10. 获取科室排班日历

**接口地址**: `GET /api/admin/schedules/department/{departmentId}/calendar`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| departmentId | number | 是 | 科室ID |

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| start_date | string | 是 | 开始日期，格式：YYYY-MM-DD |
| end_date | string | 是 | 结束日期，格式：YYYY-MM-DD |

**请求示例**:
```http
GET /api/admin/schedules/department/2/calendar?start_date=2025-11-01&end_date=2025-11-30
Authorization: Bearer <token>
```

**响应示例**: (同医生排班日历，但包含多个医生的排班)

---

### 11. 获取可预约排班列表

**接口地址**: `GET /api/admin/schedules/available`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| doctor_id | number | 否 | 医生ID |
| department_id | number | 否 | 科室ID |
| date | string | 否 | 日期，格式：YYYY-MM-DD |

**请求示例**:
```http
GET /api/admin/schedules/available?department_id=2&date=2025-11-10
Authorization: Bearer <token>
```

**响应示例**:
```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "doctor_id": 10,
      "doctor_name": "张医生",
      "department_id": 2,
      "department_name": "内科",
      "schedule_date": "2025-11-10",
      "time_slot": "上午",
      "start_time": "08:00:00",
      "end_time": "12:00:00",
      "max_appointments": 20,
      "current_appointments": 5,
      "available_appointments": 15,
      "status": "AVAILABLE",
      "room_number": "101"
    }
  ]
}
```

---

### 12. 复制排班

**接口地址**: `POST /api/admin/schedules/copy`

**请求体**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| source_schedule_id | number | 是 | 源排班ID |
| target_dates | array | 是 | 目标日期数组，格式：["2025-11-15", "2025-11-16"] |

> 说明：如需在复制时覆盖时间段与起止时间，可扩展可选字段 `time_slot`、`start_time`、`end_time`，后端支持时传入即可；当前默认沿用源排班配置。

**请求示例**:
```json
{
  "source_schedule_id": 1,
  "target_dates": ["2025-11-15", "2025-11-16", "2025-11-17"]
}
```

**响应示例**:
```json
{
  "code": 0,
  "msg": "复制成功",
  "data": {
    "created_count": 3,
    "ids": [10, 11, 12]
  }
}
```

---

### 13. 获取排班统计信息

**接口地址**: `GET /api/admin/schedules/statistics`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| doctor_id | number | 否 | 医生ID |
| department_id | number | 否 | 科室ID |
| start_date | string | 否 | 开始日期，格式：YYYY-MM-DD |
| end_date | string | 否 | 结束日期，格式：YYYY-MM-DD |

**请求示例**:
```http
GET /api/admin/schedules/statistics?doctor_id=10&start_date=2025-11-01&end_date=2025-11-30
Authorization: Bearer <token>
```

**响应示例**:
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "total_schedules": 60,
    "available_schedules": 45,
    "full_schedules": 10,
    "cancelled_schedules": 5,
    "total_appointments": 1200,
    "current_appointments": 800,
    "available_appointments": 400,
    "appointment_rate": 66.67
  }
}
```

---

## 数据字典

### 时间段 (time_slot)

| 值 | 说明 |
|----|------|
| 上午 / MORNING | 上午时段 |
| 下午 / AFTERNOON | 下午时段 |
| 晚上 / EVENING | 晚上时段 |

### 排班状态 (status)

| 值 | 说明 |
|----|------|
| AVAILABLE | 可预约 |
| FULL | 已满 |
| CANCELLED | 已取消 |

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 0 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证或token过期 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 409 | 数据冲突（如排班时间冲突） |
| 500 | 服务器内部错误 |

---

## 业务规则

1. **排班唯一性**: 同一医生在同一日期的同一时间段只能有一个排班
2. **预约数限制**: `current_appointments` 不能超过 `max_appointments`
3. **状态自动更新**: 当 `current_appointments` 达到 `max_appointments` 时，状态自动变为 `FULL`
4. **可用预约数**: `available_appointments = max_appointments - current_appointments`
5. **时间验证**: `end_time` 必须大于 `start_time`
6. **日期限制**: 不能创建过去日期的排班
7. **删除限制**: 已有预约的排班不能删除，只能取消

---

## 注意事项

1. 所有日期格式统一为 `YYYY-MM-DD`
2. 所有时间格式统一为 `HH:mm:ss`
3. 批量操作建议单次不超过100条记录
4. 日历查询建议时间范围不超过3个月
5. 列表查询建议每页不超过100条
6. 所有接口都需要携带有效的认证token
7. 时间段参数与返回支持中文（上午/下午/晚上）与英文枚举（MORNING/AFTERNOON/EVENING），请求时也可使用 `period/slot` 作为别名

---

## 附：按科室查询医生

- **接口地址**: `GET /api/admin/doctors/department/{departmentId}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| departmentId | number | 是 | 科室ID |

**响应示例**:
```json
{
  "code": 0,
  "msg": "success",
  "data": [
    { "id": 1, "doctorName": "张三" },
    { "id": 2, "doctorName": "李四" }
  ]
}
```
