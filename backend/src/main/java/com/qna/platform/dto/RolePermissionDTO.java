package com.qna.platform.dto;

import lombok.Data;
import java.util.List;

/**
 * 角色权限DTO
 *
 * @author QnA Platform
 */
@Data
public class RolePermissionDTO {
    /**
     * 角色ID
     */
    private Long roleId;
    
    /**
     * 权限ID列表
     */
    private List<Long> permissionIds;
}
