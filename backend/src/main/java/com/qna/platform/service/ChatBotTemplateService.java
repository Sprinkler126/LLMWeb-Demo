package com.qna.platform.service;

import com.qna.platform.entity.ChatBotTemplate;
import java.util.List;

/**
 * 聊天机器人模板服务接口
 *
 * @author QnA Platform
 */
public interface ChatBotTemplateService {

    /**
     * 获取所有启用的机器人模板
     *
     * @return 启用的机器人模板列表
     */
    List<ChatBotTemplate> getAllEnabledTemplates();

    /**
     * 创建机器人模板
     *
     * @param template 机器人模板
     */
    void createTemplate(ChatBotTemplate template);

    /**
     * 更新机器人模板
     *
     * @param template 机器人模板
     */
    void updateTemplate(ChatBotTemplate template);

    /**
     * 删除机器人模板
     *
     * @param id 模板ID
     */
    void deleteTemplate(Long id);
}