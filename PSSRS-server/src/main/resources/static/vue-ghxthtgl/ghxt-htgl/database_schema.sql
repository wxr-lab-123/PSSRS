-- 挂号系统数据库表结构
-- 数据库: pssrs

-- ============================================
-- 核心表（必须）
-- ============================================

-- 1. 医生排班表（必须新增）
CREATE TABLE IF NOT EXISTS doctor_schedule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '排班ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    department_id BIGINT NOT NULL COMMENT '科室ID',
    schedule_date DATE NOT NULL COMMENT '排班日期',
    time_slot VARCHAR(20) NOT NULL COMMENT '时间段(MORNING/AFTERNOON/EVENING)',
    start_time TIME COMMENT '开始时间',
    end_time TIME COMMENT '结束时间',
    max_appointments INT DEFAULT 20 COMMENT '最大挂号数',
    current_appointments INT DEFAULT 0 COMMENT '当前已挂号数',
    available_appointments INT GENERATED ALWAYS AS (max_appointments - current_appointments) STORED COMMENT '剩余号源',
    status VARCHAR(20) DEFAULT 'AVAILABLE' COMMENT '状态(AVAILABLE-可用/FULL-已满/CANCELLED-已取消)',
    room_number VARCHAR(50) COMMENT '诊室号',
    notes TEXT COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人',
    updated_by BIGINT COMMENT '更新人',
    UNIQUE KEY uk_doctor_date_slot (doctor_id, schedule_date, time_slot),
    INDEX idx_date (schedule_date),
    INDEX idx_doctor (doctor_id),
    INDEX idx_department (department_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生排班表';

-- ============================================
-- 扩展表（建议添加）
-- ============================================

-- 2. 系统设置表
CREATE TABLE IF NOT EXISTS system_settings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    setting_key VARCHAR(100) NOT NULL UNIQUE COMMENT '设置键',
    setting_value TEXT COMMENT '设置值',
    setting_type VARCHAR(20) DEFAULT 'STRING' COMMENT '值类型(STRING/NUMBER/BOOLEAN/JSON)',
    description VARCHAR(255) COMMENT '描述',
    category VARCHAR(50) COMMENT '分类',
    is_public BOOLEAN DEFAULT FALSE COMMENT '是否公开',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统设置表';

-- 3. 操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '用户名',
    operation VARCHAR(50) COMMENT '操作类型',
    method VARCHAR(200) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    result TEXT COMMENT '返回结果',
    ip VARCHAR(50) COMMENT 'IP地址',
    location VARCHAR(100) COMMENT '操作地点',
    status VARCHAR(20) DEFAULT 'SUCCESS' COMMENT '状态(SUCCESS/FAIL)',
    error_msg TEXT COMMENT '错误信息',
    execution_time INT COMMENT '执行时长(ms)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 4. 通知消息表
CREATE TABLE IF NOT EXISTS notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT COMMENT '内容',
    type VARCHAR(20) DEFAULT 'INFO' COMMENT '类型(INFO/WARNING/ERROR/SUCCESS)',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    related_type VARCHAR(50) COMMENT '关联类型(APPOINTMENT/SCHEDULE等)',
    related_id BIGINT COMMENT '关联ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL COMMENT '阅读时间',
    INDEX idx_user_read (user_id, is_read),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知消息表';

-- ============================================
-- 初始化数据示例
-- ============================================

-- 插入时间段配置
INSERT INTO system_settings (setting_key, setting_value, setting_type, description, category) VALUES
('schedule.morning.start', '08:00', 'STRING', '上午排班开始时间', 'schedule'),
('schedule.morning.end', '12:00', 'STRING', '上午排班结束时间', 'schedule'),
('schedule.afternoon.start', '14:00', 'STRING', '下午排班开始时间', 'schedule'),
('schedule.afternoon.end', '18:00', 'STRING', '下午排班结束时间', 'schedule'),
('schedule.evening.start', '18:00', 'STRING', '晚上排班开始时间', 'schedule'),
('schedule.evening.end', '21:00', 'STRING', '晚上排班结束时间', 'schedule'),
('schedule.default.max', '20', 'NUMBER', '默认最大挂号数', 'schedule')
ON DUPLICATE KEY UPDATE setting_value=VALUES(setting_value);
