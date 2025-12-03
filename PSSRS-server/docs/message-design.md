# 消息存储数据库设计

## 表：message（患者消息）
- message_id BIGINT PK AUTO_INCREMENT
- patient_id BIGINT
- message_type VARCHAR(50)
- title VARCHAR(100)
- content TEXT
- status VARCHAR(10) 默认 '未读'
- is_deleted TINYINT 默认 0
- create_time DATETIME 默认 CURRENT_TIMESTAMP
- read_time DATETIME NULL
- 索引：(`patient_id`,`is_deleted`,`status`,`create_time`)

## 表：staff_message（医生消息）
- message_id BIGINT PK AUTO_INCREMENT
- receiver_id BIGINT（医生ID）
- message_type VARCHAR(50)（PATIENT_ARRIVAL / SYSTEM_ANNOUNCEMENT / APPOINTMENT_REMINDER 等）
- title VARCHAR(100)
- content TEXT（可存富文本或JSON）
- status VARCHAR(10) 默认 '未读'
- priority INT 默认 1（1-普通，2-重要）
- pinned TINYINT 默认 0（置顶）
- is_deleted TINYINT 默认 0
- create_time DATETIME 默认 CURRENT_TIMESTAMP
- read_time DATETIME NULL
- 索引：(`receiver_id`,`is_deleted`,`status`,`message_type`,`pinned`,`create_time`)

## 推送记录
- 建议增加表：push_log（记录 WS/HTTP 推送结果、重试次数、耗时）
- 字段示例：id, target_role, target_id, payload_type, succeed, latency_ms, attempt, create_time

## 读未读同步
- 状态字段统一为 '未读' / '已读'
- 标记已读写入 `read_time`

## 富文本公告
- `content` 存储 HTML 或 JSON（带 images/links），前端按类型渲染
