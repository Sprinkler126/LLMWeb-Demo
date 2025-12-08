package com.qna.platform.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 合规检测请求DTO
 *
 * @author QnA Platform
 */
@Data
public class ComplianceCheckDTO {

    /**
     * 任务名称
     */
    @NotBlank(message = "任务名称不能为空")
    private String taskName;

    /**
     * 任务类型：LOG-日志检测，FILE-文件上传检测
     */
    @NotNull(message = "任务类型不能为空")
    private String taskType;

    /**
     * 开始时间（日志检测使用）
     */
    private String startTime;

    /**
     * 结束时间（日志检测使用）
     */
    private String endTime;

    /**
     * 用户ID列表（日志检测使用，逗号分隔）
     */
    private String userIds;

    /**
     * 上传文件路径（文件检测使用）
     */
    private String filePath;
}
