-- 为llm_compliance_task表添加api_config_id字段，关联到api_config表
ALTER TABLE llm_compliance_task 
ADD COLUMN api_config_id BIGINT COMMENT 'API配置ID（关联api_config表）' AFTER task_name;

-- 添加索引
ALTER TABLE llm_compliance_task 
ADD INDEX idx_api_config_id (api_config_id);

-- 注意：model_name和model_provider字段保留，用于记录任务创建时的快照数据
-- 这样即使API配置被删除或修改，历史任务数据仍然完整
