package com.qna.platform.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qna.platform.common.PageResult;
import com.qna.platform.dto.CreateLlmComplianceTaskDTO;
import com.qna.platform.entity.LlmComplianceTask;
import com.qna.platform.entity.LlmComplianceResult;
import com.qna.platform.entity.LlmModelConfig;
import com.qna.platform.mapper.LlmComplianceTaskMapper;
import com.qna.platform.mapper.LlmComplianceResultMapper;
import com.qna.platform.mapper.LlmModelConfigMapper;
import com.qna.platform.service.LlmComplianceService;
import com.qna.platform.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * LLM合规检测服务实现
 *
 * @author QnA Platform
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmComplianceServiceImpl implements LlmComplianceService {

    private final LlmComplianceTaskMapper taskMapper;
    private final LlmComplianceResultMapper resultMapper;
    private final LlmModelConfigMapper modelConfigMapper;
    private final SystemConfigService configService;
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(30))
            .readTimeout(Duration.ofSeconds(60))
            .build();

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    @Override
    public List<LlmModelConfig> getAvailableModels() {
        LambdaQueryWrapper<LlmModelConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LlmModelConfig::getIsEnabled, 1)
                .orderByAsc(LlmModelConfig::getDisplayOrder);
        return modelConfigMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public Long createTask(CreateLlmComplianceTaskDTO dto, Long userId) {
        // 解析问题集JSON
        JSONObject questionSet;
        try {
            questionSet = JSONUtil.parseObj(dto.getQuestionSetJson());
        } catch (Exception e) {
            throw new RuntimeException("问题集JSON格式错误：" + e.getMessage());
        }

        // 统计总问题数
        int totalQuestions = countTotalQuestions(questionSet);

        // 获取模型信息
        LambdaQueryWrapper<LlmModelConfig> modelWrapper = new LambdaQueryWrapper<>();
        modelWrapper.eq(LlmModelConfig::getModelName, dto.getModelName());
        LlmModelConfig modelConfig = modelConfigMapper.selectOne(modelWrapper);
        if (modelConfig == null) {
            throw new RuntimeException("模型配置不存在");
        }

        // 创建任务
        LlmComplianceTask task = new LlmComplianceTask();
        task.setTaskName(dto.getTaskName());
        task.setModelName(dto.getModelName());
        task.setModelProvider(modelConfig.getModelProvider());
        task.setQuestionSetJson(dto.getQuestionSetJson());
        task.setTotalQuestions(totalQuestions);
        task.setCompletedQuestions(0);
        task.setPassedCount(0);
        task.setFailedCount(0);
        task.setErrorCount(0);
        task.setTaskStatus("PENDING");
        task.setCreatedBy(userId);

        taskMapper.insert(task);
        return task.getId();
    }

    @Override
    @Async
    @Transactional
    public void startTask(Long taskId) {
        LlmComplianceTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        if (!"PENDING".equals(task.getTaskStatus())) {
            throw new RuntimeException("任务状态不允许启动");
        }

        // 更新任务状态为运行中
        task.setTaskStatus("RUNNING");
        task.setStartTime(LocalDateTime.now());
        taskMapper.updateById(task);

        log.info("开始执行LLM合规检测任务：taskId={}, modelName={}", taskId, task.getModelName());

        try {
            // 解析问题集
            JSONObject questionSet = JSONUtil.parseObj(task.getQuestionSetJson());
            JSONArray categories = questionSet.getJSONArray("categories");

            int passedCount = 0;
            int failedCount = 0;
            int errorCount = 0;
            int completedCount = 0;

            // 遍历每个分类
            for (int i = 0; i < categories.size(); i++) {
                JSONObject category = categories.getJSONObject(i);
                String categoryName = category.getStr("category");
                JSONArray questions = category.getJSONArray("questions");

                // 遍历每个问题
                for (int j = 0; j < questions.size(); j++) {
                    JSONObject question = questions.getJSONObject(j);

                    try {
                        // 处理单个问题
                        boolean passed = processQuestion(task, categoryName, question);
                        if (passed) {
                            passedCount++;
                        } else {
                            failedCount++;
                        }
                    } catch (Exception e) {
                        log.error("处理问题失败：taskId={}, questionId={}, error={}", 
                                taskId, question.getStr("id"), e.getMessage());
                        errorCount++;
                    }

                    // 更新进度
                    completedCount++;
                    task.setCompletedQuestions(completedCount);
                    task.setPassedCount(passedCount);
                    task.setFailedCount(failedCount);
                    task.setErrorCount(errorCount);
                    taskMapper.updateById(task);

                    // 避免频繁调用API，稍微等待
                    Thread.sleep(500);
                }
            }

            // 任务完成
            task.setTaskStatus("COMPLETED");
            task.setEndTime(LocalDateTime.now());
            task.setDurationSeconds((int) Duration.between(task.getStartTime(), task.getEndTime()).getSeconds());
            
            // 生成结果摘要
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalQuestions", task.getTotalQuestions());
            summary.put("passedCount", passedCount);
            summary.put("failedCount", failedCount);
            summary.put("errorCount", errorCount);
            summary.put("passRate", task.getTotalQuestions() > 0 ? 
                    (double) passedCount / task.getTotalQuestions() * 100 : 0);
            task.setResultSummary(JSONUtil.toJsonStr(summary));

            taskMapper.updateById(task);
            log.info("LLM合规检测任务完成：taskId={}", taskId);

        } catch (Exception e) {
            log.error("LLM合规检测任务失败：taskId={}, error={}", taskId, e.getMessage(), e);
            task.setTaskStatus("FAILED");
            task.setEndTime(LocalDateTime.now());
            task.setErrorMessage(e.getMessage());
            taskMapper.updateById(task);
        }
    }

    /**
     * 处理单个问题
     */
    private boolean processQuestion(LlmComplianceTask task, String category, JSONObject question) 
            throws IOException, InterruptedException {
        String questionId = question.getStr("id");
        String questionText = question.getStr("question");
        String expectedBehavior = question.getStr("expected_behavior");

        log.info("处理问题：taskId={}, questionId={}, question={}", task.getId(), questionId, questionText);

        // 记录开始时间
        long startTime = System.currentTimeMillis();

        // 调用LLM模型获取回答
        String llmResponse = callLlmModel(task.getModelName(), questionText);

        // 记录响应时间
        int responseTime = (int) (System.currentTimeMillis() - startTime);

        // 调用Python合规检测服务
        String complianceCheckResult = callComplianceService(llmResponse);
        JSONObject complianceResult = JSONUtil.parseObj(complianceCheckResult);

        // 判断是否通过
        String result = complianceResult.getStr("result", "FAIL");
        boolean passed = "PASS".equals(result);

        // 保存结果
        LlmComplianceResult resultEntity = new LlmComplianceResult();
        resultEntity.setTaskId(task.getId());
        resultEntity.setQuestionId(questionId);
        resultEntity.setCategory(category);
        resultEntity.setQuestionText(questionText);
        resultEntity.setExpectedBehavior(expectedBehavior);
        resultEntity.setLlmResponse(llmResponse);
        resultEntity.setComplianceStatus(passed ? "PASS" : "FAIL");
        resultEntity.setComplianceResult(complianceCheckResult);
        resultEntity.setRiskLevel(complianceResult.getStr("risk_level", "LOW"));
        resultEntity.setRiskCategories(complianceResult.getStr("risk_categories", ""));
        resultEntity.setConfidenceScore(complianceResult.getBigDecimal("confidence_score", BigDecimal.ZERO));
        resultEntity.setResponseTime(responseTime);

        resultMapper.insert(resultEntity);

        return passed;
    }

    /**
     * 调用LLM模型
     */
    private String callLlmModel(String modelName, String question) throws IOException {
        // 从配置中获取OpenAI API密钥
        String apiKey = configService.getConfigValue("openai.api.key", "");
        if (apiKey.isEmpty()) {
            throw new RuntimeException("OpenAI API密钥未配置");
        }

        // 构建请求
        JSONObject requestBody = new JSONObject();
        requestBody.set("model", modelName);
        
        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.set("role", "user");
        message.set("content", question);
        messages.add(message);
        requestBody.set("messages", messages);

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .post(RequestBody.create(requestBody.toString(), JSON_MEDIA_TYPE))
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("LLM调用失败: " + response.code() + " - " + response.body().string());
            }

            String responseBody = response.body().string();
            JSONObject responseJson = JSONUtil.parseObj(responseBody);
            
            // 提取回答
            JSONArray choices = responseJson.getJSONArray("choices");
            if (choices != null && choices.size() > 0) {
                JSONObject choice = choices.getJSONObject(0);
                JSONObject messageObj = choice.getJSONObject("message");
                return messageObj.getStr("content");
            }

            throw new IOException("LLM返回格式错误");
        }
    }

    /**
     * 调用Python合规检测服务
     */
    private String callComplianceService(String content) throws IOException {
        String endpoint = configService.getConfigValue("python.compliance.endpoint", 
                "http://localhost:5000/api/compliance/check");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("content", content);

        Request request = new Request.Builder()
                .url(endpoint)
                .post(RequestBody.create(JSONUtil.toJsonStr(requestBody), JSON_MEDIA_TYPE))
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("合规检测服务调用失败: " + response.code());
            }
            return response.body().string();
        }
    }

    /**
     * 统计总问题数
     */
    private int countTotalQuestions(JSONObject questionSet) {
        int total = 0;
        JSONArray categories = questionSet.getJSONArray("categories");
        if (categories != null) {
            for (int i = 0; i < categories.size(); i++) {
                JSONObject category = categories.getJSONObject(i);
                JSONArray questions = category.getJSONArray("questions");
                if (questions != null) {
                    total += questions.size();
                }
            }
        }
        return total;
    }

    @Override
    public LlmComplianceTask getTask(Long taskId) {
        return taskMapper.selectById(taskId);
    }

    @Override
    public List<LlmComplianceResult> getTaskResults(Long taskId) {
        LambdaQueryWrapper<LlmComplianceResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LlmComplianceResult::getTaskId, taskId)
                .orderByAsc(LlmComplianceResult::getId);
        return resultMapper.selectList(wrapper);
    }

    @Override
    public PageResult<LlmComplianceTask> getUserTasks(Long userId, int current, int size) {
        Page<LlmComplianceTask> page = new Page<>(current, size);
        LambdaQueryWrapper<LlmComplianceTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LlmComplianceTask::getCreatedBy, userId)
                .orderByDesc(LlmComplianceTask::getCreatedTime);

        Page<LlmComplianceTask> result = taskMapper.selectPage(page, wrapper);

        return new PageResult<>(
                result.getRecords(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize()
        );
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId, Long userId) {
        LlmComplianceTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        if (!task.getCreatedBy().equals(userId)) {
            throw new RuntimeException("无权删除该任务");
        }

        // 删除任务（级联删除结果）
        taskMapper.deleteById(taskId);
    }
}
