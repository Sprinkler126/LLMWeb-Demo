package com.qna.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qna.platform.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色Mapper
 *
 * @author QnA Platform
 */
@Mapper
public interface RoleMapper extends BaseMapper<SysRole> {
    
    /**
     * 根据用户ID查询用户角色
     */
    @Select("SELECT r.* FROM sys_role r " +
            "JOIN sys_user u ON u.role_id = r.id " +
            "WHERE u.id = #{userId}")
    SysRole selectByUserId(Long userId);
    
    /**
     * 查询所有可用角色
     */
    @Select("SELECT * FROM sys_role WHERE status = 1 ORDER BY role_level")
    List<SysRole> selectEnabledRoles();
    
    /**
     * 根据角色代码查询角色
     */
    @Select("SELECT * FROM sys_role WHERE role_code = #{roleCode}")
    SysRole selectByRoleCode(String roleCode);
}
