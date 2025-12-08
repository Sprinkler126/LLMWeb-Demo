package com.qna.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话消息实体
 *
 * @author QnA Platform
 */
@Data
@TableName("chat_message")
public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 使用的API配置ID
     */
    private Long apiConfigId;

    /**
     * 角色：user-用户，assistant-助手，system-系统
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 使用的token数
     */
    private Integer tokensUsed;

    /**
     * 响应时间（毫秒）
     */
    private Integer responseTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 合规状态：UNCHECKED-未检测，PASS-通过，FAIL-不通过
     */
    private String complianceStatus;

    /**
     * 合规检测结果（JSON格式）
     */
    private String complianceResult;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}
