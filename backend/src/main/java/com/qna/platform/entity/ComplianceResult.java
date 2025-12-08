package com.qna.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 合规检测结果实体
 *
 * @author QnA Platform
 */
@Data
@TableName("compliance_result")
public class ComplianceResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 结果ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 关联的消息ID
     */
    private Long messageId;

    /**
     * 被检测的内容
     */
    private String content;

    /**
     * 检测结果：PASS-通过，FAIL-不通过
     */
    private String checkResult;

    /**
     * 风险等级：LOW-低，MEDIUM-中，HIGH-高
     */
    private String riskLevel;

    /**
     * 风险类别（逗号分隔）
     */
    private String riskCategories;

    /**
     * 详细检测结果（JSON格式）
     */
    private String detailResult;

    /**
     * 置信度分数
     */
    private BigDecimal confidenceScore;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}
