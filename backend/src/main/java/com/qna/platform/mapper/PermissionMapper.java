package com.qna.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qna.platform.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限Mapper
 *
 * @author QnA Platform
 */
@Mapper
public interface PermissionMapper extends BaseMapper<SysPermission> {
    
    /**
     * 根据角色ID查询权限列表
     */
    @Select("SELECT p.* FROM sys_permission p " +
            "JOIN sys_role_permission rp ON rp.permission_id = p.id " +
            "WHERE rp.role_id = #{roleId} AND p.status = 1")
    List<SysPermission> selectByRoleId(Long roleId);
    
    /**
     * 根据用户ID查询用户所有权限
     */
    @Select("SELECT p.* FROM sys_permission p " +
            "JOIN sys_role_permission rp ON rp.permission_id = p.id " +
            "JOIN sys_user u ON u.role_id = rp.role_id " +
            "WHERE u.id = #{userId} AND p.status = 1")
    List<SysPermission> selectByUserId(Long userId);
    
    /**
     * 查询所有权限（树形结构）
     */
    @Select("SELECT * FROM sys_permission WHERE status = 1 ORDER BY parent_id, sort_order")
    List<SysPermission> selectAllTree();
    
    /**
     * 根据权限代码查询权限
     */
    @Select("SELECT * FROM sys_permission WHERE permission_code = #{permissionCode}")
    SysPermission selectByPermissionCode(String permissionCode);
}
