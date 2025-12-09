-- 重构系统配置表，支持多个Python服务接口管理
-- 添加服务分组字段，便于管理多个不同的服务配置

-- 添加服务分组字段
ALTER TABLE sys_config ADD COLUMN IF NOT EXISTS service_group VARCHAR(100) COMMENT '服务分组（如：compliance, analytics, nlp等）';
ALTER TABLE sys_config ADD COLUMN IF NOT EXISTS display_order INT DEFAULT 0 COMMENT '显示顺序';
ALTER TABLE sys_config ADD COLUMN IF NOT EXISTS is_active INT DEFAULT 1 COMMENT '是否激活：0-禁用，1-启用';

-- 为现有配置添加服务分组
UPDATE sys_config SET service_group = 'compliance', display_order = 1 WHERE config_key = 'python.compliance.endpoint';
UPDATE sys_config SET service_group = 'compliance', display_order = 2 WHERE config_key = 'python.compliance.timeout';
UPDATE sys_config SET service_group = 'compliance', display_order = 3 WHERE config_key = 'python.compliance.enabled';

-- 添加索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_service_group ON sys_config(service_group);
CREATE INDEX IF NOT EXISTS idx_display_order ON sys_config(display_order);

-- 插入示例：未来可扩展的其他Python服务配置（当前注释掉，需要时可启用）
-- 例如：文本分析服务
-- INSERT INTO sys_config (config_key, config_value, config_desc, config_type, service_group, display_order) VALUES
-- ('python.nlp.endpoint', 'http://localhost:5001/api/nlp/analyze', 'Python文本分析接口地址', 'STRING', 'nlp', 1),
-- ('python.nlp.timeout', '15000', 'Python文本分析接口超时时间（毫秒）', 'NUMBER', 'nlp', 2),
-- ('python.nlp.enabled', 'false', '是否启用Python文本分析', 'BOOLEAN', 'nlp', 3);

-- 例如：数据分析服务
-- INSERT INTO sys_config (config_key, config_value, config_desc, config_type, service_group, display_order) VALUES
-- ('python.analytics.endpoint', 'http://localhost:5002/api/analytics/compute', 'Python数据分析接口地址', 'STRING', 'analytics', 1),
-- ('python.analytics.timeout', '60000', 'Python数据分析接口超时时间（毫秒）', 'NUMBER', 'analytics', 2),
-- ('python.analytics.enabled', 'false', '是否启用Python数据分析', 'BOOLEAN', 'analytics', 3);
