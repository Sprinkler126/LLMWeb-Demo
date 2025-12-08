-- ====================================================
-- 权限管理系统升级脚本
-- 版本: v2.0
-- 日期: 2025-12-08
-- 说明: 添加角色权限管理功能
-- ====================================================

USE qna_platform;

-- ====================================================
-- 1. 修改 sys_user 表
-- ====================================================
-- 添加新的角色字段支持
ALTER TABLE `sys_user` 
    MODIFY COLUMN `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色：SUPER_ADMIN-超级管理员, ADMIN-管理员, USER-普通用户';

-- 添加角色 ID 外键（后续关联角色表）
ALTER TABLE `sys_user` 
    ADD COLUMN `role_id` BIGINT COMMENT '关联角色ID' AFTER `role`;

-- ====================================================
-- 2. 创建角色表
-- ====================================================
CREATE TABLE IF NOT EXISTS `sys_role` (
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
-- 3. 创建权限表
-- ====================================================
CREATE TABLE IF NOT EXISTS `sys_permission` (
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
-- 4. 创建角色权限关联表
-- ====================================================
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_role_permission (`role_id`, `permission_id`),
    INDEX idx_role_id (`role_id`),
    INDEX idx_permission_id (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ====================================================
-- 5. 创建用户操作日志表（审计功能）
-- ====================================================
CREATE TABLE IF NOT EXISTS `sys_operation_log` (
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
-- 6. 初始化角色数据
-- ====================================================
INSERT INTO `sys_role` (`role_code`, `role_name`, `role_level`, `description`, `is_system`) VALUES
('SUPER_ADMIN', '超级管理员', 0, '系统最高权限，可以管理所有功能和用户', 1),
('ADMIN', '管理员', 1, '可以管理用户、配置API、查看日志', 1),
('USER', '普通用户', 2, '可以使用API进行对话，查看自己的历史记录', 1);

-- ====================================================
-- 7. 初始化权限数据
-- ====================================================
-- 一级权限（模块）
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `permission_type`, `parent_id`, `path`, `description`, `sort_order`) VALUES
('USER_MODULE', '用户管理', 'MENU', 0, '/admin/users', '用户管理模块', 1),
('API_MODULE', 'API管理', 'MENU', 0, '/admin/api-configs', 'API配置管理模块', 2),
('CHAT_MODULE', '在线对话', 'MENU', 0, '/chat', '在线对话模块', 3),
('COMPLIANCE_MODULE', '合规检测', 'MENU', 0, '/compliance', '合规检测模块', 4),
('SYSTEM_MODULE', '系统设置', 'MENU', 0, '/admin/system', '系统设置模块', 5);

-- 用户管理权限
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `permission_type`, `parent_id`, `description`, `sort_order`) VALUES
('USER_MANAGE', '用户管理', 'API', (SELECT id FROM sys_permission WHERE permission_code = 'USER_MODULE'), '查看、创建、编辑、删除用户', 1),
('USER_VIEW', '查看用户', 'API', (SELECT id FROM sys_permission WHERE permission_code = 'USER_MODULE'), '查看用户列表和详情', 2),
('USER_CREATE', '创建用户', 'API', (SELECT id FROM sys_permission WHERE permission_code = 'USER_MODULE'), '创建新用户', 3),
('USER_EDIT', '编辑用户', 'API', (SELECT id FROM sys_permission WHERE permission_code = 'USER_MODULE'), '编辑用户信息', 4),
('USER_DELETE', '删除用户', 'API', (SELECT id FROM sys_permission WHERE permission_code = 'USER_MODULE'), '删除用户', 5),
('ROLE_ASSIGN', '分配角色', 'API', (SELECT id FROM sys_permission WHERE permission_code = 'USER_MODULE'), '为用户分配角色', 6);

-- API管理权限
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `permission_type`, `parent_id`, `description`, `sort_order`) VALUES
('API_MANAGE', 'API管理', 'API', (SELECT id FROM sys_permission WHERE permission_code = 'API_MODULE'), '管理API配置', 1),
('API_VIEW', '查看API', 'API', (SELECT id FROM sys_permission WHERE permission_code = 'API_MODULE'), '查看API配置', 2),
('API_CREATE', '创建API', 'API', (SELECT id FROM sys_permission WHERE permission_code = 'API_MODULE'), '创建API配置', 3),
('API_EDIT', '编辑API', 'API', (SELECT id FROM sys_permission WHERE permission_code = 'API_MODULE'), '编辑API配置', 4),
('API_DELETE', '删除API', 'API', (SELECT id FROM sys_permission WHERE permission_code = 'API_MODULE'), '删除API配置', 5);

-- API使用权限
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `permission_type`, `parent_id`, `description`, `sort_order`) VALUES
('API_USE', 'API使用', 'API', (SELECT id FROM sys_permission WHERE permission_code = 'CHAT_MODULE'), '使用API进行对话', 1),
('CHAT_HISTORY', '查看历史', 'API', (SELECT id FROM sys_permission WHERE permission_code = 'CHAT_MODULE'), '查看对话历史', 2),
('CHAT_EXPORT', '导出记录', 'API', (SELECT id FROM sys_permission WHERE permission_code = 'CHAT_MODULE'), '导出对话记录', 3);

-- 合规检测权限
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `permission_type`, `parent_id`, `description`, `sort_order`) VALUES
('COMPLIANCE_CHECK', '合规检测', 'API', (SELECT id FROM sys_permission WHERE permission_code = 'COMPLIANCE_MODULE'), '执行合规检测', 1),
('COMPLIANCE_VIEW', '查看结果', 'API', (SELECT id FROM sys_permission WHERE permission_code = 'COMPLIANCE_MODULE'), '查看检测结果', 2),
('COMPLIANCE_MANAGE', '检测管理', 'API', (SELECT id FROM sys_permission WHERE permission_code = 'COMPLIANCE_MODULE'), '管理检测任务', 3);

-- 系统设置权限
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `permission_type`, `parent_id`, `description`, `sort_order`) VALUES
('SYSTEM_CONFIG', '系统配置', 'API', (SELECT id FROM sys_permission WHERE permission_code = 'SYSTEM_MODULE'), '修改系统配置', 1),
('PERMISSION_CONFIG', '权限配置', 'API', (SELECT id FROM sys_permission WHERE permission_code = 'SYSTEM_MODULE'), '配置角色权限', 2),
('LOG_VIEW', '日志查看', 'API', (SELECT id FROM sys_permission WHERE permission_code = 'SYSTEM_MODULE'), '查看系统日志', 3);

-- ====================================================
-- 8. 初始化角色权限关联
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
-- 9. 更新现有用户的角色关联
-- ====================================================
-- 为 admin 用户设置为超级管理员
UPDATE `sys_user` 
SET `role` = 'SUPER_ADMIN',
    `role_id` = (SELECT id FROM sys_role WHERE role_code = 'SUPER_ADMIN')
WHERE `username` = 'admin';

-- 为 testuser 设置为普通用户
UPDATE `sys_user` 
SET `role` = 'USER',
    `role_id` = (SELECT id FROM sys_role WHERE role_code = 'USER')
WHERE `username` = 'testuser';

-- ====================================================
-- 10. 添加外键约束（可选，根据需要启用）
-- ====================================================
-- ALTER TABLE `sys_user` 
--     ADD CONSTRAINT `fk_user_role` 
--     FOREIGN KEY (`role_id`) REFERENCES `sys_role`(`id`) ON DELETE SET NULL;

-- ALTER TABLE `sys_role_permission` 
--     ADD CONSTRAINT `fk_rp_role` 
--     FOREIGN KEY (`role_id`) REFERENCES `sys_role`(`id`) ON DELETE CASCADE,
--     ADD CONSTRAINT `fk_rp_permission` 
--     FOREIGN KEY (`permission_id`) REFERENCES `sys_permission`(`id`) ON DELETE CASCADE;

-- ====================================================
-- 升级完成
-- ====================================================

-- 查看角色列表
SELECT * FROM sys_role;

-- 查看权限列表
SELECT * FROM sys_permission ORDER BY parent_id, sort_order;

-- 查看角色权限关联
SELECT 
    r.role_name,
    p.permission_name,
    p.permission_type
FROM sys_role_permission rp
JOIN sys_role r ON rp.role_id = r.id
JOIN sys_permission p ON rp.permission_id = p.id
ORDER BY r.role_level, p.parent_id, p.sort_order;

-- 查看用户角色
SELECT 
    u.username,
    u.nickname,
    r.role_name,
    u.status
FROM sys_user u
LEFT JOIN sys_role r ON u.role_id = r.id;
