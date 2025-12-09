package com.qna.platform.controller;

import com.qna.platform.annotation.RequireRole;
import com.qna.platform.common.Result;
import com.qna.platform.entity.SysConfig;
import com.qna.platform.service.SystemConfigService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统配置控制器
 * 需要超级管理员权限
 *
 * @author QnA Platform
 */
@RestController
@RequestMapping("/admin/system-config")
@RequiredArgsConstructor
@RequireRole("SUPER_ADMIN")
public class SystemConfigController {
    
    private final SystemConfigService systemConfigService;
    
    /**
     * 获取所有配置
     */
    @GetMapping
    public Result<List<SysConfig>> getAllConfigs() {
        List<SysConfig> configs = systemConfigService.getAllConfigs();
        return Result.success(configs);
    }
    
    /**
     * 根据配置键获取配置
     */
    @GetMapping("/{configKey}")
    public Result<SysConfig> getConfigByKey(@PathVariable String configKey) {
        SysConfig config = systemConfigService.getConfigByKey(configKey);
        return Result.success(config);
    }
    
    /**
     * 更新配置
     */
    @PutMapping
    public Result<Void> updateConfig(@RequestBody SysConfig config, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        systemConfigService.updateConfig(config, userId);
        return Result.success("配置更新成功");
    }
    
    /**
     * 批量更新配置
     */
    @PutMapping("/batch")
    public Result<Void> batchUpdateConfigs(@RequestBody List<SysConfig> configs, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        systemConfigService.batchUpdateConfigs(configs, userId);
        return Result.success("配置批量更新成功");
    }
    
    /**
     * 测试Python检测接口连接
     */
    @PostMapping("/test-python-connection")
    public Result<Object> testPythonConnection() {
        Object result = systemConfigService.testPythonConnection();
        return Result.success(result);
    }
}
