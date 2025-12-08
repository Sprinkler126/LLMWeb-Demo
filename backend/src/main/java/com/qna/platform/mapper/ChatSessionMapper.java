package com.qna.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qna.platform.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * 对话会话Mapper
 *
 * @author QnA Platform
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}
