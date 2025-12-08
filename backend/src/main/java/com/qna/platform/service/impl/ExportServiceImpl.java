package com.qna.platform.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qna.platform.entity.ChatMessage;
import com.qna.platform.entity.ChatSession;
import com.qna.platform.mapper.ChatMessageMapper;
import com.qna.platform.mapper.ChatSessionMapper;
import com.qna.platform.service.ExportService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 导出服务实现
 *
 * @author QnA Platform
 */
@Service
public class ExportServiceImpl implements ExportService {

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ExportServiceImpl(ChatSessionMapper sessionMapper, ChatMessageMapper messageMapper) {
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
    }

    @Override
    public void exportSessionToJson(Long sessionId, Long userId, HttpServletResponse response) {
        // 验证权限
        ChatSession session = validateSession(sessionId, userId);

        // 获取消息列表
        List<ChatMessage> messages = getSessionMessages(sessionId);

        // 设置响应头
        setJsonResponseHeader(response, "session_" + sessionId + ".json");

        try (PrintWriter writer = response.getWriter()) {
            String json = JSONUtil.toJsonPrettyStr(messages);
            writer.write(json);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    @Override
    public void exportSessionToCsv(Long sessionId, Long userId, HttpServletResponse response) {
        // 验证权限
        ChatSession session = validateSession(sessionId, userId);

        // 获取消息列表
        List<ChatMessage> messages = getSessionMessages(sessionId);

        // 设置响应头
        setCsvResponseHeader(response, "session_" + sessionId + ".csv");

        try (PrintWriter writer = response.getWriter()) {
            // 写入CSV头
            writer.println("ID,Session ID,User ID,Role,Content,Tokens Used,Response Time,Compliance Status,Created Time");

            // 写入数据
            for (ChatMessage message : messages) {
                writer.printf("%d,%d,%d,%s,\"%s\",%s,%s,%s,%s%n",
                        message.getId(),
                        message.getSessionId(),
                        message.getUserId(),
                        message.getRole(),
                        escapeCsv(message.getContent()),
                        message.getTokensUsed() != null ? message.getTokensUsed() : "",
                        message.getResponseTime() != null ? message.getResponseTime() : "",
                        message.getComplianceStatus(),
                        message.getCreatedTime().format(DATE_FORMATTER)
                );
            }
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    @Override
    public void exportSessionToExcel(Long sessionId, Long userId, HttpServletResponse response) {
        // 验证权限
        ChatSession session = validateSession(sessionId, userId);

        // 获取消息列表
        List<ChatMessage> messages = getSessionMessages(sessionId);

        // 设置响应头
        setExcelResponseHeader(response, "session_" + sessionId + ".xlsx");

        try (Workbook workbook = new XSSFWorkbook();
             OutputStream out = response.getOutputStream()) {

            Sheet sheet = workbook.createSheet("Messages");

            // 创建标题样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Session ID", "User ID", "Role", "Content", "Tokens Used", 
                              "Response Time", "Compliance Status", "Created Time"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            int rowNum = 1;
            for (ChatMessage message : messages) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(message.getId());
                row.createCell(1).setCellValue(message.getSessionId());
                row.createCell(2).setCellValue(message.getUserId());
                row.createCell(3).setCellValue(message.getRole());
                row.createCell(4).setCellValue(message.getContent());
                row.createCell(5).setCellValue(message.getTokensUsed() != null ? message.getTokensUsed() : 0);
                row.createCell(6).setCellValue(message.getResponseTime() != null ? message.getResponseTime() : 0);
                row.createCell(7).setCellValue(message.getComplianceStatus());
                row.createCell(8).setCellValue(message.getCreatedTime().format(DATE_FORMATTER));
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            out.flush();

        } catch (IOException e) {
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    @Override
    public void exportAllMessages(Long userId, String format, HttpServletResponse response) {
        // 获取用户所有消息
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getUserId, userId)
                .orderByAsc(ChatMessage::getCreatedTime);
        List<ChatMessage> messages = messageMapper.selectList(wrapper);

        String fileName = "all_messages_" + userId;

        switch (format.toUpperCase()) {
            case "JSON":
                setJsonResponseHeader(response, fileName + ".json");
                try (PrintWriter writer = response.getWriter()) {
                    writer.write(JSONUtil.toJsonPrettyStr(messages));
                    writer.flush();
                } catch (IOException e) {
                    throw new RuntimeException("导出失败: " + e.getMessage());
                }
                break;

            case "CSV":
                setCsvResponseHeader(response, fileName + ".csv");
                try (PrintWriter writer = response.getWriter()) {
                    writer.println("ID,Session ID,User ID,Role,Content,Tokens Used,Response Time,Compliance Status,Created Time");
                    for (ChatMessage message : messages) {
                        writer.printf("%d,%d,%d,%s,\"%s\",%s,%s,%s,%s%n",
                                message.getId(),
                                message.getSessionId(),
                                message.getUserId(),
                                message.getRole(),
                                escapeCsv(message.getContent()),
                                message.getTokensUsed() != null ? message.getTokensUsed() : "",
                                message.getResponseTime() != null ? message.getResponseTime() : "",
                                message.getComplianceStatus(),
                                message.getCreatedTime().format(DATE_FORMATTER)
                        );
                    }
                    writer.flush();
                } catch (IOException e) {
                    throw new RuntimeException("导出失败: " + e.getMessage());
                }
                break;

            case "EXCEL":
                setExcelResponseHeader(response, fileName + ".xlsx");
                try (Workbook workbook = new XSSFWorkbook();
                     OutputStream out = response.getOutputStream()) {

                    Sheet sheet = workbook.createSheet("All Messages");

                    // 创建表头
                    Row headerRow = sheet.createRow(0);
                    String[] headers = {"ID", "Session ID", "User ID", "Role", "Content", "Tokens Used", 
                                      "Response Time", "Compliance Status", "Created Time"};
                    for (int i = 0; i < headers.length; i++) {
                        headerRow.createCell(i).setCellValue(headers[i]);
                    }

                    // 填充数据
                    int rowNum = 1;
                    for (ChatMessage message : messages) {
                        Row row = sheet.createRow(rowNum++);
                        row.createCell(0).setCellValue(message.getId());
                        row.createCell(1).setCellValue(message.getSessionId());
                        row.createCell(2).setCellValue(message.getUserId());
                        row.createCell(3).setCellValue(message.getRole());
                        row.createCell(4).setCellValue(message.getContent());
                        row.createCell(5).setCellValue(message.getTokensUsed() != null ? message.getTokensUsed() : 0);
                        row.createCell(6).setCellValue(message.getResponseTime() != null ? message.getResponseTime() : 0);
                        row.createCell(7).setCellValue(message.getComplianceStatus());
                        row.createCell(8).setCellValue(message.getCreatedTime().format(DATE_FORMATTER));
                    }

                    workbook.write(out);
                    out.flush();

                } catch (IOException e) {
                    throw new RuntimeException("导出失败: " + e.getMessage());
                }
                break;

            default:
                throw new RuntimeException("不支持的导出格式: " + format);
        }
    }

    /**
     * 验证会话归属
     */
    private ChatSession validateSession(Long sessionId, Long userId) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new RuntimeException("会话不存在或无权访问");
        }
        return session;
    }

    /**
     * 获取会话消息
     */
    private List<ChatMessage> getSessionMessages(Long sessionId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
                .orderByAsc(ChatMessage::getCreatedTime);
        return messageMapper.selectList(wrapper);
    }

    /**
     * 设置JSON响应头
     */
    private void setJsonResponseHeader(HttpServletResponse response, String fileName) {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + 
                URLEncoder.encode(fileName, StandardCharsets.UTF_8));
    }

    /**
     * 设置CSV响应头
     */
    private void setCsvResponseHeader(HttpServletResponse response, String fileName) {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + 
                URLEncoder.encode(fileName, StandardCharsets.UTF_8));
    }

    /**
     * 设置Excel响应头
     */
    private void setExcelResponseHeader(HttpServletResponse response, String fileName) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=" + 
                URLEncoder.encode(fileName, StandardCharsets.UTF_8));
    }

    /**
     * CSV特殊字符转义
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\"", "\"\"");
    }
}
