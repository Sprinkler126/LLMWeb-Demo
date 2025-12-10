package com.qna.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话会话实体
 *
 * @author QnA Platform
 */
@Data
@TableName("chat_session")
public class ChatSession implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * API配置ID
     */
    private Long apiConfigId;

    /**
     * 会话标题
     */
    private String sessionTitle;

    /**
     * 会话状态：ACTIVE-活跃，ARCHIVED-归档
     */
    private String sessionStatus;

    /**
     * 消息数量
     */
    private Integer messageCount;

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
    /**
     * 会话级系统消息
     */
    private String systemMessage;
    /**
     *  使用的机器人模板
     */
    private Long botTemplateId;

}
