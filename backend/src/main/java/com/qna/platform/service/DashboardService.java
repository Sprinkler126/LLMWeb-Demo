package com.qna.platform.service;

/**
 * 平台数据统计服务接口
 *
 * @author QnA Platform
 */
public interface DashboardService {
    
    /**
     * 获取平台统计数据
     * 包括：
     * - 用户总数
     * - 对话总数
     * - 最近一天对话数
     * - 违规占比
     *
     * @return 统计数据
     */
    Object getPlatformStatistics();
}
