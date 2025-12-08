package com.qna.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qna.platform.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户Mapper
 *
 * @author QnA Platform
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
