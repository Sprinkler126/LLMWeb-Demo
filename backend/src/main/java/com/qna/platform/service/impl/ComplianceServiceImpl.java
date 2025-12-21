package com.qna.platform.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qna.platform.common.PageResult;
import com.qna.platform.dto.BatchComplianceResult;
import com.qna.platform.dto.ComplianceCheckDTO;
import com.qna.platform.entity.*;
import com.qna.platform.mapper.*;
import com.qna.platform.service.ComplianceService;
import com.qna.platform.util.ComplianceClient;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 合规检测服务实现
 * 预留接口给Python服务
 *
 * @author QnA Platform
 */
@Slf4j
@Service
public class ComplianceServiceImpl implements ComplianceService {

    @Value("${app.compliance.service-url}")
    private String complianceServiceUrl;

    @Value("${app.compliance.timeout}")
    private int complianceTimeout;

    private final ComplianceTaskMapper taskMapper;
    private final ComplianceResultMapper resultMapper;
    private final ChatMessageMapper messageMapper;
    private final SysUserMapper userMapper;
    private final OkHttpClient httpClient;
    private final ComplianceClient complianceClient;
    private final ObjectMapper objectMapper;

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    public ComplianceServiceImpl(ComplianceTaskMapper taskMapper,
                                ComplianceResultMapper resultMapper,
                                ChatMessageMapper messageMapper,
                                ComplianceClient complianceClient,
                                ObjectMapper objectMapper,
                                SysUserMapper userMapper) {
        this.taskMapper = taskMapper;
        this.resultMapper = resultMapper;
        this.messageMapper = messageMapper;
        this.complianceClient = complianceClient;
        this.objectMapper = objectMapper;
        this.userMapper = userMapper;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(complianceTimeout, java.util.concurrent.TimeUnit.MILLISECONDS)
                .readTimeout(complianceTimeout, java.util.concurrent.TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createComplianceTask(ComplianceCheckDTO checkDTO, Long userId) {
        // 检查用户权限
        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getHasCompliancePermission() == 0) {
            throw new RuntimeException("您没有合规检测权限");
        }

        // 创建任务
        ComplianceTask task = new ComplianceTask();
        task.setUserId(userId);
        task.setTaskName(checkDTO.getTaskName());
        task.setTaskType(checkDTO.getTaskType());
        task.setFilePath(checkDTO.getFilePath());
        task.setTaskStatus("PENDING");
        task.setTotalRecords(0);
        task.setCheckedRecords(0);
        task.setPassCount(0);
        task.setFailCount(0);

        taskMapper.insert(task);

        // 根据任务类型获取待检测的数据
        if ("LOG".equals(checkDTO.getTaskType())) {
            // 从日志加载数据
            List<ChatMessage> messages = loadMessagesFromLog(checkDTO);
            task.setTotalRecords(messages.size());
            taskMapper.updateById(task);
        } else if ("FILE".equals(checkDTO.getTaskType())) {
            // 从文件加载数据（这里只是创建任务，实际加载在触发检测时进行）
            task.setTotalRecords(0); // 稍后更新
            taskMapper.updateById(task);
        }

        return task.getId();
    }

    @Override
    public ComplianceTask getTaskDetail(Long taskId, Long userId) {
        ComplianceTask task = taskMapper.selectById(taskId);
        if (task == null || !task.getUserId().equals(userId)) {
            throw new RuntimeException("任务不存在或无权访问");
        }
        return task;
    }

    @Override
    public PageResult<ComplianceTask> getUserTasks(Long userId, int current, int size) {
        Page<ComplianceTask> page = new Page<>(current, size);
        LambdaQueryWrapper<ComplianceTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ComplianceTask::getUserId, userId)
                .orderByDesc(ComplianceTask::getCreatedTime);

        Page<ComplianceTask> result = taskMapper.selectPage(page, wrapper);

        return new PageResult<>(
                result.getRecords(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean triggerCheck(Long taskId) {
        ComplianceTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        if (!"PENDING".equals(task.getTaskStatus())) {
            throw new RuntimeException("任务状态不允许触发检测");
        }

        // 更新任务状态
        task.setTaskStatus("PROCESSING");
        task.setStartedTime(LocalDateTime.now());
        taskMapper.updateById(task);

        try {
            // 获取待检测的消息
            List<ChatMessage> messages;
            if ("LOG".equals(task.getTaskType())) {
                LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(ChatMessage::getUserId, task.getUserId())
                        .orderByDesc(ChatMessage::getCreatedTime)
                        .last("LIMIT 1000");
                messages = messageMapper.selectList(wrapper);
            } else {
                throw new RuntimeException("暂不支持该任务类型");
            }

            // 批量调用Python服务进行检测
            int passCount = 0;
            int failCount = 0;

            for (ChatMessage message : messages) {
                try {
                    String checkResult = callPythonComplianceService(message.getContent());
                    
                    // 保存检测结果
                    ComplianceResult result = new ComplianceResult();
                    result.setTaskId(taskId);
                    result.setMessageId(message.getId());
                    result.setContent(message.getContent());
                    result.setDetailResult(checkResult);
                    
                    // 解析结果
                    JSONObject resultJson = JSONUtil.parseObj(checkResult);
                    String status = resultJson.getStr("result", "PASS");
                    result.setCheckResult(status);
                    result.setRiskLevel(resultJson.getStr("risk_level", "LOW"));
                    result.setRiskCategories(resultJson.getStr("risk_categories", ""));
                    result.setConfidenceScore(resultJson.getBigDecimal("confidence_score"));
                    
                    resultMapper.insert(result);

                    // 更新消息的合规状态
                    message.setComplianceStatus(status);
                    message.setComplianceResult(checkResult);
                    messageMapper.updateById(message);

                    if ("PASS".equals(status)) {
                        passCount++;
                    } else {
                        failCount++;
                    }

                } catch (Exception e) {
                    failCount++;
                    // 记录错误但继续处理其他消息
                    System.err.println("检测消息失败: " + e.getMessage());
                }

                // 更新进度
                task.setCheckedRecords(task.getCheckedRecords() + 1);
                taskMapper.updateById(task);
            }

            // 完成任务
            task.setTaskStatus("COMPLETED");
            task.setPassCount(passCount);
            task.setFailCount(failCount);
            task.setCompletedTime(LocalDateTime.now());
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("total", messages.size());
            summary.put("pass", passCount);
            summary.put("fail", failCount);
            task.setResultSummary(JSONUtil.toJsonStr(summary));
            
            taskMapper.updateById(task);

            return true;

        } catch (Exception e) {
            // 任务失败
            task.setTaskStatus("FAILED");
            task.setCompletedTime(LocalDateTime.now());
            taskMapper.updateById(task);
            throw new RuntimeException("检测任务失败: " + e.getMessage());
        }
    }

    @Override
    public String checkSingleMessage(String content) {
        try {
            return callPythonComplianceService(content);
        } catch (IOException e) {
            throw new RuntimeException("调用合规检测服务失败: " + e.getMessage());
        }
    }

    /**
     * 调用Python合规检测服务
     * 
     * Python服务接口规范：
     * POST /api/compliance/check
     * Request Body: {
     *   "content": "待检测的文本内容"
     * }
     * Response: {
     *   "result": "PASS" | "FAIL",
     *   "risk_level": "LOW" | "MEDIUM" | "HIGH",
     *   "risk_categories": "category1,category2",
     *   "confidence_score": 0.95,
     *   "detail": "详细说明"
     * }
     */
    private String callPythonComplianceService(String content) throws IOException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("content", content);

        Request request = new Request.Builder()
                .url(complianceServiceUrl)
                .post(RequestBody.create(JSONUtil.toJsonStr(requestBody), JSON_MEDIA_TYPE))
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("合规检测服务返回错误: " + response.code());
            }
            return response.body().string();
        }
    }

