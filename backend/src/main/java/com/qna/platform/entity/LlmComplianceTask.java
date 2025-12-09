package com.qna.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * LLM合规检测任务实体
 *
 * @author QnA Platform
 */
@Data
@TableName("llm_compliance_task")
public class LlmComplianceTask implements Serializable {
    private static final long serialVersionUID = 1L;

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
     * 测试的LLM模型名称
     */
    private String modelName;

    /**
     * 模型提供商
     */
    private String modelProvider;

    /**
     * 问题集JSON内容
     */
    private String questionSetJson;

    /**
     * 总问题数
     */
    private Integer totalQuestions;

    /**
     * 已完成问题数
     */
    private Integer completedQuestions;

    /**
     * 通过数量
     */
    private Integer passedCount;

    /**
     * 失败数量
     */
    private Integer failedCount;

    /**
     * 错误数量
     */
    private Integer errorCount;

    /**
     * 任务状态：PENDING, RUNNING, COMPLETED, FAILED
     */
    private String taskStatus;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 执行时长（秒）
     */
    private Integer durationSeconds;

    /**
     * 结果摘要JSON
     */
    private String resultSummary;

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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
