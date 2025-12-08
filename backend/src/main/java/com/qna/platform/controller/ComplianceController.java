package com.qna.platform.controller;

import com.qna.platform.annotation.RequirePermission;
import com.qna.platform.common.PageResult;
import com.qna.platform.common.Result;
import com.qna.platform.dto.ComplianceCheckDTO;
import com.qna.platform.entity.ComplianceTask;
import com.qna.platform.service.ComplianceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 合规检测控制器
 * 需要合规检测权限
 *
 * @author QnA Platform
 */
@RestController
@RequestMapping("/compliance")
@RequirePermission("COMPLIANCE_CHECK")
public class ComplianceController {

    private final ComplianceService complianceService;

    public ComplianceController(ComplianceService complianceService) {
        this.complianceService = complianceService;
    }

    /**
     * 创建合规检测任务
     */
    @PostMapping("/task")
    public Result<Long> createTask(
            @Validated @RequestBody ComplianceCheckDTO checkDTO,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Long taskId = complianceService.createComplianceTask(checkDTO, userId);
            return Result.success("任务创建成功", taskId);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/task/{taskId}")
    public Result<ComplianceTask> getTaskDetail(
            @PathVariable Long taskId,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            ComplianceTask task = complianceService.getTaskDetail(taskId, userId);
            return Result.success(task);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户的任务列表
     */
    @GetMapping("/tasks")
    public Result<PageResult<ComplianceTask>> getUserTasks(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            PageResult<ComplianceTask> result = complianceService.getUserTasks(userId, current, size);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 触发检测
     */
    @PostMapping("/task/{taskId}/trigger")
    public Result<String> triggerCheck(@PathVariable Long taskId) {
        try {
            complianceService.triggerCheck(taskId);
            return Result.success("检测任务已触发");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 检查单条消息
     */
    @PostMapping("/check-message")
    public Result<String> checkMessage(@RequestBody Map<String, String> params) {
        try {
            String content = params.get("content");
            String result = complianceService.checkSingleMessage(content);
            return Result.success("检测完成", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