    /**
     * 从日志加载消息
     */
    private List<ChatMessage> loadMessagesFromLog(ComplianceCheckDTO checkDTO) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();

        // 时间范围
        if (checkDTO.getStartTime() != null && !checkDTO.getStartTime().isEmpty()) {
            LocalDateTime startTime = LocalDateTime.parse(checkDTO.getStartTime(), 
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            wrapper.ge(ChatMessage::getCreatedTime, startTime);
        }
        if (checkDTO.getEndTime() != null && !checkDTO.getEndTime().isEmpty()) {
            LocalDateTime endTime = LocalDateTime.parse(checkDTO.getEndTime(), 
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            wrapper.le(ChatMessage::getCreatedTime, endTime);
        }

        // 用户ID
        if (checkDTO.getUserIds() != null && !checkDTO.getUserIds().isEmpty()) {
            String[] userIdArray = checkDTO.getUserIds().split(",");
            List<Long> userIds = new ArrayList<>();
            for (String userId : userIdArray) {
                userIds.add(Long.parseLong(userId.trim()));
            }
            wrapper.in(ChatMessage::getUserId, userIds);
        }

        wrapper.orderByDesc(ChatMessage::getCreatedTime);
        wrapper.last("LIMIT 1000");

        return messageMapper.selectList(wrapper);
    }
    
    @Override
    public BatchComplianceResult batchCheckFromFile(MultipartFile file, Long userId) {
        try {
            String filename = file.getOriginalFilename();
            log.info("开始批量检测，文件名：{}", filename);
            
            List<ChatMessage> messages;
            
            // 根据文件类型解析
            if (filename.endsWith(".json")) {
                messages = parseJsonFile(file);
            } else if (filename.endsWith(".csv")) {
                messages = parseCsvFile(file);
            } else {
                throw new RuntimeException("不支持的文件格式，请上传JSON或CSV文件");
            }
            
            if (messages.isEmpty()) {
                throw new RuntimeException("文件中没有有效的消息数据");
            }
            
            log.info("解析到 {} 条消息，开始批量检测", messages.size());
            
            // 批量检测
            return performBatchCheck(messages);
            
        } catch (Exception e) {
            log.error("批量检测失败", e);
            throw new RuntimeException("批量检测失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 解析JSON文件
     */
    private List<ChatMessage> parseJsonFile(MultipartFile file) throws IOException {
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        JSONArray jsonArray = JSONUtil.parseArray(content);
        
        List<ChatMessage> messages = new ArrayList<>();
        for (Object obj : jsonArray) {
            JSONObject json = (JSONObject) obj;
            ChatMessage message = new ChatMessage();
            message.setId(json.getLong("id"));
            message.setSessionId(json.getLong("sessionId"));
            message.setUserId(json.getLong("userId"));
            message.setRole(json.getStr("role"));
            message.setContent(json.getStr("content"));
            messages.add(message);
        }
        
        return messages;
    }
    
    /**
     * 解析CSV文件
     */
    private List<ChatMessage> parseCsvFile(MultipartFile file) throws IOException {
        List<ChatMessage> messages = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            // 跳过BOM和标题行
            String line = reader.readLine();
            if (line != null && line.startsWith("\uFEFF")) {
                line = line.substring(1);
            }
            
            // 读取数据行
            while ((line = reader.readLine()) != null) {
                String[] fields = parseCsvLine(line);
                if (fields.length >= 5) {
                    ChatMessage message = new ChatMessage();
                    message.setId(Long.parseLong(fields[0]));
                    message.setSessionId(Long.parseLong(fields[1]));
                    message.setUserId(Long.parseLong(fields[2]));
                    message.setRole(fields[3]);
                    message.setContent(fields[4]);
                    messages.add(message);
                }
            }
        }
        
        return messages;
    }
    
    /**
     * 解析CSV行（处理引号内的逗号）
     */
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(field.toString());
                field = new StringBuilder();
            } else {
                field.append(c);
            }
        }
        fields.add(field.toString());
        
        return fields.toArray(new String[0]);
    }
    
