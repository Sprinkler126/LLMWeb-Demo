package com.qna.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 模型训练任务实体
 *
 * @author QnA Platform
 */
@Data
@TableName("model_training_task")
public class ModelTrainingTask {
    
    /**
     * 任务ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 任务名称
     */
    private String taskName;
    
    /**
     * 模型类型
     */
    private String modelType;
    
    /**
     * 数据集路径
     */
    private String datasetPath;
    
    /**
     * 模型配置（JSON）
     */
    private String modelConfig;
    
    /**
     * 任务状态：PENDING, RUNNING, COMPLETED, FAILED, STOPPED
     */
    private String taskStatus;
    
    /**
     * 训练进度（0-100）
     */
    private Integer progress;
    
    /**
     * 当前训练轮次
     */
    private Integer currentEpoch;
    
    /**
     * 总训练轮次
     */
    private Integer totalEpochs;
    
    /**
     * 训练损失
     */
    private BigDecimal trainLoss;
    
    /**
     * 训练准确率
     */
    private BigDecimal trainAccuracy;
    
    /**
     * 验证损失
     */
    private BigDecimal valLoss;
    
    /**
     * 验证准确率
     */
    private BigDecimal valAccuracy;
    
    /**
     * 模型保存路径
     */
    private String modelSavePath;
    
    /**
     * 日志文件路径
     */
    private String logFilePath;
    
    /**
     * 训练日志
     */
    private String trainingLog;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 训练时长（秒）
     */
    private Integer durationSeconds;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 创建人ID
     */
    private Long createdBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
