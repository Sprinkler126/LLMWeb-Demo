package com.qna.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 聊天机器人模板实体
 *
 * @author QnA Platform
 */
@Data
@TableName("chat_bot_template")
public class ChatBotTemplate {
    /**
     * 模板ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 模板名称
     */
    private String name;
    
    /**
     * 模板描述
     */
    private String description;
    
    /**
     * 系统消息内容
     */
    private String systemMessage;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;
}