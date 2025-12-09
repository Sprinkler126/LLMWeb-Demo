package com.qna.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户个人信息DTO
 *
 * @author QnA Platform
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 角色名称
     */
    private String roleName;
    
    /**
     * 角色代码
     */
    private String roleCode;
    
    /**
     * 账号状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * API配额
     */
    private Integer apiQuota;
    
    /**
     * API已使用次数
     */
    private Integer apiUsed;
    
    /**
     * 合规检测权限：0-无权限，1-有权限
     */
    private Integer hasCompliancePermission;
    
    /**
     * 注册时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
}
