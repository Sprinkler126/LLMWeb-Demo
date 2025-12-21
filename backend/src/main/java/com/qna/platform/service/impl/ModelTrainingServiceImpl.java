package com.qna.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qna.platform.dto.CreateTrainingTaskDTO;
import com.qna.platform.dto.TrainingProgressDTO;
import com.qna.platform.entity.ModelTrainingTask;
import com.qna.platform.mapper.ModelTrainingTaskMapper;
import com.qna.platform.service.ModelTrainingService;
import com.qna.platform.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模型训练服务实现
 *
 * @author QnA Platform
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelTrainingServiceImpl implements ModelTrainingService {
    
    private final ModelTrainingTaskMapper taskMapper;
    private final SystemConfigService systemConfigService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Override
    @Transactional
    public Long createTask(CreateTrainingTaskDTO dto, Long userId) {
        log.info("创建训练任务，用户ID：{}，任务名称：{}", userId, dto.getTaskName());
        
        // 创建任务实体
        ModelTrainingTask task = new ModelTrainingTask();
        task.setTaskName(dto.getTaskName());
        task.setModelType(dto.getModelType());
        task.setDatasetPath(dto.getDatasetPath());
        task.setTaskStatus("PENDING");
        task.setProgress(0);
        task.setCurrentEpoch(0);
        task.setTotalEpochs(dto.getEpochs() != null ? dto.getEpochs() : 10);
        task.setCreatedBy(userId);
        task.setCreatedTime(LocalDateTime.now());
        
        // 构建模型配置JSON
        Map<String, Object> config = new HashMap<>();
        config.put("epochs", dto.getEpochs() != null ? dto.getEpochs() : 10);
        config.put("batchSize", dto.getBatchSize() != null ? dto.getBatchSize() : 32);
        config.put("learningRate", dto.getLearningRate() != null ? dto.getLearningRate() : 0.001);
        
        try {
            task.setModelConfig(objectMapper.writeValueAsString(config));
        } catch (Exception e) {
            log.error("构建模型配置失败", e);
            task.setModelConfig("{}");
        }
        
        taskMapper.insert(task);
        log.info("训练任务创建成功，任务ID：{}", task.getId());
        
        return task.getId();
    }
    
    @Override
    @Async
    @Transactional
    public void startTask(Long taskId, Long userId) {
        log.info("启动训练任务，任务ID：{}，用户ID：{}", taskId, userId);
        
        ModelTrainingTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        
        if (!"PENDING".equals(task.getTaskStatus()) && !"FAILED".equals(task.getTaskStatus())) {
            throw new RuntimeException("任务状态不允许启动");
        }
        
        // 更新任务状态为运行中
        task.setTaskStatus("RUNNING");
        task.setStartTime(LocalDateTime.now());
        task.setProgress(0);
        task.setErrorMessage(null);
        taskMapper.updateById(task);
        
        try {
            // 调用Python服务启动训练
            String pythonServiceUrl = systemConfigService.getConfigValue(
                "python.service.url", "http://localhost:5000");
            String trainingUrl = pythonServiceUrl + "/api/training/start";
            
            // 准备请求参数
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("taskId", taskId);
            requestBody.put("modelType", task.getModelType());
            requestBody.put("datasetPath", task.getDatasetPath());
            requestBody.put("modelConfig", task.getModelConfig());
            requestBody.put("callbackUrl", getCallbackUrl());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            log.info("调用Python训练服务：{}", trainingUrl);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                trainingUrl, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Python训练服务启动成功，任务ID：{}", taskId);
            } else {
                throw new RuntimeException("Python服务返回错误：" + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("启动训练任务失败，任务ID：{}", taskId, e);
            
            // 更新任务状态为失败
            task.setTaskStatus("FAILED");
            task.setEndTime(LocalDateTime.now());
            task.setErrorMessage("启动训练失败：" + e.getMessage());
            taskMapper.updateById(task);
            
            throw new RuntimeException("启动训练失败", e);
        }
    }
    
    @Override
    @Transactional
    public void stopTask(Long taskId, Long userId) {
        log.info("停止训练任务，任务ID：{}，用户ID：{}", taskId, userId);
        
        ModelTrainingTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        
        if (!"RUNNING".equals(task.getTaskStatus())) {
            throw new RuntimeException("任务不在运行中，无法停止");
        }
        
        try {
            // 调用Python服务停止训练
            String pythonServiceUrl = systemConfigService.getConfigValue(
                "python.service.url", "http://localhost:5000");
            String stopUrl = pythonServiceUrl + "/api/training/stop/" + taskId;
            
            restTemplate.postForEntity(stopUrl, null, Map.class);
            
            // 更新任务状态
            task.setTaskStatus("STOPPED");
            task.setEndTime(LocalDateTime.now());
            
            if (task.getStartTime() != null) {
                Duration duration = Duration.between(task.getStartTime(), task.getEndTime());
                task.setDurationSeconds((int) duration.getSeconds());
            }
            
            taskMapper.updateById(task);
            log.info("训练任务已停止，任务ID：{}", taskId);
            
        } catch (Exception e) {
            log.error("停止训练任务失败，任务ID：{}", taskId, e);
            throw new RuntimeException("停止训练失败", e);
        }
    }
    
    @Override
    public ModelTrainingTask getTaskDetail(Long taskId) {
        return taskMapper.selectById(taskId);
    }
    
    @Override
    public TrainingProgressDTO getTaskProgress(Long taskId) {
        ModelTrainingTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        
        return TrainingProgressDTO.builder()
            .taskId(task.getId())
            .taskStatus(task.getTaskStatus())
            .progress(task.getProgress())
            .currentEpoch(task.getCurrentEpoch())
            .totalEpochs(task.getTotalEpochs())
            .trainLoss(task.getTrainLoss())
            .trainAccuracy(task.getTrainAccuracy())
            .valLoss(task.getValLoss())
            .valAccuracy(task.getValAccuracy())
            .latestLog(task.getTrainingLog())
            .errorMessage(task.getErrorMessage())
            .build();
    }
    
    @Override
    public List<ModelTrainingTask> getUserTasks(Long userId) {
        LambdaQueryWrapper<ModelTrainingTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelTrainingTask::getCreatedBy, userId)
            .orderByDesc(ModelTrainingTask::getCreatedTime);
        return taskMapper.selectList(wrapper);
    }
    
    @Override
    public List<ModelTrainingTask> getAllTasks() {
        LambdaQueryWrapper<ModelTrainingTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ModelTrainingTask::getCreatedTime);
        return taskMapper.selectList(wrapper);
    }
    
    @Override
    @Transactional
    public void deleteTask(Long taskId, Long userId) {
        ModelTrainingTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        
        if ("RUNNING".equals(task.getTaskStatus())) {
            throw new RuntimeException("任务正在运行中，无法删除");
        }
        
        taskMapper.deleteById(taskId);
        log.info("训练任务已删除，任务ID：{}", taskId);
    }
    
    @Override
    @Transactional
    public void updateProgress(TrainingProgressDTO progressDTO) {
        log.debug("更新训练进度，任务ID：{}", progressDTO.getTaskId());
        
        ModelTrainingTask task = taskMapper.selectById(progressDTO.getTaskId());
        if (task == null) {
            log.error("任务不存在，任务ID：{}", progressDTO.getTaskId());
            return;
        }
        
        // 更新任务信息
        task.setTaskStatus(progressDTO.getTaskStatus());
        task.setProgress(progressDTO.getProgress());
        task.setCurrentEpoch(progressDTO.getCurrentEpoch());
        task.setTrainLoss(progressDTO.getTrainLoss());
        task.setTrainAccuracy(progressDTO.getTrainAccuracy());
        task.setValLoss(progressDTO.getValLoss());
        task.setValAccuracy(progressDTO.getValAccuracy());
        task.setTrainingLog(progressDTO.getLatestLog());
        task.setErrorMessage(progressDTO.getErrorMessage());
        
        // 如果任务完成或失败，更新结束时间和时长
        if ("COMPLETED".equals(progressDTO.getTaskStatus()) || 
            "FAILED".equals(progressDTO.getTaskStatus())) {
            task.setEndTime(LocalDateTime.now());
            
            if (task.getStartTime() != null) {
                Duration duration = Duration.between(task.getStartTime(), task.getEndTime());
                task.setDurationSeconds((int) duration.getSeconds());
            }
        }
        
        taskMapper.updateById(task);
    }
    
    @Override
    public String getTrainingLog(Long taskId) {
        ModelTrainingTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        return task.getTrainingLog();
    }
    
    /**
     * 获取回调URL
     */
    private String getCallbackUrl() {
        // 从系统配置获取后端服务地址
        String backendUrl = systemConfigService.getConfigValue(
            "backend.service.url", "http://localhost:8080");
        return backendUrl + "/api/training/progress";
    }
}
