package com.qna.platform.service;

import com.qna.platform.common.PageResult;
import com.qna.platform.entity.ApiConfig;

import java.util.List;

/**
 * API配置服务接口
 *
 * @author QnA Platform
 */
public interface ApiConfigService {

    /**
     * 创建API配置
     */
    boolean createConfig(ApiConfig apiConfig, Long userId);

    /**
     * 更新API配置
     */
    boolean updateConfig(ApiConfig apiConfig, Long userId);

    /**
     * 删除API配置
     */
    boolean deleteConfig(Long id, Long userId);

    /**
     * 获取API配置详情
     */
    ApiConfig getConfigById(Long id);

    /**
     * 获取所有启用的API配置
     */
    List<ApiConfig> getEnabledConfigs();

    /**
     * 分页查询API配置
     */
    PageResult<ApiConfig> getConfigList(int current, int size, String provider, Integer status);

    /**
     * 测试API配置
     */
    boolean testConfig(Long id);
}
