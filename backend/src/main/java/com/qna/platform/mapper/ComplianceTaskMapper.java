package com.qna.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qna.platform.entity.ComplianceTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 合规检测任务Mapper
 *
 * @author QnA Platform
 */
@Mapper
public interface ComplianceTaskMapper extends BaseMapper<ComplianceTask> {
}
