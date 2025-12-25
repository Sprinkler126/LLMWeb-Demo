package com.qna.platform.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 文件解析工具类
 * 用于提取各种文本类文件的内容
 *
 * @author QnA Platform
 */
@Slf4j
@Component
public class FileParser {
    
    /**
     * 解析文件内容
     *
     * @param file 上传的文件
     * @return 提取的文本内容
     * @throws IOException IO异常
     */
    public String parseFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        String fileExtension = getFileExtension(fileName).toLowerCase();
        log.info("解析文件：{}，类型：{}", fileName, fileExtension);
        
        // 根据文件类型选择解析方法
        switch (fileExtension) {
            case "txt":
            case "md":
            case "log":
            case "json":
            case "xml":
            case "csv":
            case "java":
            case "py":
            case "js":
            case "ts":
            case "html":
            case "css":
            case "yml":
            case "yaml":
            case "properties":
                return parseTextFile(file);
            
            case "pdf":
                // PDF解析需要额外的库（如Apache PDFBox）
                // 暂时返回提示信息
                throw new UnsupportedOperationException("PDF文件解析暂未实现，请转换为TXT格式");
            
            case "docx":
            case "doc":
                // Word文档解析需要额外的库（如Apache POI）
                // 暂时返回提示信息
                throw new UnsupportedOperationException("Word文档解析暂未实现，请转换为TXT格式");
            
            default:
                throw new UnsupportedOperationException("不支持的文件类型：" + fileExtension);
        }
    }
    
    /**
     * 解析纯文本文件
     */
    private String parseTextFile(MultipartFile file) throws IOException {
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        return content.toString();
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }
    
    /**
     * 检查文件大小是否在限制内
     *
     * @param fileSize 文件大小（字节）
     * @param maxSizeMB 最大大小（MB）
     * @return 是否在限制内
     */
    public boolean checkFileSize(long fileSize, int maxSizeMB) {
        long maxSizeBytes = (long) maxSizeMB * 1024 * 1024;
        return fileSize <= maxSizeBytes;
    }
}
