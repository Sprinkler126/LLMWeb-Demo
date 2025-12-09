package com.qna.platform.controller;

import com.qna.platform.common.Result;
import com.qna.platform.dto.UserProfileDTO;
import com.qna.platform.dto.UpdatePasswordDTO;
import com.qna.platform.dto.UpdateProfileDTO;
import com.qna.platform.service.UserProfileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户个人信息控制器
 * 不需要特殊权限，所有登录用户都可以访问
 *
 * @author QnA Platform
 */
@RestController
@RequestMapping("/user/profile")
@RequiredArgsConstructor
public class UserProfileController {
    
    private final UserProfileService userProfileService;
    
    /**
     * 获取当前用户的个人信息
     */
    @GetMapping
    public Result<UserProfileDTO> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("未登录");
        }
        
        UserProfileDTO profile = userProfileService.getUserProfile(userId);
        return Result.success(profile);
    }
    
    /**
     * 更新当前用户的个人信息（用户名、邮箱）
     */
    @PutMapping
    public Result<Void> updateProfile(HttpServletRequest request, @RequestBody UpdateProfileDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("未登录");
        }
        
        userProfileService.updateProfile(userId, dto);
        Result<Void> result = Result.success();
        result.setMessage("个人信息更新成功");
        return result;
    }
    
    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<Void> updatePassword(HttpServletRequest request, @RequestBody UpdatePasswordDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("未登录");
        }
        
        userProfileService.updatePassword(userId, dto);
        Result<Void> result = Result.success();
        result.setMessage("密码修改成功");
        return result;
    }
    
    /**
     * 获取当前用户的API使用统计
     */
    @GetMapping("/api-usage")
    public Result<Object> getApiUsage(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("未登录");
        }
        
        Object usage = userProfileService.getApiUsage(userId);
        return Result.success(usage);
    }
}
