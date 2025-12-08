package com.qna.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统用户实体
 *
 * @author QnA Platform
 */
@Data
@TableName("sys_user")
public class SysUser implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（加密）
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 角色：ADMIN, USER
     */
    private String role;

    /**
     * 角色ID（关联sys_role表）
     */
    private Long roleId;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * API调用次数配额（每日）
     */
    private Integer apiQuota;

    /**
     * API已使用次数
     */
    private Integer apiUsed;

    /**
     * 配额重置时间
     */
    private LocalDateTime quotaResetTime;

    /**
     * 是否有合规检测权限：0-无，1-有
     */
    private Integer hasCompliancePermission;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
