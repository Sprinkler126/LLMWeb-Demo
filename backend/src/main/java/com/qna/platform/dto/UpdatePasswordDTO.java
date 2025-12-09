package com.qna.platform.dto;

import lombok.Data;

/**
 * 修改密码DTO
 *
 * @author QnA Platform
 */
@Data
public class UpdatePasswordDTO {
    
    /**
     * 原密码
     */
    private String oldPassword;
    
    /**
     * 新密码
     */
    private String newPassword;
}
