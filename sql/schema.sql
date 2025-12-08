-- ====================================================
-- 在线问答平台数据库设计
-- ====================================================

CREATE DATABASE IF NOT EXISTS qna_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE qna_platform;

-- ====================================================
-- 1. 用户表
-- ====================================================
CREATE TABLE `sys_user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    `nickname` VARCHAR(100) COMMENT '昵称',
    `email` VARCHAR(100) COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `avatar` VARCHAR(500) COMMENT '头像URL',
    `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色：ADMIN, USER',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `api_quota` INT DEFAULT 100 COMMENT 'API调用次数配额（每日）',
    `api_used` INT DEFAULT 0 COMMENT 'API已使用次数',
    `quota_reset_time` DATETIME COMMENT '配额重置时间',
    `has_compliance_permission` TINYINT DEFAULT 0 COMMENT '是否有合规检测权限：0-无，1-有',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (`username`),
    INDEX idx_email (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ====================================================
-- 2. API配置表
-- ====================================================
CREATE TABLE `api_config` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'API配置ID',
    `config_name` VARCHAR(100) NOT NULL COMMENT '配置名称',
    `provider` VARCHAR(50) NOT NULL COMMENT 'API提供商：OpenAI, Anthropic, Google, Aliyun, Baidu, Local等',
    `model_name` VARCHAR(100) NOT NULL COMMENT '模型名称',
    `api_endpoint` VARCHAR(500) NOT NULL COMMENT 'API端点URL',
    `api_key` VARCHAR(500) COMMENT 'API密钥（加密存储）',
    `api_type` VARCHAR(20) DEFAULT 'ONLINE' COMMENT 'API类型：ONLINE-在线，LOCAL-本地',
    `max_tokens` INT DEFAULT 2000 COMMENT '最大token数',
    `temperature` DECIMAL(3,2) DEFAULT 0.7 COMMENT '温度参数',
    `timeout` INT DEFAULT 30 COMMENT '超时时间（秒）',
    `extra_params` TEXT COMMENT '额外参数（JSON格式）',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `created_by` BIGINT COMMENT '创建者ID',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_provider (`provider`),
    INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API配置表';

-- ====================================================
-- 3. 对话会话表
-- ====================================================
CREATE TABLE `chat_session` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '会话ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `api_config_id` BIGINT NOT NULL COMMENT 'API配置ID',
    `session_title` VARCHAR(200) COMMENT '会话标题',
    `session_status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '会话状态：ACTIVE-活跃，ARCHIVED-归档',
    `message_count` INT DEFAULT 0 COMMENT '消息数量',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (`user_id`),
    INDEX idx_api_config_id (`api_config_id`),
    INDEX idx_created_time (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话会话表';

-- ====================================================
-- 4. 对话消息表
-- ====================================================
CREATE TABLE `chat_message` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    `session_id` BIGINT NOT NULL COMMENT '会话ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `api_config_id` BIGINT NOT NULL COMMENT '使用的API配置ID',
    `role` VARCHAR(20) NOT NULL COMMENT '角色：user-用户，assistant-助手，system-系统',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `tokens_used` INT COMMENT '使用的token数',
    `response_time` INT COMMENT '响应时间（毫秒）',
    `error_message` TEXT COMMENT '错误信息',
    `compliance_status` VARCHAR(20) DEFAULT 'UNCHECKED' COMMENT '合规状态：UNCHECKED-未检测，PASS-通过，FAIL-不通过',
    `compliance_result` TEXT COMMENT '合规检测结果（JSON格式）',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_session_id (`session_id`),
    INDEX idx_user_id (`user_id`),
    INDEX idx_created_time (`created_time`),
    INDEX idx_compliance_status (`compliance_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话消息表';

-- ====================================================
-- 5. API调用日志表
-- ====================================================
CREATE TABLE `api_call_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `api_config_id` BIGINT NOT NULL COMMENT 'API配置ID',
    `session_id` BIGINT COMMENT '会话ID',
    `message_id` BIGINT COMMENT '消息ID',
    `request_content` TEXT COMMENT '请求内容',
    `response_content` TEXT COMMENT '响应内容',
    `status` VARCHAR(20) COMMENT '调用状态：SUCCESS-成功，FAILED-失败',
    `error_message` TEXT COMMENT '错误信息',
    `tokens_used` INT COMMENT '使用的token数',
    `response_time` INT COMMENT '响应时间（毫秒）',
    `ip_address` VARCHAR(50) COMMENT 'IP地址',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (`user_id`),
    INDEX idx_api_config_id (`api_config_id`),
    INDEX idx_created_time (`created_time`),
    INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API调用日志表';

-- ====================================================
-- 6. 合规检测任务表
-- ====================================================
CREATE TABLE `compliance_task` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '任务ID',
    `user_id` BIGINT NOT NULL COMMENT '创建用户ID',
    `task_name` VARCHAR(200) NOT NULL COMMENT '任务名称',
    `task_type` VARCHAR(20) NOT NULL COMMENT '任务类型：LOG-日志检测，FILE-文件上传检测',
    `file_path` VARCHAR(500) COMMENT '上传文件路径',
    `total_records` INT DEFAULT 0 COMMENT '总记录数',
    `checked_records` INT DEFAULT 0 COMMENT '已检测记录数',
    `pass_count` INT DEFAULT 0 COMMENT '通过数量',
    `fail_count` INT DEFAULT 0 COMMENT '不通过数量',
    `task_status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '任务状态：PENDING-待处理，PROCESSING-处理中，COMPLETED-已完成，FAILED-失败',
    `result_summary` TEXT COMMENT '检测结果摘要（JSON格式）',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `started_time` DATETIME COMMENT '开始时间',
    `completed_time` DATETIME COMMENT '完成时间',
    INDEX idx_user_id (`user_id`),
    INDEX idx_task_status (`task_status`),
    INDEX idx_created_time (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合规检测任务表';

-- ====================================================
-- 7. 合规检测结果表
-- ====================================================
CREATE TABLE `compliance_result` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '结果ID',
    `task_id` BIGINT NOT NULL COMMENT '任务ID',
    `message_id` BIGINT COMMENT '关联的消息ID',
    `content` TEXT NOT NULL COMMENT '被检测的内容',
    `check_result` VARCHAR(20) COMMENT '检测结果：PASS-通过，FAIL-不通过',
    `risk_level` VARCHAR(20) COMMENT '风险等级：LOW-低，MEDIUM-中，HIGH-高',
    `risk_categories` VARCHAR(500) COMMENT '风险类别（逗号分隔）',
    `detail_result` TEXT COMMENT '详细检测结果（JSON格式）',
    `confidence_score` DECIMAL(5,4) COMMENT '置信度分数',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_task_id (`task_id`),
    INDEX idx_message_id (`message_id`),
    INDEX idx_check_result (`check_result`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合规检测结果表';

-- ====================================================
-- 8. 系统配置表
-- ====================================================
CREATE TABLE `sys_config` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    `config_key` VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `config_type` VARCHAR(20) COMMENT '配置类型：STRING, NUMBER, JSON',
    `description` VARCHAR(500) COMMENT '配置描述',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_config_key (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- ====================================================
-- 初始化数据
-- ====================================================

-- 创建管理员账号（密码：admin123，使用BCrypt加密）
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `role`, `status`, `api_quota`, `has_compliance_permission`, `quota_reset_time`) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'ADMIN', 1, 10000, 1, DATE_ADD(NOW(), INTERVAL 1 DAY));

-- 创建测试用户（密码：user123）
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `role`, `status`, `api_quota`, `has_compliance_permission`, `quota_reset_time`) 
VALUES ('testuser', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '测试用户', 'USER', 1, 100, 0, DATE_ADD(NOW(), INTERVAL 1 DAY));

-- 插入系统配置
INSERT INTO `sys_config` (`config_key`, `config_value`, `config_type`, `description`) VALUES
('COMPLIANCE_CHECK_URL', 'http://localhost:5000/api/compliance/check', 'STRING', '合规检测服务URL'),
('COMPLIANCE_CHECK_TIMEOUT', '30', 'NUMBER', '合规检测超时时间（秒）'),
('EXPORT_FORMAT', 'JSON', 'STRING', '导出格式：JSON, CSV, XLSX'),
('MAX_SESSION_MESSAGES', '1000', 'NUMBER', '单个会话最大消息数'),
('DEFAULT_API_QUOTA', '100', 'NUMBER', '默认API调用配额');

-- 插入示例API配置
-- 标准模式：模型名称在请求体中
INSERT INTO `api_config` (`config_name`, `provider`, `model_name`, `api_endpoint`, `api_type`, `max_tokens`, `temperature`, `status`, `created_by`) VALUES
('OpenAI GPT-4', 'OpenAI', 'gpt-4', 'https://api.openai.com/v1/chat/completions', 'ONLINE', 2000, 0.7, 0, 1),
('OpenAI GPT-3.5', 'OpenAI', 'gpt-3.5-turbo', 'https://api.openai.com/v1/chat/completions', 'ONLINE', 2000, 0.7, 0, 1),
('Anthropic Claude', 'Anthropic', 'claude-3-opus', 'https://api.anthropic.com/v1/messages', 'ONLINE', 2000, 0.7, 0, 1),
('本地模型', 'Local', 'llama-2-7b', 'http://localhost:8080/v1/chat/completions', 'LOCAL', 2000, 0.7, 0, 1),

-- DeepSeek模型（兼容OpenAI格式）
('DeepSeek Chat', 'DeepSeek', 'deepseek-chat', 'https://api.deepseek.com/chat/completions', 'ONLINE', 4096, 0.7, 0, 1),
('DeepSeek Reasoner', 'DeepSeek', 'deepseek-reasoner', 'https://api.deepseek.com/chat/completions', 'ONLINE', 4096, 0.7, 0, 1),
('DeepSeek Coder', 'DeepSeek', 'deepseek-coder', 'https://api.deepseek.com/chat/completions', 'ONLINE', 4096, 0.7, 0, 1),

-- URL路径模式：模型名称在URL中（使用{model}占位符）
-- 示例：企业统一网关
('企业网关-GPT4', 'OpenAI', 'gpt-4', 'https://api.company.com/v1/chat/{model}', 'ONLINE', 2000, 0.7, 0, 1),
('企业网关-Claude', 'Anthropic', 'claude-3', 'https://api.company.com/v1/chat/{model}', 'ONLINE', 2000, 0.7, 0, 1);
