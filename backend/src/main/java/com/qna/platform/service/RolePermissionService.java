package com.qna.platform.service;

import com.qna.platform.entity.SysPermission;
import com.qna.platform.entity.SysRole;

import java.util.List;

/**
 * 角色权限服务
 *
 * @author QnA Platform
 */
public interface RolePermissionService {
    
    /**
     * 获取所有角色
     */
    List<SysRole> getAllRoles();
    
    /**
     * 获取可用角色列表
     */
    List<SysRole> getEnabledRoles();
    
    /**
     * 获取所有权限（树形结构）
     */
    List<SysPermission> getAllPermissions();
    
    /**
     * 获取角色的权限列表
     */
    List<SysPermission> getRolePermissions(Long roleId);
    
    /**
     * 获取角色的权限ID列表
     */
    List<Long> getRolePermissionIds(Long roleId);
    
    /**
     * 为角色分配权限
     */
    void assignPermissionsToRole(Long roleId, List<Long> permissionIds);
    
    /**
     * 获取用户权限列表
     */
    List<SysPermission> getUserPermissions(Long userId);
    
    /**
     * 检查用户是否有某个权限
     */
    boolean hasPermission(Long userId, String permissionCode);
}
