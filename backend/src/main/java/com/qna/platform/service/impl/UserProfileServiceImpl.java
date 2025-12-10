package com.qna.platform.service.impl;

import com.qna.platform.dto.UpdatePasswordDTO;
import com.qna.platform.dto.UpdateProfileDTO;
import com.qna.platform.dto.UserProfileDTO;
import com.qna.platform.entity.SysRole;
import com.qna.platform.entity.SysUser;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qna.platform.mapper.RoleMapper;
import com.qna.platform.mapper.SysUserMapper;
import com.qna.platform.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户个人信息服务实现类
 *
 * @author QnA Platform
 */
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {
    
    private final SysUserMapper userMapper;
    private final RoleMapper roleMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    
    @Override
    public UserProfileDTO getUserProfile(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 获取用户角色
        SysRole role = roleMapper.selectByUserId(userId);
        // 检查是否需要重置API配额
        checkAndResetApiQuota(user);
        
        return UserProfileDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roleName(role != null ? role.getRoleName() : "普通用户")
                .roleCode(role != null ? role.getRoleCode() : "USER")
                .status(user.getStatus())
                .apiQuota(user.getApiQuota())
                .apiUsed(user.getApiUsed())
                .hasCompliancePermission(user.getHasCompliancePermission())
                .createdAt(user.getCreatedTime())
                .lastLoginTime(user.getUpdatedTime())  // 使用更新时间作为最后活动时间
                .build();
    }
    
    @Override
    @Transactional
    public void updateProfile(Long userId, UpdateProfileDTO dto) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 检查邮箱是否已被其他用户使用
        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("email", dto.getEmail());
            SysUser existingUser = userMapper.selectOne(queryWrapper);
            if (existingUser != null && !existingUser.getId().equals(userId)) {
                throw new RuntimeException("邮箱已被使用");
            }
            user.setEmail(dto.getEmail());
        }
        
        // 通常不允许修改用户名，但如果前端传了且不同，也可以支持
        // 这里先注释掉，如果需要可以打开
        /*
        if (dto.getUsername() != null && !dto.getUsername().equals(user.getUsername())) {
            SysUser existingUser = userMapper.selectByUsername(dto.getUsername());
            if (existingUser != null && !existingUser.getId().equals(userId)) {
                throw new RuntimeException("用户名已被使用");
            }
            user.setUsername(dto.getUsername());
        }
        */
        
        userMapper.updateById(user);
    }
    
    @Override
    @Transactional
    public void updatePassword(Long userId, UpdatePasswordDTO dto) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 验证原密码
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        
        // 验证新密码格式
        if (dto.getNewPassword() == null || dto.getNewPassword().length() < 6) {
            throw new RuntimeException("新密码长度不能少于6位");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }
    
    @Override
    public Object getApiUsage(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        Map<String, Object> usage = new HashMap<>();
        usage.put("apiQuota", user.getApiQuota());
        usage.put("apiUsed", user.getApiUsed());
        usage.put("remaining", user.getApiQuota() - user.getApiUsed());
        usage.put("usagePercent", user.getApiQuota() > 0 ? 
            (double) user.getApiUsed() / user.getApiQuota() * 100 : 0);
        
        return usage;
    }

    @Override
    @Transactional
    public Map<String, Object> checkAndResetApiQuota(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        boolean wasReset = false;
        
        // 检查是否需要重置
        if (user.getApiUsed() != null && user.getApiUsed() > 0
                && user.getQuotaResetTime() != null
                && LocalDateTime.now().isAfter(user.getQuotaResetTime())) {
            
            // 重置配额
            user.setApiUsed(0);
            // 将重置时间设置为今天的24:00（即明天的00:00）
            user.setQuotaResetTime(LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIDNIGHT));
            userMapper.updateById(user);
            wasReset = true;
        }
        
        // 返回详细信息
        result.put("reset", wasReset);
        result.put("apiQuota", user.getApiQuota());
        result.put("apiUsed", user.getApiUsed());
        result.put("remaining", user.getApiQuota() - user.getApiUsed());
        result.put("nextResetTime", user.getQuotaResetTime());
        result.put("message", wasReset ? "配额已重置" : "配额无需重置");
        
        return result;
    }


    private void checkAndResetApiQuota(SysUser user) {
        if (user.getApiUsed() != null && user.getApiUsed() > 0
                && user.getQuotaResetTime() != null
                && LocalDateTime.now().isAfter(user.getQuotaResetTime())) {

            user.setApiUsed(0);
            // 将重置时间设置为今天的24:00（即明天的00:00）
            user.setQuotaResetTime(LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIDNIGHT));
            userMapper.updateById(user);
        }
    }
}
