package com.qna.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统权限实体
 *
 * @author QnA Platform
 */
@Data
@TableName("sys_permission")
public class SysPermission {
    /**
     * 权限ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 权限代码
     */
    private String permissionCode;

    /**
     * 权限名称
     */
    private String permissionName;

    /**
     * 权限类型：MENU-菜单，BUTTON-按钮，API-接口
     */
    private String permissionType;

    /**
     * 父权限ID（0表示顶级权限）
     */
    private Long parentId;

    /**
     * 权限路径（前端路由）
     */
    private String path;

    /**
     * 权限描述
     */
    private String description;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
