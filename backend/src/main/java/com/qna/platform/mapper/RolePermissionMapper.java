package com.qna.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qna.platform.entity.SysRolePermission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色权限关联Mapper
 *
 * @author QnA Platform
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<SysRolePermission> {
    
    /**
     * 删除角色的所有权限
     */
    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    int deleteByRoleId(Long roleId);
    
    /**
     * 根据角色ID查询权限ID列表
     */
    @Select("SELECT permission_id FROM sys_role_permission WHERE role_id = #{roleId}")
    List<Long> selectPermissionIdsByRoleId(Long roleId);
}
