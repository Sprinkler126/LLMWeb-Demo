package com.qna.platform.controller;

import com.qna.platform.common.Result;
import com.qna.platform.dto.ChatRequestDTO;
import com.qna.platform.service.ChatService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 对话控制器
 *
 * @author QnA Platform
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 发送消息
     */
    @PostMapping("/send")
    public Result<Map<String, Object>> sendMessage(
            @Validated @RequestBody ChatRequestDTO requestDTO,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Map<String, Object> result = chatService.sendMessage(userId, requestDTO);
            return Result.success("发送成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取会话历史
     */
    @GetMapping("/session/{sessionId}")
    public Result<Map<String, Object>> getSessionHistory(
            @PathVariable Long sessionId,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Map<String, Object> result = chatService.getSessionHistory(userId, sessionId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户的所有会话
     */
    @GetMapping("/sessions")
    public Result<Map<String, Object>> getUserSessions(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Map<String, Object> result = chatService.getUserSessions(userId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/session/{sessionId}")
    public Result<String> deleteSession(
            @PathVariable Long sessionId,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            chatService.deleteSession(userId, sessionId);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
