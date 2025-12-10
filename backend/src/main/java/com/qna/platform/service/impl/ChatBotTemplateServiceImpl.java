package com.qna.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qna.platform.entity.ChatBotTemplate;
import com.qna.platform.mapper.ChatBotTemplateMapper;
import com.qna.platform.service.ChatBotTemplateService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天机器人模板服务实现类
 *
 * @author QnA Platform
 */
@Service
public class ChatBotTemplateServiceImpl extends ServiceImpl<ChatBotTemplateMapper, ChatBotTemplate> implements ChatBotTemplateService {

    private final ChatBotTemplateMapper chatBotTemplateMapper;

    public ChatBotTemplateServiceImpl(ChatBotTemplateMapper chatBotTemplateMapper) {
        this.chatBotTemplateMapper = chatBotTemplateMapper;
    }

    @Override
    public List<ChatBotTemplate> getAllEnabledTemplates() {
        // 只返回公开的模板（status=1），用于普通用户聊天界面
        LambdaQueryWrapper<ChatBotTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatBotTemplate::getStatus, 1)
                .orderByDesc(ChatBotTemplate::getCreatedTime);
        return chatBotTemplateMapper.selectList(wrapper);
    }

    @Override
    public List<ChatBotTemplate> getAllTemplates() {
        // 返回所有状态的模板，用于管理员管理界面
        LambdaQueryWrapper<ChatBotTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ChatBotTemplate::getCreatedTime);
        return chatBotTemplateMapper.selectList(wrapper);
    }

    @Override
    public void createTemplate(ChatBotTemplate template) {
        template.setCreatedTime(LocalDateTime.now());
        if (template.getStatus() == null) {
            template.setStatus(1); // 默认启用
        }
        chatBotTemplateMapper.insert(template);
    }

    @Override
    public void updateTemplate(ChatBotTemplate template) {
        chatBotTemplateMapper.updateById(template);
    }

    @Override
    public void deleteTemplate(Long id) {
        chatBotTemplateMapper.deleteById(id);
    }
}