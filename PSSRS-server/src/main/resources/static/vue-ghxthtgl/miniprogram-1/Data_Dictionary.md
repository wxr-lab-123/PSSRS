# PSSRS 医院挂号系统数据字典

## 1. 文档说明
本文档详细描述了 PSSRS 医院挂号系统的数据库表结构、字段定义及相关数据约束，旨在为系统开发、维护及数据分析提供准确的数据参考。

## 2. 核心业务表

### 2.1 医生排班表 (doctor_schedule)
存储医生排班信息及号源库存状态。

| 字段名 | 类型 | 长度 | 是否为空 | 默认值 | 描述 | 备注 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| id | BIGINT | 20 | NO | AUTO_INCREMENT | 排班ID | 主键 |
| doctor_id | BIGINT | 20 | NO | - | 医生ID | 外键关联 doctor 表 |
| department_id | BIGINT | 20 | NO | - | 科室ID | 外键关联 department 表 |
| schedule_date | DATE | - | NO | - | 排班日期 | 格式: YYYY-MM-DD |
| time_slot | VARCHAR | 20 | NO | - | 时间段 | 枚举: MORNING, AFTERNOON, EVENING |
| start_time | TIME | - | YES | - | 开始时间 | 格式: HH:MM:SS |
| end_time | TIME | - | YES | - | 结束时间 | 格式: HH:MM:SS |
| max_appointments | INT | 11 | YES | 20 | 最大挂号数 | 总号源 |
| current_appointments | INT | 11 | YES | 0 | 当前已挂号数 | 已占用号源 |
| available_appointments | INT | 11 | YES | (generated) | 剩余号源 | 计算列: max - current |
| status | VARCHAR | 20 | YES | 'AVAILABLE' | 状态 | AVAILABLE(可用), FULL(已满), CANCELLED(已取消) |
| room_number | VARCHAR | 50 | YES | - | 诊室号 | 医生出诊地点 |
| notes | TEXT | - | YES | - | 备注 | 排班特殊说明 |
| created_at | TIMESTAMP | - | YES | CURRENT_TIMESTAMP | 创建时间 | |
| updated_at | TIMESTAMP | - | YES | CURRENT_TIMESTAMP | 更新时间 | 自动更新 |

### 2.2 挂号记录表 (registrations)
存储患者的挂号预约记录。

| 字段名 | 类型 | 长度 | 是否为空 | 默认值 | 描述 | 备注 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| id | BIGINT | 20 | NO | AUTO_INCREMENT | 挂号ID | 主键 |
| user_id | BIGINT | 20 | NO | - | 用户ID | 挂号操作人 |
| patient_id | BIGINT | 20 | NO | - | 就诊人ID | 实际就诊人 |
| schedule_id | BIGINT | 20 | NO | - | 排班ID | 关联 doctor_schedule |
| status | VARCHAR | 20 | NO | 'PENDING' | 状态 | PENDING(待支付), PAID(待就诊), COMPLETED(已就诊), CANCELLED(已取消) |
| visit_number | VARCHAR | 20 | YES | - | 就诊序号 | 取号后生成，如 A001 |
| fee | DECIMAL | 10,2 | NO | - | 挂号费 | 单位: 元 |
| created_at | TIMESTAMP | - | YES | CURRENT_TIMESTAMP | 创建时间 | |

### 2.3 订单表 (orders)
存储支付订单信息。

| 字段名 | 类型 | 长度 | 是否为空 | 默认值 | 描述 | 备注 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| id | BIGINT | 20 | NO | AUTO_INCREMENT | 订单ID | 主键 |
| order_no | VARCHAR | 64 | NO | - | 订单号 | 全局唯一业务单号 |
| registration_id | BIGINT | 20 | NO | - | 挂号ID | 关联 registrations 表 |
| amount | DECIMAL | 10,2 | NO | - | 支付金额 | 单位: 元 |
| pay_status | VARCHAR | 20 | NO | 'UNPAID' | 支付状态 | UNPAID, PAID, REFUNDED |
| pay_time | DATETIME | - | YES | - | 支付时间 | |
| pay_way | VARCHAR | 20 | YES | - | 支付方式 | WECHAT, ALIPAY |
| created_at | TIMESTAMP | - | YES | CURRENT_TIMESTAMP | 创建时间 | |

