package com.qna.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 训练进度DTO
 *
 * @author QnA Platform
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingProgressDTO {
    
    /**
     * 任务ID
     */
    private Long taskId;
    
    /**
     * 任务状态
     */
    private String taskStatus;
    
    /**
     * 进度百分比（0-100）
     */
    private Integer progress;
    
    /**
     * 当前轮次
     */
    private Integer currentEpoch;
    
    /**
     * 总轮次
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
     * 最新日志
     */
    private String latestLog;
    
    /**
     * 错误信息
     */
    private String errorMessage;
}
