package com.qna.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qna.platform.dto.UserManagementDTO;
import com.qna.platform.entity.SysRole;
import com.qna.platform.entity.SysUser;
import com.qna.platform.mapper.RoleMapper;
import com.qna.platform.mapper.SysUserMapper;
import com.qna.platform.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户管理服务实现
 *
 * @author QnA Platform
 */
@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {
    
    private final SysUserMapper userMapper;
    private final RoleMapper roleMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Override
    public List<UserManagementDTO> getAllUsers() {
        List<SysUser> users = userMapper.selectList(null);
        List<UserManagementDTO> dtoList = new ArrayList<>();
        
        for (SysUser user : users) {
            UserManagementDTO dto = convertToDTO(user);
            dtoList.add(dto);
        }
        
        return dtoList;
    }
    
    @Override
    public UserManagementDTO getUserById(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return convertToDTO(user);
    }
    
    @Override
    @Transactional
    public void assignRole(Long userId, Long roleId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        SysRole role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        
        user.setRoleId(roleId);
        user.setRole(role.getRoleCode());
        userMapper.updateById(user);
    }
    
    @Override
    public void updateUserStatus(Long userId, Integer status) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        user.setStatus(status);
        userMapper.updateById(user);
    }
    
    @Override
    public void updateApiQuota(Long userId, Integer quota) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        user.setApiQuota(quota);
        userMapper.updateById(user);
    }
    
    @Override
    public void updateCompliancePermission(Long userId, Integer hasPermission) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        user.setHasCompliancePermission(hasPermission);
        userMapper.updateById(user);
    }
    
    @Override
    @Transactional
    public void deleteUser(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 不允许删除超级管理员
        if ("SUPER_ADMIN".equals(user.getRole())) {
            throw new RuntimeException("不允许删除超级管理员");
        }
        
        userMapper.deleteById(userId);
    }
    
    @Override
    @Transactional
    public SysUser createUser(SysUser user) {
        // 检查用户名是否已存在
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.eq("username", user.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        //默认角色为USER
        user.setRole("USER");
        user.setRoleId(3L);
        // 设置默认值
        if (user.getApiQuota() == null) {
            user.setApiQuota(100);
        }
        if (user.getApiUsed() == null) {
            user.setApiUsed(0);
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        if (user.getHasCompliancePermission() == null) {
            user.setHasCompliancePermission(0);
        }
        
        userMapper.insert(user);
        return user;
    }
    
    @Override
    public void updateUser(SysUser user) {
        SysUser existingUser = userMapper.selectById(user.getId());
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 如果修改了密码，需要加密
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null); // 不更新密码
        }
        
        userMapper.updateById(user);
    }

    /**
     * 转换为DTO
     */
    private UserManagementDTO convertToDTO(SysUser user) {
        UserManagementDTO dto = new UserManagementDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRoleId(user.getRoleId());
        dto.setRoleCode(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setApiQuota(user.getApiQuota());
        dto.setApiUsed(user.getApiUsed());
        dto.setHasCompliancePermission(user.getHasCompliancePermission());
        dto.setCreatedTime(user.getCreatedTime() != null ? user.getCreatedTime().toString() : null);
        
        // 获取角色名称
        if (user.getRoleId() != null) {
            SysRole role = roleMapper.selectById(user.getRoleId());
            if (role != null) {
                dto.setRoleName(role.getRoleName());
            }
        }
        
        return dto;
    }
}