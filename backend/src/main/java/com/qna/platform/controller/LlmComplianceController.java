package com.qna.platform.controller;

import com.qna.platform.annotation.RequirePermission;
import com.qna.platform.common.PageResult;
import com.qna.platform.common.Result;
import com.qna.platform.dto.CreateLlmComplianceTaskDTO;
import com.qna.platform.entity.ApiConfig;
import com.qna.platform.entity.LlmComplianceTask;
import com.qna.platform.entity.LlmComplianceResult;
import com.qna.platform.service.LlmComplianceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * LLM合规检测控制器
 * 需要合规检测权限
 *
 * @author QnA Platform
 */
@RestController
@RequestMapping("/llm-compliance")
@RequirePermission("COMPLIANCE_CHECK")
@RequiredArgsConstructor
public class LlmComplianceController {

    private final LlmComplianceService llmComplianceService;

    /**
     * 获取可用的API配置列表（用于模型选择）
     */
    @GetMapping("/api-configs")
    public Result<List<ApiConfig>> getAvailableApiConfigs() {
        List<ApiConfig> configs = llmComplianceService.getAvailableApiConfigs();
        return Result.success(configs);
    }

    /**
     * 创建LLM合规检测任务
     */
    @PostMapping("/task")
    public Result<Long> createTask(
            @Validated @RequestBody CreateLlmComplianceTaskDTO dto,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Long taskId = llmComplianceService.createTask(dto, userId);
            return Result.success("任务创建成功", taskId);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 启动任务执行
     */
    @PostMapping("/task/{taskId}/start")
    public Result<String> startTask(@PathVariable Long taskId) {
        try {
            llmComplianceService.startTask(taskId);
            Result<String> result = Result.success();
            result.setMessage("任务已启动");
            return result;
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/task/{taskId}")
    public Result<LlmComplianceTask> getTask(@PathVariable Long taskId) {
        try {
            LlmComplianceTask task = llmComplianceService.getTask(taskId);
            return Result.success(task);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取任务结果列表
     */
    @GetMapping("/task/{taskId}/results")
    public Result<List<LlmComplianceResult>> getTaskResults(@PathVariable Long taskId) {
        try {
            List<LlmComplianceResult> results = llmComplianceService.getTaskResults(taskId);
            return Result.success(results);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户的任务列表（分页）
     */
    @GetMapping("/tasks")
    public Result<PageResult<LlmComplianceTask>> getUserTasks(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            PageResult<LlmComplianceTask> result = llmComplianceService.getUserTasks(userId, current, size);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/task/{taskId}")
    public Result<String> deleteTask(@PathVariable Long taskId, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            llmComplianceService.deleteTask(taskId, userId);
            Result<String> result = Result.success();
            result.setMessage("任务删除成功");
            return result;
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