    /**
     * 执行批量检测
     */
    private BatchComplianceResult performBatchCheck(List<ChatMessage> messages) {
        List<BatchComplianceResult.ComplianceItem> items = new ArrayList<>();
        int passedCount = 0;
        int failedCount = 0;
        int uncheckedCount = 0;
        
        // 按会话分组，配对用户消息和AI响应
        Map<Long, List<ChatMessage>> sessionMessages = new HashMap<>();
        for (ChatMessage message : messages) {
            sessionMessages.computeIfAbsent(message.getSessionId(), k -> new ArrayList<>()).add(message);
        }
        
        int index = 1;
        for (Map.Entry<Long, List<ChatMessage>> entry : sessionMessages.entrySet()) {
            List<ChatMessage> sessionMsgs = entry.getValue();
            
            // 按时间排序
            sessionMsgs.sort(Comparator.comparing(ChatMessage::getId));
            
            // 配对用户消息和AI响应
            for (int i = 0; i < sessionMsgs.size(); i++) {
                ChatMessage msg = sessionMsgs.get(i);
                
                if ("user".equals(msg.getRole())) {
                    BatchComplianceResult.ComplianceItem item = new BatchComplianceResult.ComplianceItem();
                    item.setIndex(index++);
                    item.setSessionId(msg.getSessionId());
                    item.setMessageId(msg.getId());
                    item.setUserContent(msg.getContent());
                    item.setTimestamp(System.currentTimeMillis());
                    
                    // 检测用户消息
                    JSONObject userResult = complianceClient.checkContent(msg.getContent());
                    if (userResult != null) {
                        item.setUserResult(userResult.getStr("result"));
                        item.setUserRiskLevel(userResult.getStr("risk_level"));
                        item.setUserRiskCategories(userResult.getStr("risk_categories"));
                    } else {
                        item.setUserResult("UNCHECKED");
                        item.setUserRiskLevel("UNKNOWN");
                        uncheckedCount++;
                    }
                    
                    // 查找对应的AI响应
                    if (i + 1 < sessionMsgs.size() && "assistant".equals(sessionMsgs.get(i + 1).getRole())) {
                        ChatMessage assistantMsg = sessionMsgs.get(i + 1);
                        item.setAssistantContent(assistantMsg.getContent());
                        
                        // 检测AI响应
                        JSONObject assistantResult = complianceClient.checkContent(assistantMsg.getContent());
                        if (assistantResult != null) {
                            item.setAssistantResult(assistantResult.getStr("result"));
                            item.setAssistantRiskLevel(assistantResult.getStr("risk_level"));
                            item.setAssistantRiskCategories(assistantResult.getStr("risk_categories"));
                        } else {
                            item.setAssistantResult("UNCHECKED");
                            item.setAssistantRiskLevel("UNKNOWN");
                            uncheckedCount++;
                        }
                        
                        i++; // 跳过已处理的assistant消息
                    }
                    
                    // 统计结果
                    boolean hasFail = "FAIL".equals(item.getUserResult()) || "FAIL".equals(item.getAssistantResult());
                    boolean hasPass = "PASS".equals(item.getUserResult()) || "PASS".equals(item.getAssistantResult());
                    
                    if (hasFail) {
                        failedCount++;
                    } else if (hasPass) {
                        passedCount++;
                    }
                    
                    items.add(item);
                }
            }
        }
        
        return BatchComplianceResult.builder()
                .items(items)
                .total(items.size())
                .passedCount(passedCount)
                .failedCount(failedCount)
                .uncheckedCount(uncheckedCount)
                .build();
    }
}
