package com.qna.platform.controller;

import com.qna.platform.common.Result;
import com.qna.platform.service.ExportService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

/**
 * 导出控制器
 *
 * @author QnA Platform
 */
@RestController
@RequestMapping("/export")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    /**
     * 导出会话消息（JSON）
     */
    @GetMapping("/session/{sessionId}/json")
    public void exportSessionJson(
            @PathVariable Long sessionId,
            HttpServletRequest request,
            HttpServletResponse response) {
        Long userId = (Long) request.getAttribute("userId");
        exportService.exportSessionToJson(sessionId, userId, response);
    }

    /**
     * 导出会话消息（CSV）
     */
    @GetMapping("/session/{sessionId}/csv")
    public void exportSessionCsv(
            @PathVariable Long sessionId,
            HttpServletRequest request,
            HttpServletResponse response) {
        Long userId = (Long) request.getAttribute("userId");
        exportService.exportSessionToCsv(sessionId, userId, response);
    }

    /**
     * 导出会话消息（Excel）
     */
    @GetMapping("/session/{sessionId}/excel")
    public void exportSessionExcel(
            @PathVariable Long sessionId,
            HttpServletRequest request,
            HttpServletResponse response) {
        Long userId = (Long) request.getAttribute("userId");
        exportService.exportSessionToExcel(sessionId, userId, response);
    }

    /**
     * 导出用户所有消息
     */
    @GetMapping("/all/{format}")
    public void exportAllMessages(
            @PathVariable String format,
            HttpServletRequest request,
            HttpServletResponse response) {
        Long userId = (Long) request.getAttribute("userId");
        exportService.exportAllMessages(userId, format, response);
    }
}
