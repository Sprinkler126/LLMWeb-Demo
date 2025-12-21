-- 模型训练任务表
CREATE TABLE IF NOT EXISTS model_training_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    task_name VARCHAR(200) NOT NULL COMMENT '任务名称',
    model_type VARCHAR(50) NOT NULL COMMENT '模型类型：TEXT_CLASSIFIER, SENTIMENT_ANALYZER, TOPIC_MODEL',
    dataset_path VARCHAR(500) COMMENT '数据集路径',
    model_config TEXT COMMENT '模型配置（JSON格式）',
    
    -- 训练状态
    task_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '任务状态：PENDING, RUNNING, COMPLETED, FAILED, STOPPED',
    progress INT DEFAULT 0 COMMENT '训练进度（0-100）',
    current_epoch INT DEFAULT 0 COMMENT '当前训练轮次',
    total_epochs INT DEFAULT 10 COMMENT '总训练轮次',
    
    -- 训练指标
    train_loss DECIMAL(10, 6) COMMENT '训练损失',
    train_accuracy DECIMAL(5, 4) COMMENT '训练准确率',
    val_loss DECIMAL(10, 6) COMMENT '验证损失',
    val_accuracy DECIMAL(5, 4) COMMENT '验证准确率',
    
    -- 输出路径
    model_save_path VARCHAR(500) COMMENT '模型保存路径',
    log_file_path VARCHAR(500) COMMENT '日志文件路径',
    training_log TEXT COMMENT '训练日志（最近的日志）',
    
    -- 时间信息
    start_time TIMESTAMP NULL COMMENT '开始时间',
    end_time TIMESTAMP NULL COMMENT '结束时间',
    duration_seconds INT DEFAULT 0 COMMENT '训练时长（秒）',
    
    -- 错误信息
    error_message TEXT COMMENT '错误信息',
    
    -- 元数据
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_created_by (created_by),
    INDEX idx_task_status (task_status),
    INDEX idx_created_time (created_time)
) COMMENT='模型训练任务表';
