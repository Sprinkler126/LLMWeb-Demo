package com.qna.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * API配置实体
 *
 * @author QnA Platform
 */
@Data
@TableName("api_config")
public class ApiConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * API配置ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * API提供商
     */
    private String provider;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * API端点URL
     */
    private String apiEndpoint;

    /**
     * API密钥（加密存储）
     */
    private String apiKey;

    /**
     * API类型：ONLINE-在线，LOCAL-本地
     */
    private String apiType;

    /**
     * 最大token数
     */
    private Integer maxTokens;

    /**
     * 温度参数
     */
    private BigDecimal temperature;

    /**
     * 超时时间（秒）
     */
    private Integer timeout;

    /**
     * 额外参数（JSON格式）
     */
    private String extraParams;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 创建者ID
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
