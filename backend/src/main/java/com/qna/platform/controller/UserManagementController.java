package com.qna.platform.controller;

import com.qna.platform.common.Result;
import com.qna.platform.dto.UserManagementDTO;
import com.qna.platform.entity.SysUser;
import com.qna.platform.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 *
 * @author QnA Platform
 */
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserManagementController {
    
    private final UserManagementService userManagementService;
    
    /**
     * 获取所有用户列表
     */
    @GetMapping
    public Result<List<UserManagementDTO>> getAllUsers() {
        List<UserManagementDTO> users = userManagementService.getAllUsers();
        return Result.success(users);
    }
    
    /**
     * 获取用户详情
     */
    @GetMapping("/{userId}")
    public Result<UserManagementDTO> getUserById(@PathVariable Long userId) {
        UserManagementDTO user = userManagementService.getUserById(userId);
        return Result.success(user);
    }
    
    /**
     * 创建用户
     */
    @PostMapping
    public Result<SysUser> createUser(@RequestBody SysUser user) {
        SysUser created = userManagementService.createUser(user);
        return Result.success(created);
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/{userId}")
    public Result<Void> updateUser(@PathVariable Long userId, @RequestBody SysUser user) {
        user.setId(userId);
        userManagementService.updateUser(user);
        return Result.success();
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    public Result<Void> deleteUser(@PathVariable Long userId) {
        userManagementService.deleteUser(userId);
        return Result.success();
    }
    
    /**
     * 分配角色
     */
    @PutMapping("/{userId}/role")
    public Result<Void> assignRole(@PathVariable Long userId, @RequestParam Long roleId) {
        userManagementService.assignRole(userId, roleId);
        return Result.success();
    }
    
    /**
     * 更新用户状态
     */
    @PutMapping("/{userId}/status")
    public Result<Void> updateStatus(@PathVariable Long userId, @RequestParam Integer status) {
        userManagementService.updateUserStatus(userId, status);
        return Result.success();
    }
    
    /**
     * 更新API配额
     */
    @PutMapping("/{userId}/quota")
    public Result<Void> updateQuota(@PathVariable Long userId, @RequestParam Integer quota) {
        userManagementService.updateApiQuota(userId, quota);
        return Result.success();
    }
    
    /**
     * 更新合规检测权限
     */
    @PutMapping("/{userId}/compliance-permission")
    public Result<Void> updateCompliancePermission(@PathVariable Long userId, @RequestParam Integer hasPermission) {
        userManagementService.updateCompliancePermission(userId, hasPermission);
        return Result.success();
    }
}
