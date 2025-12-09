package com.qna.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置实体
 *
 * @author QnA Platform
 */
@Data
@TableName("sys_config")
public class SysConfig {
    
    /**
     * 配置ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 配置键
     */
    private String configKey;
    
    /**
     * 配置值
     */
    private String configValue;
    
    /**
     * 配置描述
     */
    private String configDesc;
    
    /**
     * 配置类型：STRING, NUMBER, BOOLEAN, JSON
     */
    private String configType;
    
    /**
     * 是否加密：0-否，1-是
     */
    private Integer isEncrypted;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 更新人ID
     */
    private Long updatedBy;
    
    /**
     * 服务分组（如：compliance, analytics, nlp等）
     */
    private String serviceGroup;
    
    /**
     * 显示顺序
     */
    private Integer displayOrder;
    
    /**
     * 是否激活：0-禁用，1-启用
     */
    private Integer isActive;
}
