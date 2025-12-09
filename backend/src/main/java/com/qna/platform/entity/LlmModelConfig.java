package com.qna.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * LLM模型配置实体
 *
 * @author QnA Platform
 */
@Data
@TableName("llm_model_config")
public class LlmModelConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 配置ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 模型提供商
     */
    private String modelProvider;

    /**
     * API端点地址
     */
    private String apiEndpoint;

    /**
     * 模型描述
     */
    private String modelDescription;

    /**
     * 是否启用：0-禁用，1-启用
     */
    private Integer isEnabled;

    /**
     * 显示顺序
     */
    private Integer displayOrder;

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
