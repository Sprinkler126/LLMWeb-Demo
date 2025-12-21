package com.qna.platform.dto;

import lombok.Data;

/**
 * 创建训练任务DTO
 *
 * @author QnA Platform
 */
@Data
public class CreateTrainingTaskDTO {
    
    /**
     * 任务名称
     */
    private String taskName;
    
    /**
     * 模型类型：TEXT_CLASSIFIER, SENTIMENT_ANALYZER, TOPIC_MODEL
     */
    private String modelType;
    
    /**
     * 数据集路径（可选，使用默认数据集）
     */
    private String datasetPath;
    
    /**
     * 训练轮次
     */
    private Integer epochs;
    
    /**
     * 批次大小
     */
    private Integer batchSize;
    
    /**
     * 学习率
     */
    private Double learningRate;
}
