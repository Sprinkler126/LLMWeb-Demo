package com.qna.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qna.platform.dto.LoginDTO;
import com.qna.platform.entity.SysUser;
import com.qna.platform.enums.UserRole;
import com.qna.platform.mapper.SysUserMapper;
import com.qna.platform.service.AuthService;
import com.qna.platform.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务实现
 *
 * @author QnA Platform
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthServiceImpl(SysUserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public Map<String, Object> login(LoginDTO loginDTO) {
        // 查询用户
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, loginDTO.getUsername());
        SysUser user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("role", user.getRole());
        result.put("avatar", user.getAvatar());
        result.put("apiQuota", user.getApiQuota());
        result.put("apiUsed", user.getApiUsed());
        result.put("hasCompliancePermission", user.getHasCompliancePermission());

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean register(LoginDTO loginDTO) {
        // 检查用户名是否存在
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, loginDTO.getUsername());
        Long count = userMapper.selectCount(wrapper);

        if (count > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建新用户
        SysUser user = new SysUser();
        user.setUsername(loginDTO.getUsername());
        user.setPassword(passwordEncoder.encode(loginDTO.getPassword()));
        user.setNickname(loginDTO.getUsername());
        user.setRole(UserRole.USER.name());
        user.setStatus(1);
        user.setApiQuota(100);
        user.setApiUsed(0);
        user.setQuotaResetTime(LocalDateTime.now().plusDays(1));
        user.setHasCompliancePermission(0);

        return userMapper.insert(user) > 0;
    }
}
