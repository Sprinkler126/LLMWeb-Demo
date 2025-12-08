package com.qna.platform.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qna.platform.common.PageResult;
import com.qna.platform.dto.ComplianceCheckDTO;
import com.qna.platform.entity.*;
import com.qna.platform.mapper.*;
import com.qna.platform.service.ComplianceService;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 合规检测服务实现
 * 预留接口给Python服务
 *
 * @author QnA Platform
 */
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

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    public ComplianceServiceImpl(ComplianceTaskMapper taskMapper,
                                ComplianceResultMapper resultMapper,
                                ChatMessageMapper messageMapper,
                                SysUserMapper userMapper) {
        this.taskMapper = taskMapper;
        this.resultMapper = resultMapper;
        this.messageMapper = messageMapper;
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
}
