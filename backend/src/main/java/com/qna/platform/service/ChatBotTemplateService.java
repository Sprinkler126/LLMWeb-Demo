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
     * 获取所有公开的机器人模板（status=1，用户可见）
     *
     * @return 公开的机器人模板列表
     */
    List<ChatBotTemplate> getAllEnabledTemplates();

    /**
     * 获取所有机器人模板（管理员使用，包含所有状态）
     *
     * @return 所有机器人模板列表
     */
    List<ChatBotTemplate> getAllTemplates();

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