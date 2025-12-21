-- 添加服务URL配置，支持分离部署
INSERT INTO sys_config (config_key, config_value, config_desc, config_type, service_group, display_order, is_active) VALUES
-- Python服务配置
('python.service.url', 'http://localhost:5000', 'Python服务基础URL（合规检测、模型训练等）', 'STRING', 'service', 1, 1),
('python.service.timeout', '30000', 'Python服务超时时间（毫秒）', 'NUMBER', 'service', 2, 1),

-- 后端服务配置（用于Python服务回调）
('backend.service.url', 'http://localhost:8080/api', '后端服务基础URL（用于Python服务回调）', 'STRING', 'service', 3, 1),

-- 前端服务配置
('frontend.service.url', 'http://localhost:5173', '前端服务URL', 'STRING', 'service', 4, 1);
