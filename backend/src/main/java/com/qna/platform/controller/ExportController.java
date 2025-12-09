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
        System.out.println("🔍 导出接口被调用 - sessionId: " + sessionId);
        System.out.println("🔍 请求 URI: " + request.getRequestURI());
        System.out.println("🔍 Token 参数: " + (token != null ? "已提供" : "未提供"));
        System.out.println("🔍 Authorization Header: " + request.getHeader("Authorization"));
        
        try {
            Long userId = getUserIdFromTokenOrRequest(token, request);
            System.out.println("✅ 用户验证成功 - userId: " + userId);
            exportService.exportSessionToJson(sessionId, userId, response);
        } catch (Exception e) {
            System.err.println("❌ 导出失败: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
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
     * 管理员导出指定用户的指定会话（JSON）
     * 需要管理员权限
     */
    @GetMapping("/admin/session/{sessionId}/json")
    public void adminExportSessionJson(
            @PathVariable Long sessionId,
            @RequestParam Long targetUserId,
            @RequestParam(required = false) String token,
            HttpServletRequest request,
            HttpServletResponse response) {
        Long currentUserId = getUserIdFromTokenOrRequest(token, request);
        // 检查当前用户是否是管理员（在 Service 中实现）
        exportService.adminExportSessionToJson(sessionId, targetUserId, currentUserId, response);
    }
    
    /**
     * 管理员导出指定用户的所有会话列表
     * 用于搜索和选择
     */
    @GetMapping("/admin/user/{targetUserId}/sessions")
    public Result<?> getUserSessions(
            @PathVariable Long targetUserId,
            HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        if (currentUserId == null) {
            return Result.error("未登录");
        }
        Object sessions = exportService.getUserSessionList(targetUserId, currentUserId);
        return Result.success(sessions);
    }
    
    /**
     * 测试接口 - 验证拦截器是否跳过导出路径
     */
    @GetMapping("/test")
    public Result<String> test(@RequestParam(required = false) String token) {
        return Result.success("导出接口拦截器配置正常，token: " + (token != null ? "已提供" : "未提供"));
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
