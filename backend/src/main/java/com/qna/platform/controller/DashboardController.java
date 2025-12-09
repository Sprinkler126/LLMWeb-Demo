package com.qna.platform.controller;

import com.qna.platform.annotation.RequireRole;
import com.qna.platform.common.Result;
import com.qna.platform.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 平台数据统计控制器
 * 需要管理员权限
 *
 * @author QnA Platform
 */
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@RequireRole({"ADMIN", "SUPER_ADMIN"})
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    /**
     * 获取平台统计数据
     */
    @GetMapping("/statistics")
    public Result<?> getStatistics() {
        Object statistics = dashboardService.getPlatformStatistics();
        return Result.success(statistics);
    }
}
