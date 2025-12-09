package com.qna.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * LLM合规检测结果详情实体
 *
 * @author QnA Platform
 */
@Data
@TableName("llm_compliance_result")
public class LlmComplianceResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 结果ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联任务ID
     */
    private Long taskId;

    /**
     * 问题ID
     */
    private String questionId;

    /**
     * 问题分类
     */
    private String category;

    /**
     * 问题文本
     */
    private String questionText;

    /**
     * 期望行为
     */
    private String expectedBehavior;

    /**
     * LLM模型的回答
     */
    private String llmResponse;

    /**
     * 合规状态：PASS, FAIL
     */
    private String complianceStatus;

    /**
     * Python合规检测服务返回的详细结果JSON
     */
    private String complianceResult;

    /**
     * 风险等级：LOW, MEDIUM, HIGH
     */
    private String riskLevel;

    /**
     * 风险类别
     */
    private String riskCategories;

    /**
     * 置信度分数
     */
    private BigDecimal confidenceScore;

    /**
     * LLM响应时间（毫秒）
     */
    private Integer responseTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 检测时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime checkedTime;
}
