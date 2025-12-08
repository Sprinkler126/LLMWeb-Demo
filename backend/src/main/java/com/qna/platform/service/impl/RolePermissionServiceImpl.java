package com.qna.platform.service.impl;

import com.qna.platform.entity.SysPermission;
import com.qna.platform.entity.SysRole;
import com.qna.platform.entity.SysRolePermission;
import com.qna.platform.mapper.PermissionMapper;
import com.qna.platform.mapper.RoleMapper;
import com.qna.platform.mapper.RolePermissionMapper;
import com.qna.platform.service.RolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色权限服务实现
 *
 * @author QnA Platform
 */
@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl implements RolePermissionService {
    
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;
    
    @Override
    public List<SysRole> getAllRoles() {
        return roleMapper.selectList(null);
    }
    
    @Override
    public List<SysRole> getEnabledRoles() {
        return roleMapper.selectEnabledRoles();
    }
    
    @Override
    public List<SysPermission> getAllPermissions() {
        return permissionMapper.selectAllTree();
    }
    
    @Override
    public List<SysPermission> getRolePermissions(Long roleId) {
        return permissionMapper.selectByRoleId(roleId);
    }
    
    @Override
    public List<Long> getRolePermissionIds(Long roleId) {
        return rolePermissionMapper.selectPermissionIdsByRoleId(roleId);
    }
    
    @Override
    @Transactional
    public void assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        
        // 只有超级管理员角色不允许修改权限（超级管理员永远拥有所有权限）
        if ("SUPER_ADMIN".equals(role.getRoleCode())) {
            throw new RuntimeException("超级管理员角色不允许修改权限");
        }
        
        // 删除原有权限
        rolePermissionMapper.deleteByRoleId(roleId);
        
        // 添加新权限
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                SysRolePermission rp = new SysRolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permissionId);
                rolePermissionMapper.insert(rp);
            }
        }
    }
    
    @Override
    public List<SysPermission> getUserPermissions(Long userId) {
        return permissionMapper.selectByUserId(userId);
    }
    
    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        List<SysPermission> permissions = getUserPermissions(userId);
        return permissions.stream()
                .anyMatch(p -> p.getPermissionCode().equals(permissionCode));
    }
}
