package com.qna.platform.service;

import com.qna.platform.dto.ChatRequestDTO;
import java.util.Map;

/**
 * 对话服务接口
 *
 * @author QnA Platform
 */
public interface ChatService {

    /**
     * 发送消息并获取回复
     *
     * @param userId 用户ID
     * @param requestDTO 请求参数
     * @return 回复消息和相关信息
     */
    Map<String, Object> sendMessage(Long userId, ChatRequestDTO requestDTO);

    /**
     * 获取会话历史
     *
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 消息列表
     */
    Map<String, Object> getSessionHistory(Long userId, Long sessionId);

    /**
     * 获取用户的所有会话
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    Map<String, Object> getUserSessions(Long userId);

    /**
     * 删除会话
     *
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 是否成功
     */
    boolean deleteSession(Long userId, Long sessionId);
}
