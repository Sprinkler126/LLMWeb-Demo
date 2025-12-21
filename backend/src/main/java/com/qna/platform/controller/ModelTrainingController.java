package com.qna.platform.controller;

import com.qna.platform.annotation.RequireRole;
import com.qna.platform.common.Result;
import com.qna.platform.dto.CreateTrainingTaskDTO;
import com.qna.platform.dto.TrainingProgressDTO;
import com.qna.platform.entity.ModelTrainingTask;
import com.qna.platform.service.ModelTrainingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模型训练控制器
 *
 * @author QnA Platform
 */
@Slf4j
@RestController
@RequestMapping("/training")
@RequiredArgsConstructor
public class ModelTrainingController {
    
    private final ModelTrainingService trainingService;
    
    /**
     * 创建训练任务（管理员和超管）
     */
    @PostMapping("/task")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    public Result<Map<String, Object>> createTask(
            @RequestBody CreateTrainingTaskDTO dto,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Long taskId = trainingService.createTask(dto, userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("taskId", taskId);
            result.put("message", "训练任务创建成功");
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("创建训练任务失败", e);
            return Result.error("创建训练任务失败：" + e.getMessage());
        }
    }
    
    /**
     * 启动训练任务（管理员和超管）
     */
    @PostMapping("/task/{taskId}/start")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    public Result<String> startTask(
            @PathVariable Long taskId,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            trainingService.startTask(taskId, userId);
            return Result.success("训练任务已启动");
        } catch (Exception e) {
            log.error("启动训练任务失败，任务ID：{}", taskId, e);
            return Result.error("启动训练任务失败：" + e.getMessage());
        }
    }
    
    /**
     * 停止训练任务（管理员和超管）
     */
    @PostMapping("/task/{taskId}/stop")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    public Result<String> stopTask(
            @PathVariable Long taskId,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            trainingService.stopTask(taskId, userId);
            return Result.success("训练任务已停止");
        } catch (Exception e) {
            log.error("停止训练任务失败，任务ID：{}", taskId, e);
            return Result.error("停止训练任务失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取任务详情
     */
    @GetMapping("/task/{taskId}")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    public Result<ModelTrainingTask> getTaskDetail(@PathVariable Long taskId) {
        try {
            ModelTrainingTask task = trainingService.getTaskDetail(taskId);
            if (task == null) {
                return Result.error("任务不存在");
            }
            return Result.success(task);
        } catch (Exception e) {
            log.error("获取任务详情失败，任务ID：{}", taskId, e);
            return Result.error("获取任务详情失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取任务进度
     */
    @GetMapping("/task/{taskId}/progress")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    public Result<TrainingProgressDTO> getTaskProgress(@PathVariable Long taskId) {
        try {
            TrainingProgressDTO progress = trainingService.getTaskProgress(taskId);
            return Result.success(progress);
        } catch (Exception e) {
            log.error("获取任务进度失败，任务ID：{}", taskId, e);
            return Result.error("获取任务进度失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取当前用户的所有训练任务
     */
    @GetMapping("/tasks")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    public Result<List<ModelTrainingTask>> getUserTasks(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            List<ModelTrainingTask> tasks = trainingService.getUserTasks(userId);
            return Result.success(tasks);
        } catch (Exception e) {
            log.error("获取用户训练任务失败", e);
            return Result.error("获取训练任务失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取所有训练任务（超管）
     */
    @GetMapping("/tasks/all")
    @RequireRole("SUPER_ADMIN")
    public Result<List<ModelTrainingTask>> getAllTasks() {
        try {
            List<ModelTrainingTask> tasks = trainingService.getAllTasks();
            return Result.success(tasks);
        } catch (Exception e) {
            log.error("获取所有训练任务失败", e);
            return Result.error("获取训练任务失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除训练任务
     */
    @DeleteMapping("/task/{taskId}")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    public Result<String> deleteTask(
            @PathVariable Long taskId,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            trainingService.deleteTask(taskId, userId);
            return Result.success("训练任务已删除");
        } catch (Exception e) {
            log.error("删除训练任务失败，任务ID：{}", taskId, e);
            return Result.error("删除训练任务失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取训练日志
     */
    @GetMapping("/task/{taskId}/log")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    public Result<Map<String, String>> getTrainingLog(@PathVariable Long taskId) {
        try {
            String log = trainingService.getTrainingLog(taskId);
            Map<String, String> result = new HashMap<>();
            result.put("log", log != null ? log : "暂无日志");
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取训练日志失败，任务ID：{}", taskId, e);
            return Result.error("获取训练日志失败：" + e.getMessage());
        }
    }
    
    /**
     * 接收Python服务的进度更新（内部接口，无需鉴权）
     */
    @PostMapping("/progress")
    public Result<String> updateProgress(@RequestBody TrainingProgressDTO progressDTO) {
        try {
            trainingService.updateProgress(progressDTO);
            return Result.success("进度更新成功");
        } catch (Exception e) {
            log.error("更新训练进度失败", e);
            return Result.error("更新进度失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取支持的模型类型列表
     */
    @GetMapping("/model-types")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    public Result<List<Map<String, String>>> getModelTypes() {
        List<Map<String, String>> modelTypes = List.of(
            Map.of("value", "TEXT_CLASSIFIER", "label", "文本分类器"),
            Map.of("value", "SENTIMENT_ANALYZER", "label", "情感分析器"),
            Map.of("value", "TOPIC_MODEL", "label", "主题模型")
        );
        return Result.success(modelTypes);
    }
}
