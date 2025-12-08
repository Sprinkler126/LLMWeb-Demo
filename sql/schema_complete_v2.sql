-- ====================================================
-- 在线问答平台完整数据库设计
-- 版本: v2.0 (包含权限管理系统)
-- 日期: 2025-12-08
-- 说明: 完整建库脚本，重置所有数据但保留 api_config 表结构
-- ====================================================

-- 如果数据库已存在，先删除（注意：这将删除所有数据！）
DROP DATABASE IF EXISTS qna_platform;

CREATE DATABASE qna_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE qna_platform;

-- ====================================================
-- 1. 系统用户表
-- ====================================================
CREATE TABLE `sys_user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    `nickname` VARCHAR(100) COMMENT '昵称',
    `email` VARCHAR(100) COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `avatar` VARCHAR(500) COMMENT '头像URL',
    `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色代码：SUPER_ADMIN-超级管理员, ADMIN-管理员, USER-普通用户',
    `role_id` BIGINT COMMENT '关联角色ID',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `api_quota` INT DEFAULT 1000 COMMENT 'API调用次数配额（每日）',
    `api_used` INT DEFAULT 0 COMMENT 'API已使用次数',
    `quota_reset_time` DATETIME COMMENT '配额重置时间',
    `has_compliance_permission` TINYINT DEFAULT 0 COMMENT '是否有合规检测权限：0-无，1-有',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (`username`),
    INDEX idx_email (`email`),
    INDEX idx_role_id (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ====================================================
-- 2. 系统角色表
-- ====================================================
CREATE TABLE `sys_role` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    `role_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '角色代码：SUPER_ADMIN, ADMIN, USER',
    `role_name` VARCHAR(100) NOT NULL COMMENT '角色名称',
    `role_level` INT NOT NULL DEFAULT 0 COMMENT '角色级别：0-超级管理员，1-管理员，2-普通用户',
    `description` VARCHAR(500) COMMENT '角色描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `is_system` TINYINT DEFAULT 0 COMMENT '是否系统角色：0-否，1-是（不可删除）',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_role_code (`role_code`),
    INDEX idx_role_level (`role_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- ====================================================
-- 3. 系统权限表
-- ====================================================
CREATE TABLE `sys_permission` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '权限ID',
    `permission_code` VARCHAR(100) NOT NULL UNIQUE COMMENT '权限代码：USER_MANAGE, API_MANAGE, API_USE, COMPLIANCE_CHECK',
    `permission_name` VARCHAR(100) NOT NULL COMMENT '权限名称',
    `permission_type` VARCHAR(20) NOT NULL COMMENT '权限类型：MENU-菜单，BUTTON-按钮，API-接口',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父权限ID（0表示顶级权限）',
    `path` VARCHAR(200) COMMENT '权限路径（前端路由）',
    `description` VARCHAR(500) COMMENT '权限描述',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_permission_code (`permission_code`),
    INDEX idx_parent_id (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统权限表';

-- ====================================================
-- 4. 角色权限关联表
-- ====================================================
CREATE TABLE `sys_role_permission` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_role_permission (`role_id`, `permission_id`),
    INDEX idx_role_id (`role_id`),
    INDEX idx_permission_id (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ====================================================
-- 5. API配置表（保留表结构，不插入初始数据）
-- ====================================================
CREATE TABLE `api_config` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'API配置ID',
    `config_name` VARCHAR(100) NOT NULL COMMENT '配置名称',
    `provider` VARCHAR(50) NOT NULL COMMENT 'API提供商：OPENAI, ANTHROPIC, GOOGLE, ALIYUN, BAIDU, DEEPSEEK, LOCAL等',
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
-- 6. 对话会话表
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
-- 7. 对话消息表
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
-- 8. API调用日志表
-- ====================================================
CREATE TABLE `api_call_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `api_config_id` BIGINT NOT NULL COMMENT 'API配置ID',
    `session_id` BIGINT COMMENT '会话ID',
    `message_id` BIGINT COMMENT '消息ID',
    `request_content` TEXT COMMENT '请求内容',
    `response_content` TEXT COMMENT '响应内容',
    `tokens_used` INT COMMENT '使用token数',
    `response_time` INT COMMENT '响应时间（毫秒）',
    `status` VARCHAR(20) COMMENT '状态：SUCCESS-成功，FAILED-失败',
    `error_message` TEXT COMMENT '错误信息',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (`user_id`),
    INDEX idx_api_config_id (`api_config_id`),
    INDEX idx_created_time (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API调用日志表';

-- ====================================================
-- 9. 用户操作日志表
-- ====================================================
CREATE TABLE `sys_operation_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    `user_id` BIGINT NOT NULL COMMENT '操作用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '操作用户名',
    `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型：ROLE_ASSIGN, PERMISSION_UPDATE, USER_MANAGE等',
    `operation_desc` VARCHAR(500) COMMENT '操作描述',
    `target_type` VARCHAR(50) COMMENT '目标类型：USER, ROLE, PERMISSION',
    `target_id` BIGINT COMMENT '目标ID',
    `request_method` VARCHAR(10) COMMENT '请求方法：GET, POST, PUT, DELETE',
    `request_url` VARCHAR(500) COMMENT '请求URL',
    `request_params` TEXT COMMENT '请求参数（JSON格式）',
    `request_ip` VARCHAR(50) COMMENT '请求IP',
    `response_status` INT COMMENT '响应状态码',
    `error_message` TEXT COMMENT '错误信息',
    `execution_time` INT COMMENT '执行时间（毫秒）',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (`user_id`),
    INDEX idx_operation_type (`operation_type`),
    INDEX idx_created_time (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户操作日志表';

-- ====================================================
-- 初始化数据
-- ====================================================

-- ====================================================
-- 1. 初始化角色数据
-- ====================================================
INSERT INTO `sys_role` (`role_code`, `role_name`, `role_level`, `description`, `is_system`) VALUES
('SUPER_ADMIN', '超级管理员', 0, '系统最高权限，可以管理所有功能、用户和权限配置', 1),
('ADMIN', '管理员', 1, '可以管理用户、配置API、查看日志，但不能修改权限配置', 1),
('USER', '普通用户', 2, '可以使用API进行对话，查看自己的历史记录', 1);

-- ====================================================
-- 2. 初始化权限数据
-- ====================================================
-- 一级权限（模块）
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `permission_type`, `parent_id`, `path`, `description`, `sort_order`) VALUES
('USER_MODULE', '用户管理', 'MENU', 0, '/admin/users', '用户管理模块', 1),
('API_MODULE', 'API管理', 'MENU', 0, '/admin/api-configs', 'API配置管理模块', 2),
('CHAT_MODULE', '在线对话', 'MENU', 0, '/chat', '在线对话模块', 3),
('COMPLIANCE_MODULE', '合规检测', 'MENU', 0, '/compliance', '合规检测模块', 4),
('SYSTEM_MODULE', '系统设置', 'MENU', 0, '/admin/system', '系统设置模块', 5);

-- 用户管理权限
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `permission_type`, `parent_id`, `description`, `sort_order`) 
SELECT 'USER_MANAGE', '用户管理', 'API', id, '查看、创建、编辑、删除用户', 1 FROM sys_permission WHERE permission_code = 'USER_MODULE'
UNION ALL SELECT 'USER_VIEW', '查看用户', 'API', id, '查看用户列表和详情', 2 FROM sys_permission WHERE permission_code = 'USER_MODULE'
UNION ALL SELECT 'USER_CREATE', '创建用户', 'API', id, '创建新用户', 3 FROM sys_permission WHERE permission_code = 'USER_MODULE'
UNION ALL SELECT 'USER_EDIT', '编辑用户', 'API', id, '编辑用户信息', 4 FROM sys_permission WHERE permission_code = 'USER_MODULE'
UNION ALL SELECT 'USER_DELETE', '删除用户', 'API', id, '删除用户', 5 FROM sys_permission WHERE permission_code = 'USER_MODULE'
UNION ALL SELECT 'ROLE_ASSIGN', '分配角色', 'API', id, '为用户分配角色', 6 FROM sys_permission WHERE permission_code = 'USER_MODULE';

-- API管理权限
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `permission_type`, `parent_id`, `description`, `sort_order`) 
SELECT 'API_MANAGE', 'API管理', 'API', id, '管理API配置', 1 FROM sys_permission WHERE permission_code = 'API_MODULE'
UNION ALL SELECT 'API_VIEW', '查看API', 'API', id, '查看API配置', 2 FROM sys_permission WHERE permission_code = 'API_MODULE'
UNION ALL SELECT 'API_CREATE', '创建API', 'API', id, '创建API配置', 3 FROM sys_permission WHERE permission_code = 'API_MODULE'
UNION ALL SELECT 'API_EDIT', '编辑API', 'API', id, '编辑API配置', 4 FROM sys_permission WHERE permission_code = 'API_MODULE'
UNION ALL SELECT 'API_DELETE', '删除API', 'API', id, '删除API配置', 5 FROM sys_permission WHERE permission_code = 'API_MODULE';

-- API使用权限
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `permission_type`, `parent_id`, `description`, `sort_order`) 
SELECT 'API_USE', 'API使用', 'API', id, '使用API进行对话', 1 FROM sys_permission WHERE permission_code = 'CHAT_MODULE'
UNION ALL SELECT 'CHAT_HISTORY', '查看历史', 'API', id, '查看对话历史', 2 FROM sys_permission WHERE permission_code = 'CHAT_MODULE'
UNION ALL SELECT 'CHAT_EXPORT', '导出记录', 'API', id, '导出对话记录', 3 FROM sys_permission WHERE permission_code = 'CHAT_MODULE';

-- 合规检测权限
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `permission_type`, `parent_id`, `description`, `sort_order`) 
SELECT 'COMPLIANCE_CHECK', '合规检测', 'API', id, '执行合规检测', 1 FROM sys_permission WHERE permission_code = 'COMPLIANCE_MODULE'
UNION ALL SELECT 'COMPLIANCE_VIEW', '查看结果', 'API', id, '查看检测结果', 2 FROM sys_permission WHERE permission_code = 'COMPLIANCE_MODULE'
UNION ALL SELECT 'COMPLIANCE_MANAGE', '检测管理', 'API', id, '管理检测任务', 3 FROM sys_permission WHERE permission_code = 'COMPLIANCE_MODULE';

-- 系统设置权限
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `permission_type`, `parent_id`, `description`, `sort_order`) 
SELECT 'SYSTEM_CONFIG', '系统配置', 'API', id, '修改系统配置', 1 FROM sys_permission WHERE permission_code = 'SYSTEM_MODULE'
UNION ALL SELECT 'PERMISSION_CONFIG', '权限配置', 'API', id, '配置角色权限', 2 FROM sys_permission WHERE permission_code = 'SYSTEM_MODULE'
UNION ALL SELECT 'LOG_VIEW', '日志查看', 'API', id, '查看系统日志', 3 FROM sys_permission WHERE permission_code = 'SYSTEM_MODULE';

-- ====================================================
-- 3. 初始化角色权限关联
-- ====================================================
-- 超级管理员拥有所有权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 
    (SELECT id FROM sys_role WHERE role_code = 'SUPER_ADMIN'),
    id
FROM sys_permission;

-- 管理员权限（除了权限配置外的其他权限）
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 
    (SELECT id FROM sys_role WHERE role_code = 'ADMIN'),
    id
FROM sys_permission
WHERE permission_code IN (
    'USER_MODULE', 'USER_MANAGE', 'USER_VIEW', 'USER_CREATE', 'USER_EDIT', 'USER_DELETE', 'ROLE_ASSIGN',
    'API_MODULE', 'API_MANAGE', 'API_VIEW', 'API_CREATE', 'API_EDIT', 'API_DELETE',
    'CHAT_MODULE', 'API_USE', 'CHAT_HISTORY', 'CHAT_EXPORT',
    'COMPLIANCE_MODULE', 'COMPLIANCE_CHECK', 'COMPLIANCE_VIEW', 'COMPLIANCE_MANAGE',
    'SYSTEM_MODULE', 'SYSTEM_CONFIG', 'LOG_VIEW'
);

-- 普通用户权限（只能使用API和查看自己的记录）
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 
    (SELECT id FROM sys_role WHERE role_code = 'USER'),
    id
FROM sys_permission
WHERE permission_code IN (
    'CHAT_MODULE', 'API_USE', 'CHAT_HISTORY', 'CHAT_EXPORT'
);

-- ====================================================
-- 4. 初始化默认用户
-- ====================================================
-- 密码均为 BCrypt 加密后的字符串
-- admin123 -> $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH (示例，需要实际加密)
-- user123 -> $2a$10$CJm7vPPLe7Q7QC5TrY9e3OmBvD5KqJmNz1vZ8x3xS0xqZWQW0qW0q (示例，需要实际加密)

INSERT INTO `sys_user` 
    (`username`, `password`, `nickname`, `email`, `role`, `role_id`, `status`, `api_quota`, `has_compliance_permission`) 
VALUES 
    -- 超级管理员（密码：admin123）
    ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'admin@qnaplatform.com', 
     'SUPER_ADMIN', (SELECT id FROM sys_role WHERE role_code = 'SUPER_ADMIN'), 1, 10000, 1),
    
    -- 测试用户（密码：user123）
    ('testuser', '$2a$10$CJm7vPPLe7Q7QC5TrY9e3OmBvD5KqJmNz1vZ8x3xS0xqZWQW0qW0q', '测试用户', 'test@qnaplatform.com', 
     'USER', (SELECT id FROM sys_role WHERE role_code = 'USER'), 1, 1000, 0);

-- ====================================================
-- 建库完成
-- ====================================================

-- 验证查询
SELECT '=== 角色列表 ===' AS info;
SELECT * FROM sys_role;

SELECT '=== 权限列表（按模块分组） ===' AS info;
SELECT * FROM sys_permission ORDER BY parent_id, sort_order;

SELECT '=== 用户列表 ===' AS info;
SELECT 
    u.id,
    u.username,
    u.nickname,
    r.role_name,
    u.api_quota,
    u.status
FROM sys_user u
LEFT JOIN sys_role r ON u.role_id = r.id;

SELECT '=== 角色权限统计 ===' AS info;
SELECT 
    r.role_name,
    COUNT(rp.permission_id) as permission_count
FROM sys_role r
LEFT JOIN sys_role_permission rp ON r.id = rp.role_id
GROUP BY r.id, r.role_name
ORDER BY r.role_level;

SELECT '=== 数据库初始化完成 ===' AS info;
