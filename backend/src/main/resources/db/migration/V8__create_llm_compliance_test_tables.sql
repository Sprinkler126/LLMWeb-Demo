-- LLM合规检测任务表
CREATE TABLE IF NOT EXISTS llm_compliance_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    task_name VARCHAR(200) NOT NULL COMMENT '任务名称',
    model_name VARCHAR(100) NOT NULL COMMENT '测试的LLM模型名称（如：gpt-3.5-turbo, gpt-4等）',
    model_provider VARCHAR(50) NOT NULL COMMENT '模型提供商（如：openai, anthropic等）',
    question_set_json TEXT NOT NULL COMMENT '问题集JSON内容',
    total_questions INT DEFAULT 0 COMMENT '总问题数',
    completed_questions INT DEFAULT 0 COMMENT '已完成问题数',
    passed_count INT DEFAULT 0 COMMENT '通过数量',
    failed_count INT DEFAULT 0 COMMENT '失败数量',
    error_count INT DEFAULT 0 COMMENT '错误数量（调用失败）',
    task_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '任务状态：PENDING, RUNNING, COMPLETED, FAILED',
    start_time TIMESTAMP NULL COMMENT '开始时间',
    end_time TIMESTAMP NULL COMMENT '结束时间',
    duration_seconds INT DEFAULT 0 COMMENT '执行时长（秒）',
    result_summary TEXT COMMENT '结果摘要JSON',
    error_message TEXT COMMENT '错误信息',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_created_by (created_by),
    INDEX idx_task_status (task_status),
    INDEX idx_created_time (created_time)
) COMMENT='LLM合规检测任务表';

-- LLM合规检测结果详情表
CREATE TABLE IF NOT EXISTS llm_compliance_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '结果ID',
    task_id BIGINT NOT NULL COMMENT '关联任务ID',
    question_id VARCHAR(50) NOT NULL COMMENT '问题ID',
    category VARCHAR(100) COMMENT '问题分类',
    question_text TEXT NOT NULL COMMENT '问题文本',
    expected_behavior VARCHAR(20) COMMENT '期望行为（ACCEPT, REJECT, CORRECT）',
    llm_response TEXT COMMENT 'LLM模型的回答',
    compliance_status VARCHAR(20) COMMENT '合规状态：PASS, FAIL',
    compliance_result TEXT COMMENT 'Python合规检测服务返回的详细结果JSON',
    risk_level VARCHAR(20) COMMENT '风险等级：LOW, MEDIUM, HIGH',
    risk_categories VARCHAR(200) COMMENT '风险类别',
    confidence_score DECIMAL(5,4) COMMENT '置信度分数',
    response_time INT COMMENT 'LLM响应时间（毫秒）',
    error_message TEXT COMMENT '错误信息',
    checked_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '检测时间',
    INDEX idx_task_id (task_id),
    INDEX idx_compliance_status (compliance_status),
    INDEX idx_category (category),
    FOREIGN KEY (task_id) REFERENCES llm_compliance_task(id) ON DELETE CASCADE
) COMMENT='LLM合规检测结果详情表';

-- 注意：使用现有的 api_config 表作为模型配置表
-- 不再创建单独的 llm_model_config 表
