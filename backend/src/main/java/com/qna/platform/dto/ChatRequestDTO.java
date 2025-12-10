package com.qna.platform.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 对话请求DTO
 *
 * @author QnA Platform
 */
@Data
public class ChatRequestDTO {

    /**
     * 会话ID（新建会话时为空）
     */
    private Long sessionId;

    /**
     * API配置ID
     */
    @NotNull(message = "API配置ID不能为空")
    private Long apiConfigId;

    /**
     * 用户消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String message;

    /**
     * 会话标题（新建会话时使用）
     */
    private String sessionTitle;
    
    /**
     * 机器人模板ID
     */
    private Long botTemplateId;
    
    /**
     * 自定义系统消息
     */
    private String systemMessage;
}