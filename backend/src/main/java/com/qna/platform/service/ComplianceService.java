package com.qna.platform.service;

import com.qna.platform.common.PageResult;
import com.qna.platform.dto.BatchComplianceResult;
import com.qna.platform.dto.ComplianceCheckDTO;
import com.qna.platform.entity.ComplianceTask;
import org.springframework.web.multipart.MultipartFile;

/**
 * 合规检测服务接口
 *
 * @author QnA Platform
 */
public interface ComplianceService {

    /**
     * 创建合规检测任务
     *
     * @param checkDTO 检测参数
     * @param userId 用户ID
     * @return 任务ID
     */
    Long createComplianceTask(ComplianceCheckDTO checkDTO, Long userId);

    /**
     * 获取任务详情
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 任务详情
     */
    ComplianceTask getTaskDetail(Long taskId, Long userId);

    /**
     * 获取用户的任务列表
     *
     * @param userId 用户ID
     * @param current 当前页
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<ComplianceTask> getUserTasks(Long userId, int current, int size);

    /**
     * 手动触发检测（调用Python服务）
     *
     * @param taskId 任务ID
     * @return 是否成功
     */
    boolean triggerCheck(Long taskId);

    /**
     * 检查单条消息的合规性
     *
     * @param content 消息内容
     * @return 检测结果
     */
    String checkSingleMessage(String content);
    
    /**
     * 从文件批量检测合规性
     * 支持JSON和CSV格式的导出文件
     *
     * @param file 上传的文件
     * @param userId 用户ID
     * @return 批量检测结果
     */
    BatchComplianceResult batchCheckFromFile(MultipartFile file, Long userId);
}
