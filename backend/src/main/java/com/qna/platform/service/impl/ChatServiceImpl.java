package com.qna.platform.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qna.platform.dto.ChatRequestDTO;
import com.qna.platform.entity.*;
import com.qna.platform.enums.ComplianceStatus;
import com.qna.platform.mapper.*;
import com.qna.platform.service.ChatService;
import com.qna.platform.util.AiApiClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 对话服务实现
 *
 * @author QnA Platform
 */
@Service
public class ChatServiceImpl implements ChatService {

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final ApiConfigMapper apiConfigMapper;
    private final SysUserMapper userMapper;
    private final AiApiClient aiApiClient;
    private final com.qna.platform.util.ComplianceClient complianceClient;

    public ChatServiceImpl(ChatSessionMapper sessionMapper,
                          ChatMessageMapper messageMapper,
                          ApiConfigMapper apiConfigMapper,
                          SysUserMapper userMapper,
                          AiApiClient aiApiClient,
                          com.qna.platform.util.ComplianceClient complianceClient) {
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
        this.apiConfigMapper = apiConfigMapper;
        this.userMapper = userMapper;
        this.aiApiClient = aiApiClient;
        this.complianceClient = complianceClient;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> sendMessage(Long userId, ChatRequestDTO requestDTO) {
        // 检查用户配额
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查配额是否需要重置
        if (user.getQuotaResetTime() != null && LocalDateTime.now().isAfter(user.getQuotaResetTime())) {
            user.setApiUsed(0);
            user.setQuotaResetTime(LocalDateTime.now().plusDays(1));
            userMapper.updateById(user);
        }

        if (user.getApiUsed() >= user.getApiQuota()) {
            throw new RuntimeException("API调用次数已达上限");
        }

        // 获取API配置
        ApiConfig apiConfig = apiConfigMapper.selectById(requestDTO.getApiConfigId());
        if (apiConfig == null || apiConfig.getStatus() == 0) {
            throw new RuntimeException("API配置不存在或已禁用");
        }

        // 获取或创建会话
        ChatSession session;
        if (requestDTO.getSessionId() == null) {
            // 创建新会话
            session = new ChatSession();
            session.setUserId(userId);
            session.setApiConfigId(requestDTO.getApiConfigId());
            session.setSessionTitle(StrUtil.isBlank(requestDTO.getSessionTitle()) 
                    ? "会话 - " + LocalDateTime.now() 
                    : requestDTO.getSessionTitle());
            session.setSessionStatus("ACTIVE");
            session.setMessageCount(0);
            sessionMapper.insert(session);
        } else {
            session = sessionMapper.selectById(requestDTO.getSessionId());
            if (session == null || !session.getUserId().equals(userId)) {
                throw new RuntimeException("会话不存在或无权访问");
            }
        }

        // 保存用户消息
        ChatMessage userMessage = new ChatMessage();
        userMessage.setSessionId(session.getId());
        userMessage.setUserId(userId);
        userMessage.setApiConfigId(requestDTO.getApiConfigId());
        userMessage.setRole("user");
        userMessage.setContent(requestDTO.getMessage());
        userMessage.setComplianceStatus(ComplianceStatus.UNCHECKED.name());
        messageMapper.insert(userMessage);
        
        // 对用户消息进行合规检测（异步）
        checkMessageCompliance(userMessage);

        // 获取会话历史（最近10条）
        List<Map<String, String>> messages = buildMessageHistory(session.getId());
        messages.add(Map.of("role", "user", "content", requestDTO.getMessage()));

        // 调用AI API
        long startTime = System.currentTimeMillis();
        String aiResponse;
        ChatMessage assistantMessage = new ChatMessage();
        
        try {
            aiResponse = aiApiClient.callAiApi(apiConfig, messages);
            long responseTime = System.currentTimeMillis() - startTime;

            // 保存AI回复
            assistantMessage.setSessionId(session.getId());
            assistantMessage.setUserId(userId);
            assistantMessage.setApiConfigId(requestDTO.getApiConfigId());
            assistantMessage.setRole("assistant");
            assistantMessage.setContent(aiResponse);
            assistantMessage.setResponseTime((int) responseTime);
            assistantMessage.setComplianceStatus(ComplianceStatus.UNCHECKED.name());
            messageMapper.insert(assistantMessage);
            
            // 对AI回复进行合规检测（异步）
            checkMessageCompliance(assistantMessage);

            // 更新会话消息数量
            session.setMessageCount(session.getMessageCount() + 2);
            sessionMapper.updateById(session);

            // 更新用户API使用次数
            user.setApiUsed(user.getApiUsed() + 1);
            userMapper.updateById(user);

        } catch (Exception e) {
            // 记录错误
            assistantMessage.setSessionId(session.getId());
            assistantMessage.setUserId(userId);
            assistantMessage.setApiConfigId(requestDTO.getApiConfigId());
            assistantMessage.setRole("assistant");
            assistantMessage.setContent("抱歉，调用AI服务时出现错误");
            assistantMessage.setErrorMessage(e.getMessage());
            messageMapper.insert(assistantMessage);

            throw new RuntimeException("调用AI服务失败: " + e.getMessage());
        }

        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", session.getId());
        result.put("messageId", assistantMessage.getId());
        result.put("content", aiResponse);
        result.put("responseTime", assistantMessage.getResponseTime());
        result.put("apiUsed", user.getApiUsed());
        result.put("apiQuota", user.getApiQuota());

        return result;
    }

    @Override
    public Map<String, Object> getSessionHistory(Long userId, Long sessionId) {
        // 验证会话归属
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new RuntimeException("会话不存在或无权访问");
        }

        // 查询消息列表
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
                .orderByAsc(ChatMessage::getCreatedTime);
        List<ChatMessage> messages = messageMapper.selectList(wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("session", session);
        result.put("messages", messages);

        return result;
    }

    @Override
    public Map<String, Object> getUserSessions(Long userId) {
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSession::getUserId, userId)
                .orderByDesc(ChatSession::getUpdatedTime);
        List<ChatSession> sessions = sessionMapper.selectList(wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("sessions", sessions);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSession(Long userId, Long sessionId) {
        // 验证会话归属
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new RuntimeException("会话不存在或无权访问");
        }

        // 删除会话的所有消息
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId);
        messageMapper.delete(wrapper);

        // 删除会话
        return sessionMapper.deleteById(sessionId) > 0;
    }

