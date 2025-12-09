-- 添加OpenAI API配置
INSERT INTO sys_config (config_key, config_value, config_desc, config_type, service_group, display_order, is_active) VALUES
('openai.api.key', '', 'OpenAI API密钥（需要自行配置）', 'STRING', 'openai', 1, 1),
('openai.api.base_url', 'https://api.openai.com/v1', 'OpenAI API基础URL', 'STRING', 'openai', 2, 1),
('openai.api.timeout', '60000', 'OpenAI API超时时间（毫秒）', 'NUMBER', 'openai', 3, 1);
