-- 系统配置表
CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT NOT NULL COMMENT '配置值',
    config_desc VARCHAR(500) COMMENT '配置描述',
    config_type VARCHAR(50) DEFAULT 'STRING' COMMENT '配置类型：STRING, NUMBER, BOOLEAN, JSON',
    is_encrypted INT DEFAULT 0 COMMENT '是否加密：0-否，1-是',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    updated_by BIGINT COMMENT '更新人ID',
    INDEX idx_config_key (config_key)
) COMMENT='系统配置表';

-- 插入默认的Python检测接口配置
INSERT INTO sys_config (config_key, config_value, config_desc, config_type) VALUES
('python.compliance.endpoint', 'http://localhost:5000/check_compliance', 'Python合规检测接口地址', 'STRING'),
('python.compliance.timeout', '30000', 'Python合规检测接口超时时间（毫秒）', 'NUMBER'),
('python.compliance.enabled', 'true', '是否启用Python合规检测', 'BOOLEAN');
