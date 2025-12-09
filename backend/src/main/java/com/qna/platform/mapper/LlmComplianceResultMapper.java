package com.qna.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qna.platform.entity.LlmComplianceResult;
import org.apache.ibatis.annotations.Mapper;

/**
 * LLM合规检测结果Mapper
 *
 * @author QnA Platform
 */
@Mapper
public interface LlmComplianceResultMapper extends BaseMapper<LlmComplianceResult> {
}
