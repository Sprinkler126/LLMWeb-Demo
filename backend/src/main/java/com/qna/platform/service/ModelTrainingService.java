package com.qna.platform.service;

import com.qna.platform.dto.CreateTrainingTaskDTO;
import com.qna.platform.dto.TrainingProgressDTO;
import com.qna.platform.entity.ModelTrainingTask;

import java.util.List;

/**
 * 模型训练服务接口
 *
 * @author QnA Platform
 */
public interface ModelTrainingService {
    
    /**
     * 创建训练任务
     *
     * @param dto 创建训练任务DTO
     * @param userId 用户ID
     * @return 任务ID
     */
    Long createTask(CreateTrainingTaskDTO dto, Long userId);
    
    /**
     * 启动训练任务
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     */
    void startTask(Long taskId, Long userId);
    
    /**
     * 停止训练任务
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     */
    void stopTask(Long taskId, Long userId);
    
    /**
     * 获取任务详情
     *
     * @param taskId 任务ID
     * @return 任务详情
     */
    ModelTrainingTask getTaskDetail(Long taskId);
    
    /**
     * 获取任务进度
     *
     * @param taskId 任务ID
     * @return 训练进度
     */
    TrainingProgressDTO getTaskProgress(Long taskId);
    
    /**
     * 获取用户的所有训练任务
     *
     * @param userId 用户ID
     * @return 任务列表
     */
    List<ModelTrainingTask> getUserTasks(Long userId);
    
    /**
     * 获取所有训练任务（管理员）
     *
     * @return 任务列表
     */
    List<ModelTrainingTask> getAllTasks();
    
    /**
     * 删除训练任务
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     */
    void deleteTask(Long taskId, Long userId);
    
    /**
     * 更新训练进度（由Python服务调用）
     *
     * @param progressDTO 进度信息
     */
    void updateProgress(TrainingProgressDTO progressDTO);
    
    /**
     * 获取训练日志
     *
     * @param taskId 任务ID
     * @return 训练日志
     */
    String getTrainingLog(Long taskId);
}
