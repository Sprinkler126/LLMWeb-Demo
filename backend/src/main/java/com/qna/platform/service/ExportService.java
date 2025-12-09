package com.qna.platform.service;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 导出服务接口
 *
 * @author QnA Platform
 */
public interface ExportService {

    /**
     * 导出会话消息（JSON格式）
     *
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param response HTTP响应
     */
    void exportSessionToJson(Long sessionId, Long userId, HttpServletResponse response);

    /**
     * 导出会话消息（CSV格式）
     *
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param response HTTP响应
     */
    void exportSessionToCsv(Long sessionId, Long userId, HttpServletResponse response);

    /**
     * 导出会话消息（Excel格式）
     *
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param response HTTP响应
     */
    void exportSessionToExcel(Long sessionId, Long userId, HttpServletResponse response);

    /**
     * 导出用户所有聊天记录
     *
     * @param userId 用户ID
     * @param format 格式：JSON, CSV, EXCEL
     * @param response HTTP响应
     */
    void exportAllMessages(Long userId, String format, HttpServletResponse response);
    
    /**
     * 管理员导出指定用户的会话消息（JSON格式）
     *
     * @param sessionId 会话ID
     * @param targetUserId 目标用户ID
     * @param currentUserId 当前管理员用户ID
     * @param response HTTP响应
     */
    void adminExportSessionToJson(Long sessionId, Long targetUserId, Long currentUserId, HttpServletResponse response);
    
    /**
     * 获取指定用户的会话列表
     *
     * @param targetUserId 目标用户ID
     * @param currentUserId 当前用户ID
     * @return 会话列表
     */
    Object getUserSessionList(Long targetUserId, Long currentUserId);
}
