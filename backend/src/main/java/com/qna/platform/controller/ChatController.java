package com.qna.platform.controller;

import com.qna.platform.annotation.RequirePermission;
import com.qna.platform.common.Result;
import com.qna.platform.dto.ChatRequestDTO;
import com.qna.platform.dto.FileUploadResult;
import com.qna.platform.service.ChatService;
import com.qna.platform.util.FileParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 对话控制器
 * 需要API使用权限
 *
 * @author QnA Platform
 */
@Slf4j
@RestController
@RequestMapping("/chat")
@RequirePermission("API_USE")
public class ChatController {

    private final ChatService chatService;
    private final FileParser fileParser;

    public ChatController(ChatService chatService, FileParser fileParser) {
        this.chatService = chatService;
        this.fileParser = fileParser;
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
    
    /**
     * 上传文件并提取文本内容
     * 支持多文件上传
     */
    @PostMapping("/upload-files")
    public Result<List<FileUploadResult>> uploadFiles(
            @RequestParam("files") MultipartFile[] files,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            log.info("用户 {} 上传了 {} 个文件", userId, files.length);
            
            List<FileUploadResult> results = new ArrayList<>();
            
            // 限制文件数量
            if (files.length > 10) {
                return Result.error("最多只能上传10个文件");
            }
            
            for (MultipartFile file : files) {
                FileUploadResult result = processFile(file);
                results.add(result);
            }
            
            return Result.success("文件上传成功", results);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }
    
    /**
     * 处理单个文件
     */
    private FileUploadResult processFile(MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            long fileSize = file.getSize();
            
            // 检查文件大小（限制为10MB）
            if (!fileParser.checkFileSize(fileSize, 10)) {
                return FileUploadResult.builder()
                        .fileName(fileName)
                        .fileSize(fileSize)
                        .success(false)
                        .errorMessage("文件大小超过10MB限制")
                        .build();
            }
            
            // 解析文件内容
            String content = fileParser.parseFile(file);
            
            // 限制内容长度（100KB文本）
            if (content.length() > 100000) {
                content = content.substring(0, 100000) + "\n\n[内容过长，已截断]";
            }
            
            return FileUploadResult.builder()
                    .fileName(fileName)
                    .fileType(file.getContentType())
                    .fileSize(fileSize)
                    .content(content)
                    .success(true)
                    .build();
                    
        } catch (Exception e) {
            log.error("处理文件失败：{}", file.getOriginalFilename(), e);
            return FileUploadResult.builder()
                    .fileName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }
}
