package com.qna.platform.controller;

import com.qna.platform.common.Result;
import com.qna.platform.dto.RolePermissionDTO;
import com.qna.platform.entity.SysPermission;
import com.qna.platform.entity.SysRole;
import com.qna.platform.service.RolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色权限管理控制器
 *
 * @author QnA Platform
 */
@RestController
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
public class RolePermissionController {
    
    private final RolePermissionService rolePermissionService;
    
    /**
     * 获取所有角色
     */
    @GetMapping
    public Result<List<SysRole>> getAllRoles() {
        List<SysRole> roles = rolePermissionService.getAllRoles();
        return Result.success(roles);
    }
    
    /**
     * 获取可用角色列表
     */
    @GetMapping("/enabled")
    public Result<List<SysRole>> getEnabledRoles() {
        List<SysRole> roles = rolePermissionService.getEnabledRoles();
        return Result.success(roles);
    }
    
    /**
     * 获取所有权限（树形结构）
     */
    @GetMapping("/permissions")
    public Result<List<SysPermission>> getAllPermissions() {
        List<SysPermission> permissions = rolePermissionService.getAllPermissions();
        return Result.success(permissions);
    }
    
    /**
     * 获取角色的权限列表
     */
    @GetMapping("/{roleId}/permissions")
    public Result<Map<String, Object>> getRolePermissions(@PathVariable Long roleId) {
        List<SysPermission> permissions = rolePermissionService.getRolePermissions(roleId);
        List<Long> permissionIds = rolePermissionService.getRolePermissionIds(roleId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("permissions", permissions);
        result.put("permissionIds", permissionIds);
        
        return Result.success(result);
    }
    
    /**
     * 为角色分配权限
     */
    @PutMapping("/{roleId}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long roleId, 
                                          @RequestBody RolePermissionDTO dto) {
        rolePermissionService.assignPermissionsToRole(roleId, dto.getPermissionIds());
        return Result.success();
    }
    
    /**
     * 获取用户权限列表
     */
    @GetMapping("/user/{userId}/permissions")
    public Result<List<SysPermission>> getUserPermissions(@PathVariable Long userId) {
        List<SysPermission> permissions = rolePermissionService.getUserPermissions(userId);
        return Result.success(permissions);
    }
    
    /**
     * 检查用户是否有某个权限
     */
    @GetMapping("/user/{userId}/has-permission")
    public Result<Boolean> hasPermission(@PathVariable Long userId, 
                                         @RequestParam String permissionCode) {
        boolean has = rolePermissionService.hasPermission(userId, permissionCode);
        return Result.success(has);
    }
}
