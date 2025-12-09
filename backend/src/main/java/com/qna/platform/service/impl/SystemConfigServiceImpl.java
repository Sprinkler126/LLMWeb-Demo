package com.qna.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qna.platform.entity.SysConfig;
import com.qna.platform.mapper.SysConfigMapper;
import com.qna.platform.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置服务实现类
 *
 * @author QnA Platform
 */
@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {
    
    private final SysConfigMapper configMapper;
    private final RestTemplate restTemplate;
    
    @Override
    public List<SysConfig> getAllConfigs() {
        return configMapper.selectList(null);
    }
    
    @Override
    public SysConfig getConfigByKey(String configKey) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, configKey);
        return configMapper.selectOne(wrapper);
    }
    
    @Override
    public String getConfigValue(String configKey, String defaultValue) {
        SysConfig config = getConfigByKey(configKey);
        return config != null ? config.getConfigValue() : defaultValue;
    }
    
    @Override
    @Transactional
    public void updateConfig(SysConfig config, Long userId) {
        if (config.getId() == null) {
            throw new RuntimeException("配置ID不能为空");
        }
        
        SysConfig existingConfig = configMapper.selectById(config.getId());
        if (existingConfig == null) {
            throw new RuntimeException("配置不存在");
        }
        
        // 更新配置值和描述
        existingConfig.setConfigValue(config.getConfigValue());
        if (config.getConfigDesc() != null) {
            existingConfig.setConfigDesc(config.getConfigDesc());
        }
        existingConfig.setUpdatedBy(userId);
        
        configMapper.updateById(existingConfig);
    }
    
    @Override
    @Transactional
    public void batchUpdateConfigs(List<SysConfig> configs, Long userId) {
        for (SysConfig config : configs) {
            updateConfig(config, userId);
        }
    }
    
    @Override
    public Object testPythonConnection() {
        String endpoint = getConfigValue("python.compliance.endpoint", "http://localhost:5000/api/compliance/check");
        
        Map<String, Object> result = new HashMap<>();
        result.put("endpoint", endpoint);
        
        try {
            // 发送测试请求
            Map<String, Object> testData = new HashMap<>();
            testData.put("content", "This is a test message for compliance check");  // Python服务期望content字段
            
            // 设置超时时间
            String timeoutStr = getConfigValue("python.compliance.timeout", "30000");
            long timeout = Long.parseLong(timeoutStr);
            
            // 调用Python接口
            long startTime = System.currentTimeMillis();
            Map<String, Object> response = restTemplate.postForObject(endpoint, testData, Map.class);
            long endTime = System.currentTimeMillis();
            
            result.put("success", true);
            result.put("responseTime", endTime - startTime);
            result.put("response", response);
            result.put("message", "连接成功");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "连接失败: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }
        
        return result;
    }
}
