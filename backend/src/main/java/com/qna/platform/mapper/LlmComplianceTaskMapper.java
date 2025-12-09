package com.qna.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qna.platform.entity.LlmComplianceTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * LLM合规检测任务Mapper
 *
 * @author QnA Platform
 */
@Mapper
public interface LlmComplianceTaskMapper extends BaseMapper<LlmComplianceTask> {
}
