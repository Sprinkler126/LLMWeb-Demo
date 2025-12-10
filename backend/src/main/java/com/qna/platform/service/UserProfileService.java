package com.qna.platform.service;

import com.qna.platform.dto.UpdatePasswordDTO;
import com.qna.platform.dto.UpdateProfileDTO;
import com.qna.platform.dto.UserProfileDTO;

/**
 * 用户个人信息服务接口
 *
 * @author QnA Platform
 */
public interface UserProfileService {
    
    /**
     * 获取用户个人信息
     *
     * @param userId 用户ID
     * @return 用户个人信息
     */
    UserProfileDTO getUserProfile(Long userId);
    
    /**
     * 更新用户个人信息
     *
     * @param userId 用户ID
     * @param dto 更新信息
     */
    void updateProfile(Long userId, UpdateProfileDTO dto);
    
    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param dto 密码信息
     */
    void updatePassword(Long userId, UpdatePasswordDTO dto);
    
    /**
     * 获取用户API使用统计
     *
     * @param userId 用户ID
     * @return API使用统计
     */
    Object getApiUsage(Long userId);

    /**
     * 检查并重置用户API配额
     *
     * @param userId 用户ID
     */
    void checkAndResetApiQuota(Long userId);
}