### 2.4 医生请假申请表 (leave_request)
存储医生的排班请假申请。

| 字段名 | 类型 | 长度 | 是否为空 | 默认值 | 描述 | 备注 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| id | BIGINT | 20 | NO | AUTO_INCREMENT | 申请ID | 主键 |
| doctor_id | BIGINT | 20 | NO | - | 医生ID | 申请人 |
| schedule_id | BIGINT | 20 | NO | - | 排班ID | 关联 doctor_schedule |
| reason | TEXT | - | YES | - | 请假理由 | |
| leave_type | VARCHAR | 20 | YES | - | 请假类型 | 病假, 事假, 调休 |
| attachments | TEXT | - | YES | - | 附件 | 图片URL，JSON数组存储 |
| status | VARCHAR | 20 | YES | 'PENDING' | 审批状态 | PENDING(待审批), APPROVED(通过), REJECTED(拒绝) |
| approver_id | BIGINT | 20 | YES | - | 审批人ID | 管理员ID |
| approval_time | DATETIME | - | YES | - | 审批时间 | |
| create_time | DATETIME | - | YES | CURRENT_TIMESTAMP | 申请时间 | |

## 3. 基础信息表

### 3.1 用户表 (user)
存储所有系统用户（医生、管理员）。

| 字段名 | 类型 | 长度 | 是否为空 | 默认值 | 描述 | 备注            |
| :--- | :--- | :--- | :--- | :--- | :--- |:--------------|
| id | BIGINT | 20 | NO | AUTO_INCREMENT | 用户ID | 主键            |
| username | VARCHAR | 50 | NO | - | 用户名 | 唯一            |
| password | VARCHAR | 100 | NO | - | 密码 | BCrypt加密      |
| phone | VARCHAR | 20 | YES | - | 手机号 | 唯一            |
| real_name | VARCHAR | 50 | YES | - | 真实姓名 |               |
| role | VARCHAR | 20 | NO | 'PATIENT' | 角色 |  DOCTOR, ADMIN|
| status | INT | 1 | YES | 1 | 状态 | 1:正常, 0:禁用    |
| created_at | TIMESTAMP | - | YES | CURRENT_TIMESTAMP | 注册时间 |               |

### 3.2 就诊人表 (patient)
存储患者添加的就诊人档案。

| 字段名 | 类型 | 长度 | 是否为空 | 默认值 | 描述 | 备注 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| id | BIGINT | 20 | NO | AUTO_INCREMENT | ID | 主键 |
| user_id | BIGINT | 20 | NO | - | 所属用户ID | 关联 users 表 |
| name | VARCHAR | 50 | NO | - | 姓名 | |
| id_card | VARCHAR | 18 | NO | - | 身份证号 | 加密存储建议 |
| phone | VARCHAR | 20 | NO | - | 手机号 | |
| relation | VARCHAR | 20 | YES | - | 关系 | 本人, 家属, 朋友, 其他 |
| gender | INT | 1 | YES | - | 性别 | 0:男, 1:女, 2:未知 |
| birth_date | DATE | - | YES | - | 出生日期 | |

### 3.3 科室表 (department)
医院科室设置。

| 字段名 | 类型 | 长度 | 是否为空 | 默认值 | 描述 | 备注 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| id | BIGINT | 20 | NO | AUTO_INCREMENT | 科室ID | 主键 |
| name | VARCHAR | 50 | NO | - | 科室名称 | |
| description | TEXT | - | YES | - | 介绍 | |
| icon | VARCHAR | 255 | YES | - | 图标 | 图片URL |
| sort_order | INT | 11 | YES | 0 | 排序值 | 越小越靠前 |

## 4. 系统配置表

### 4.1 系统设置表 (system_settings)
存储系统全局配置参数。

| 字段名 | 类型 | 长度 | 是否为空 | 默认值 | 描述 | 备注 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| id | BIGINT | 20 | NO | AUTO_INCREMENT | ID | 主键 |
| setting_key | VARCHAR | 100 | NO | - | 配置键 | 唯一索引 |
| setting_value | TEXT | - | YES | - | 配置值 | |
| setting_type | VARCHAR | 20 | YES | 'STRING' | 值类型 | STRING, NUMBER, BOOLEAN, JSON |
| description | VARCHAR | 255 | YES | - | 描述 | |
| category | VARCHAR | 50 | YES | - | 分类 | schedule, payment, system |