    /**
     * 构建消息历史（用于API调用）
     */
    private List<Map<String, String>> buildMessageHistory(Long sessionId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
                .orderByDesc(ChatMessage::getCreatedTime)
                .last("LIMIT 10");
        
        List<ChatMessage> messages = messageMapper.selectList(wrapper);
        Collections.reverse(messages); // 按时间正序

        List<Map<String, String>> result = new ArrayList<>();
        for (ChatMessage msg : messages) {
            if (msg.getErrorMessage() == null) { // 只包含成功的消息
                result.add(Map.of(
                        "role", msg.getRole(),
                        "content", msg.getContent()
                ));
            }
        }

        return result;
    }
    
    /**
     * 检测消息合规性
     * 
     * @param message 待检测的消息
     */
    private void checkMessageCompliance(ChatMessage message) {
        try {
            // 调用Python合规检测服务
            cn.hutool.json.JSONObject result = complianceClient.checkContent(message.getContent());
            
            // 更新消息的合规状态
            String complianceResult = result.getStr("result", "PASS");
            message.setComplianceStatus(complianceResult);
            message.setComplianceResult(result.toString());
            
            // 更新到数据库
            messageMapper.updateById(message);
            
        } catch (Exception e) {
            // 检测失败时，保持未检测状态
            org.slf4j.LoggerFactory.getLogger(getClass())
                    .error("合规检测失败: messageId={}, error={}", message.getId(), e.getMessage());
        }
    }
}
