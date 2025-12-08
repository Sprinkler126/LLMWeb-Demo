package com.qna.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qna.platform.common.PageResult;
import com.qna.platform.entity.ApiConfig;
import com.qna.platform.mapper.ApiConfigMapper;
import com.qna.platform.service.ApiConfigService;
import com.qna.platform.util.AiApiClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * API配置服务实现
 *
 * @author QnA Platform
 */
@Service
public class ApiConfigServiceImpl implements ApiConfigService {

    private final ApiConfigMapper apiConfigMapper;
    private final AiApiClient aiApiClient;

    public ApiConfigServiceImpl(ApiConfigMapper apiConfigMapper, AiApiClient aiApiClient) {
        this.apiConfigMapper = apiConfigMapper;
        this.aiApiClient = aiApiClient;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createConfig(ApiConfig apiConfig, Long userId) {
        apiConfig.setCreatedBy(userId);
        apiConfig.setStatus(1);
        return apiConfigMapper.insert(apiConfig) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateConfig(ApiConfig apiConfig, Long userId) {
        ApiConfig existingConfig = apiConfigMapper.selectById(apiConfig.getId());
        if (existingConfig == null) {
            throw new RuntimeException("API配置不存在");
        }
        return apiConfigMapper.updateById(apiConfig) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteConfig(Long id, Long userId) {
        return apiConfigMapper.deleteById(id) > 0;
    }

    @Override
    public ApiConfig getConfigById(Long id) {
        return apiConfigMapper.selectById(id);
    }

    @Override
    public List<ApiConfig> getEnabledConfigs() {
        LambdaQueryWrapper<ApiConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiConfig::getStatus, 1)
                .orderByDesc(ApiConfig::getCreatedTime);
        return apiConfigMapper.selectList(wrapper);
    }

    @Override
    public PageResult<ApiConfig> getConfigList(int current, int size, String provider, Integer status) {
        Page<ApiConfig> page = new Page<>(current, size);
        LambdaQueryWrapper<ApiConfig> wrapper = new LambdaQueryWrapper<>();
        
        if (provider != null && !provider.isEmpty()) {
            wrapper.eq(ApiConfig::getProvider, provider);
        }
        if (status != null) {
            wrapper.eq(ApiConfig::getStatus, status);
        }
        
        wrapper.orderByDesc(ApiConfig::getCreatedTime);
        
        Page<ApiConfig> result = apiConfigMapper.selectPage(page, wrapper);
        
        return new PageResult<>(
                result.getRecords(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize()
        );
    }

    @Override
    public boolean testConfig(Long id) {
        ApiConfig config = apiConfigMapper.selectById(id);
        if (config == null) {
            throw new RuntimeException("API配置不存在");
        }

        try {
            // 发送测试消息
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "user", "content", "Hi, this is a test message."));
            
            String response = aiApiClient.callAiApi(config, messages);
            
            return response != null && !response.isEmpty();
        } catch (Exception e) {
            throw new RuntimeException("API测试失败: " + e.getMessage());
        }
    }
}
