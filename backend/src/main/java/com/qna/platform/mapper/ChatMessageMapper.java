package com.qna.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qna.platform.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 对话消息Mapper
 *
 * @author QnA Platform
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
