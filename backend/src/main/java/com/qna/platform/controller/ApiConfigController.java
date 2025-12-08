package com.qna.platform.controller;

import com.qna.platform.annotation.RequirePermission;
import com.qna.platform.common.PageResult;
import com.qna.platform.common.Result;
import com.qna.platform.entity.ApiConfig;
import com.qna.platform.service.ApiConfigService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API配置控制器
 * 需要API管理权限
 *
 * @author QnA Platform
 */
@RestController
@RequestMapping("/api-config")
@RequirePermission("API_MANAGE")
public class ApiConfigController {

    private final ApiConfigService apiConfigService;

    public ApiConfigController(ApiConfigService apiConfigService) {
        this.apiConfigService = apiConfigService;
    }

    /**
     * 创建API配置
     */
    @PostMapping
    public Result<String> createConfig(@RequestBody ApiConfig apiConfig, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            apiConfigService.createConfig(apiConfig, userId);
            return Result.success("创建成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新API配置
     */
    @PutMapping("/{id}")
    public Result<String> updateConfig(
            @PathVariable Long id,
            @RequestBody ApiConfig apiConfig,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            apiConfig.setId(id);
            apiConfigService.updateConfig(apiConfig, userId);
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除API配置
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteConfig(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            apiConfigService.deleteConfig(id, userId);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取API配置详情
     */
    @GetMapping("/{id}")
    public Result<ApiConfig> getConfig(@PathVariable Long id) {
        try {
            ApiConfig config = apiConfigService.getConfigById(id);
            return Result.success(config);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取所有启用的API配置（所有用户可访问）
     */
    @GetMapping("/enabled")
    @RequirePermission("API_USE")
    public Result<List<ApiConfig>> getEnabledConfigs() {
        try {
            List<ApiConfig> configs = apiConfigService.getEnabledConfigs();
            return Result.success(configs);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 分页查询API配置
     */
    @GetMapping("/list")
    public Result<PageResult<ApiConfig>> getConfigList(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) Integer status) {
        try {
            PageResult<ApiConfig> result = apiConfigService.getConfigList(current, size, provider, status);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 测试API配置
     */
    @PostMapping("/{id}/test")
    public Result<String> testConfig(@PathVariable Long id) {
        try {
            apiConfigService.testConfig(id);
            return Result.success("测试成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
