package com.qna.platform.dto;

import lombok.Data;

/**
 * 用户管理DTO
 *
 * @author QnA Platform
 */
@Data
public class UserManagementDTO {
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 角色ID
     */
    private Long roleId;
    
    /**
     * 角色代码
     */
    private String roleCode;
    
    /**
     * 角色名称
     */
    private String roleName;
    
    /**
     * 状态
     */
    private Integer status;
    
    /**
     * API配额
     */
    private Integer apiQuota;
    
    /**
     * API已使用
     */
    private Integer apiUsed;
    
    /**
     * 是否有合规检测权限
     */
    private Integer hasCompliancePermission;
    
    /**
     * 创建时间
     */
    private String createdTime;
}
