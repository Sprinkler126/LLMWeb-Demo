package com.qna.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qna.platform.entity.ChatMessage;
import com.qna.platform.entity.ChatSession;
import com.qna.platform.entity.SysUser;
import com.qna.platform.mapper.ChatMessageMapper;
import com.qna.platform.mapper.ChatSessionMapper;
import com.qna.platform.mapper.SysUserMapper;
import com.qna.platform.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 平台数据统计服务实现
 *
 * @author QnA Platform
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    
    private final SysUserMapper userMapper;
    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    
    @Override
    public Object getPlatformStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 1. 统计用户总数
        Long totalUsers = userMapper.selectCount(null);
        statistics.put("totalUsers", totalUsers);
        
        // 2. 统计会话总数
        Long totalSessions = sessionMapper.selectCount(null);
        statistics.put("totalSessions", totalSessions);
        
        // 3. 统计消息总数（对话总数）
        Long totalMessages = messageMapper.selectCount(null);
        statistics.put("totalMessages", totalMessages);
        
        // 4. 统计最近一天的对话数
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        LambdaQueryWrapper<ChatMessage> recentWrapper = new LambdaQueryWrapper<>();
        recentWrapper.ge(ChatMessage::getCreatedTime, oneDayAgo);
        Long recentDayMessages = messageMapper.selectCount(recentWrapper);
        statistics.put("recentDayMessages", recentDayMessages);
        
        // 5. 统计违规消息数和占比
        LambdaQueryWrapper<ChatMessage> violationWrapper = new LambdaQueryWrapper<>();
        violationWrapper.eq(ChatMessage::getComplianceStatus, "VIOLATION");
        Long violationMessages = messageMapper.selectCount(violationWrapper);
        statistics.put("violationMessages", violationMessages);
        
        // 计算违规占比
        double violationRate = totalMessages > 0 ? 
            (double) violationMessages / totalMessages * 100 : 0;
        statistics.put("violationRate", Math.round(violationRate * 100.0) / 100.0);
        
        // 6. 统计活跃用户数（最近一天有对话的用户数）
        // 这里需要使用原生SQL或者分组查询
        // 暂时使用简单的方式估算
        LambdaQueryWrapper<ChatSession> activeSessionWrapper = new LambdaQueryWrapper<>();
        activeSessionWrapper.ge(ChatSession::getUpdatedTime, oneDayAgo);
        Long activeSessions = sessionMapper.selectCount(activeSessionWrapper);
        statistics.put("activeSessions", activeSessions);
        
        // 7. 统计今日新增用户
        LambdaQueryWrapper<SysUser> newUserWrapper = new LambdaQueryWrapper<>();
        newUserWrapper.ge(SysUser::getCreatedAt, LocalDateTime.now().withHour(0).withMinute(0).withSecond(0));
        Long newUsersToday = userMapper.selectCount(newUserWrapper);
        statistics.put("newUsersToday", newUsersToday);
        
        return statistics;
    }
}
