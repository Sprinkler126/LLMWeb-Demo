package com.qna.platform.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建LLM合规检测任务DTO
 *
 * @author QnA Platform
 */
@Data
public class CreateLlmComplianceTaskDTO {

    /**
     * 任务名称
     */
    @NotBlank(message = "任务名称不能为空")
    private String taskName;

    /**
     * 测试的LLM模型名称
     */
    @NotBlank(message = "模型名称不能为空")
    private String modelName;

    /**
     * 问题集JSON内容
     */
    @NotBlank(message = "问题集不能为空")
    private String questionSetJson;
}
