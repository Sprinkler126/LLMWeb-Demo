package com.qna.platform.service;

import com.qna.platform.common.PageResult;
import com.qna.platform.dto.CreateLlmComplianceTaskDTO;
import com.qna.platform.entity.ApiConfig;
import com.qna.platform.entity.LlmComplianceTask;
import com.qna.platform.entity.LlmComplianceResult;

import java.util.List;

/**
 * LLM合规检测服务接口
 *
 * @author QnA Platform
 */
public interface LlmComplianceService {

    /**
     * 获取可用的API配置列表（用于模型选择）
     *
     * @return API配置列表
     */
    List<ApiConfig> getAvailableApiConfigs();

    /**
     * 创建LLM合规检测任务
     *
     * @param dto 任务DTO
     * @param userId 用户ID
     * @return 任务ID
     */
    Long createTask(CreateLlmComplianceTaskDTO dto, Long userId);

    /**
     * 启动任务执行
     *
     * @param taskId 任务ID
     */
    void startTask(Long taskId);

    /**
     * 获取任务详情
     *
     * @param taskId 任务ID
     * @return 任务详情
     */
    LlmComplianceTask getTask(Long taskId);

    /**
     * 获取任务结果列表
     *
     * @param taskId 任务ID
     * @return 结果列表
     */
    List<LlmComplianceResult> getTaskResults(Long taskId);

    /**
     * 获取用户的任务列表
     *
     * @param userId 用户ID
     * @param current 当前页
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<LlmComplianceTask> getUserTasks(Long userId, int current, int size);

    /**
     * 删除任务
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     */
    void deleteTask(Long taskId, Long userId);
}
