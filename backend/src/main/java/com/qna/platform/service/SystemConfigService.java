package com.qna.platform.service;

import com.qna.platform.entity.SysConfig;

import java.util.List;

/**
 * 系统配置服务接口
 *
 * @author QnA Platform
 */
public interface SystemConfigService {
    
    /**
     * 获取所有配置
     *
     * @return 配置列表
     */
    List<SysConfig> getAllConfigs();
    
    /**
     * 根据配置键获取配置
     *
     * @param configKey 配置键
     * @return 配置
     */
    SysConfig getConfigByKey(String configKey);
    
    /**
     * 根据配置键获取配置值
     *
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    String getConfigValue(String configKey, String defaultValue);
    
    /**
     * 更新配置
     *
     * @param config 配置
     * @param userId 更新人ID
     */
    void updateConfig(SysConfig config, Long userId);
    
    /**
     * 批量更新配置
     *
     * @param configs 配置列表
     * @param userId 更新人ID
     */
    void batchUpdateConfigs(List<SysConfig> configs, Long userId);
    
    /**
     * 测试Python检测接口连接
     *
     * @return 测试结果
     */
    Object testPythonConnection();
    
    /**
     * 根据服务分组获取配置列表
     *
     * @param serviceGroup 服务分组
     * @return 配置列表
     */
    List<SysConfig> getConfigsByServiceGroup(String serviceGroup);
    
    /**
     * 获取所有服务分组
     *
     * @return 服务分组列表
     */
    List<String> getAllServiceGroups();
}
