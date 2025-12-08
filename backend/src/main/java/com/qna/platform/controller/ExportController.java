package com.qna.platform.controller;

import com.qna.platform.common.Result;
import com.qna.platform.service.ExportService;
import com.qna.platform.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 导出控制器
 *
 * @author QnA Platform
 */
@RestController
@RequestMapping("/export")
@RequiredArgsConstructor
public class ExportController {
    
    // 注意：导出接口不添加权限注解，因为需要通过 URL 参数传递 token

    private final ExportService exportService;
    private final JwtUtil jwtUtil;

    /**
     * 导出会话消息（JSON）
     */
    @GetMapping("/session/{sessionId}/json")
    public void exportSessionJson(
            @PathVariable Long sessionId,
            @RequestParam(required = false) String token,
            HttpServletRequest request,
            HttpServletResponse response) {
        Long userId = getUserIdFromTokenOrRequest(token, request);
        exportService.exportSessionToJson(sessionId, userId, response);
    }

    /**
     * 导出会话消息（CSV）
     */
    @GetMapping("/session/{sessionId}/csv")
    public void exportSessionCsv(
            @PathVariable Long sessionId,
            @RequestParam(required = false) String token,
            HttpServletRequest request,
            HttpServletResponse response) {
        Long userId = getUserIdFromTokenOrRequest(token, request);
        exportService.exportSessionToCsv(sessionId, userId, response);
    }

    /**
     * 导出会话消息（Excel）
     */
    @GetMapping("/session/{sessionId}/excel")
    public void exportSessionExcel(
            @PathVariable Long sessionId,
            @RequestParam(required = false) String token,
            HttpServletRequest request,
            HttpServletResponse response) {
        Long userId = getUserIdFromTokenOrRequest(token, request);
        exportService.exportSessionToExcel(sessionId, userId, response);
    }

    /**
     * 导出用户所有消息
     */
    @GetMapping("/all/{format}")
    public void exportAllMessages(
            @PathVariable String format,
            @RequestParam(required = false) String token,
            HttpServletRequest request,
            HttpServletResponse response) {
        Long userId = getUserIdFromTokenOrRequest(token, request);
        exportService.exportAllMessages(userId, format, response);
    }
    
    /**
     * 从 token 参数或 request attribute 获取用户ID
     */
    private Long getUserIdFromTokenOrRequest(String token, HttpServletRequest request) {
        // 优先从 request attribute 获取（正常请求）
        Long userId = (Long) request.getAttribute("userId");
        if (userId != null) {
            return userId;
        }
        
        // 如果没有，从 URL 参数的 token 获取（用于导出下载）
        if (token != null && !token.isEmpty()) {
            try {
                return jwtUtil.getUserIdFromToken(token);
            } catch (Exception e) {
                throw new RuntimeException("Token 无效或已过期");
            }
        }
        
        throw new RuntimeException("未授权访问");
    }
}
