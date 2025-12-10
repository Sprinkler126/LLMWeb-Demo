package com.qna.platform.controller;

import com.qna.platform.annotation.RequireRole;
import com.qna.platform.common.Result;
import com.qna.platform.entity.ChatBotTemplate;
import com.qna.platform.service.ChatBotTemplateService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 * @author LHR
 * @email sprinkler@foxmail.com
 * @date 2025/12/10 11:03
 */
@RestController
@RequestMapping("/admin/bot-template")
@RequireRole({"ADMIN", "SUPER_ADMIN"})
public class ChatBotTemplateController {

    private final ChatBotTemplateService chatBotTemplateService;
    public ChatBotTemplateController(ChatBotTemplateService chatBotTemplateService) {
        this.chatBotTemplateService = chatBotTemplateService;
    }

    // 获取所有启用的机器人模板
    @GetMapping("/list")
    public Result<List<ChatBotTemplate>> listTemplates() {
        List<ChatBotTemplate> templates = chatBotTemplateService.getAllEnabledTemplates();
        return Result.success(templates);
    }

    // 管理员创建/编辑/删除模板
    @PostMapping("/create")
    public Result<String> createTemplate(@RequestBody ChatBotTemplate template) {
        chatBotTemplateService.createTemplate(template);
        return Result.success("创建成功");
    }

    @PutMapping("/update")
    public Result<String> updateTemplate(@RequestBody ChatBotTemplate template) {
        chatBotTemplateService.updateTemplate(template);
        return Result.success("更新成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> deleteTemplate(@PathVariable Long id) {
        chatBotTemplateService.deleteTemplate(id);
        return Result.success("删除成功");
    }
}
