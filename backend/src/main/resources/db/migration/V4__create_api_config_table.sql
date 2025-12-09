-- API配置表
CREATE TABLE IF NOT EXISTS `api_config` (
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

-- 插入默认的API配置示例
INSERT INTO `api_config` (`config_name`, `provider`, `model_name`, `api_endpoint`, `api_key`, `api_type`, `max_tokens`, `temperature`, `timeout`, `status`, `created_by`) VALUES
('GPT-3.5 Turbo', 'OPENAI', 'gpt-3.5-turbo', 'https://api.openai.com/v1/chat/completions', NULL, 'ONLINE', 4096, 0.70, 30, 0, 1),
('GPT-4', 'OPENAI', 'gpt-4', 'https://api.openai.com/v1/chat/completions', NULL, 'ONLINE', 8192, 0.70, 60, 0, 1),
('GPT-4 Turbo', 'OPENAI', 'gpt-4-turbo', 'https://api.openai.com/v1/chat/completions', NULL, 'ONLINE', 128000, 0.70, 60, 0, 1),
('GPT-4o', 'OPENAI', 'gpt-4o', 'https://api.openai.com/v1/chat/completions', NULL, 'ONLINE', 128000, 0.70, 60, 0, 1),
('Claude 3 Opus', 'ANTHROPIC', 'claude-3-opus-20240229', 'https://api.anthropic.com/v1/messages', NULL, 'ONLINE', 4096, 0.70, 60, 0, 1),
('Claude 3 Sonnet', 'ANTHROPIC', 'claude-3-sonnet-20240229', 'https://api.anthropic.com/v1/messages', NULL, 'ONLINE', 4096, 0.70, 60, 0, 1);
