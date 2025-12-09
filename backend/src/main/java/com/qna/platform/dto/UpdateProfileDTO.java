package com.qna.platform.dto;

import lombok.Data;

/**
 * 更新个人信息DTO
 *
 * @author QnA Platform
 */
@Data
public class UpdateProfileDTO {
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 用户名（可选，通常不允许修改）
     */
    private String username;
}