### 4.2 操作日志表 (operation_log)
记录关键业务操作日志。

| 字段名 | 类型 | 长度 | 是否为空 | 默认值 | 描述 | 备注 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| id | BIGINT | 20 | NO | AUTO_INCREMENT | 日志ID | 主键 |
| user_id | BIGINT | 20 | YES | - | 操作人ID | |
| operation | VARCHAR | 50 | YES | - | 操作类型 | 如: 创建订单, 审批请假 |
| method | VARCHAR | 200 | YES | - | 请求方法 | Controller方法名 |
| params | TEXT | - | YES | - | 请求参数 | |
| result | TEXT | - | YES | - | 返回结果 | |
| execution_time | INT | 11 | YES | - | 耗时 | 单位: ms |
| created_at | TIMESTAMP | - | YES | CURRENT_TIMESTAMP | 操作时间 | |

### 4.3 通知消息表 (message)
站内信通知。

| 字段名 | 类型 | 长度 | 是否为空 | 默认值 | 描述 | 备注 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| id | BIGINT | 20 | NO | AUTO_INCREMENT | ID | 主键 |
| user_id | BIGINT | 20 | NO | - | 接收人ID | |
| title | VARCHAR | 200 | NO | - | 标题 | |
| content | TEXT | - | YES | - | 内容 | |
| is_read | BOOLEAN | - | YES | FALSE | 是否已读 | |
| created_at | TIMESTAMP | - | YES | CURRENT_TIMESTAMP | 发送时间 | |

## 5. 数据元素定义

本章节详细定义了系统中关键数据项的业务含义、格式和取值范围，作为数据校验和业务逻辑处理的依据。

| 数据元素 | 标识符 | 数据类型 | 长度 | 格式/示例 | 取值范围/枚举 | 说明 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **用户角色** | Role | String | 20 | <br>`DOCTOR`: 医生<br>`ADMIN`: 管理员 | 系统用户的权限标识，决定用户可访问的功能模块 |
| **排班状态** | ScheduleStatus | String | 20 | `AVAILABLE` | `AVAILABLE`: 可预约<br>`FULL`: 已满<br>`STOPPED`: 停诊<br>`EXPIRED`: 已过期 | 描述医生排班号源的当前可用性 |
| **挂号状态** | RegistrationStatus | String | 20 | `PAID` | `PENDING`: 待支付<br>`PAID`: 待就诊<br>`COMPLETED`: 已就诊<br>`CANCELLED`: 已取消<br>`REFUNDED`: 已退款 | 描述一次挂号记录的生命周期状态 |
| **就诊时段** | TimeSlot | String | 20 | `MORNING` | `MORNING`: 上午<br>`AFTERNOON`: 下午<br>`EVENING`: 晚上 | 粗粒度的排班时间段划分 |
| **支付方式** | PayWay | String | 20 | `WECHAT` | `WECHAT`: 微信支付<br>`ALIPAY`: 支付宝<br>`CASH`: 现金(线下) | 订单支付的渠道标识 |
| **性别** | Gender | Integer | 1 | `1` | `0`: 男<br>`1`: 女<br>`2`: 未知 | 符合 GB/T 2261.1 标准 |
| **身份证号** | IDCard | String | 18 | `11010119900307123X` | 符合中国居民身份证编码规则 (GB 11643-1999) | 18位数字，最后一位可能是X；存储时建议加密，展示时脱敏 |
| **手机号** | Phone | String | 11 | `13800138000` | 符合中国大陆手机号格式 | 11位数字，以1开头 |
| **请假类型** | LeaveType | String | 20 | `SICK` | `SICK`: 病假<br>`PERSONAL`: 事假<br>`ADJUST`: 调休 | 医生请假申请的类别 |
| **审批状态** | ApprovalStatus | String | 20 | `PENDING` | `PENDING`: 待审批<br>`APPROVED`: 已通过<br>`REJECTED`: 已拒绝 | 各类申请（请假、退款）的流转状态 |
