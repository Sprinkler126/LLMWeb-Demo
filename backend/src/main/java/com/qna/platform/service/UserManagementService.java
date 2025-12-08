package com.qna.platform.service;

import com.qna.platform.dto.UserManagementDTO;
import com.qna.platform.entity.SysUser;

import java.util.List;

/**
 * 用户管理服务
 *
 * @author QnA Platform
 */
public interface UserManagementService {
    
    /**
     * 获取所有用户列表
     */
    List<UserManagementDTO> getAllUsers();
    
    /**
     * 根据ID获取用户详情
     */
    UserManagementDTO getUserById(Long userId);
    
    /**
     * 为用户分配角色
     */
    void assignRole(Long userId, Long roleId);
    
    /**
     * 启用/禁用用户
     */
    void updateUserStatus(Long userId, Integer status);
    
    /**
     * 更新用户API配额
     */
    void updateApiQuota(Long userId, Integer quota);
    
    /**
     * 更新用户合规检测权限
     */
    void updateCompliancePermission(Long userId, Integer hasPermission);
    
    /**
     * 删除用户
     */
    void deleteUser(Long userId);
    
    /**
     * 创建用户
     */
    SysUser createUser(SysUser user);
    
    /**
     * 更新用户信息
     */
    void updateUser(SysUser user);
}
