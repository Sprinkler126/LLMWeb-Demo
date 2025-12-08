package com.qna.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 合规检测任务实体
 *
 * @author QnA Platform
 */
@Data
@TableName("compliance_task")
public class ComplianceTask implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 创建用户ID
     */
    private Long userId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务类型：LOG-日志检测，FILE-文件上传检测
     */
    private String taskType;

    /**
     * 上传文件路径
     */
    private String filePath;

    /**
     * 总记录数
     */
    private Integer totalRecords;

    /**
     * 已检测记录数
     */
    private Integer checkedRecords;

    /**
     * 通过数量
     */
    private Integer passCount;

    /**
     * 不通过数量
     */
    private Integer failCount;

    /**
     * 任务状态：PENDING-待处理，PROCESSING-处理中，COMPLETED-已完成，FAILED-失败
     */
    private String taskStatus;

    /**
     * 检测结果摘要（JSON格式）
     */
    private String resultSummary;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 开始时间
     */
    private LocalDateTime startedTime;

    /**
     * 完成时间
     */
    private LocalDateTime completedTime;
}
