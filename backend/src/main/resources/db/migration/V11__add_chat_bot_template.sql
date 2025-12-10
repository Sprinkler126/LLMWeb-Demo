CREATE TABLE chat_bot_template (
                                   id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                   name VARCHAR(100) NOT NULL COMMENT '机器人名称',
                                   description TEXT COMMENT '机器人描述',
                                   system_message TEXT NOT NULL COMMENT '系统消息内容',
                                   created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用'
);
-- 添加会话级系统消息和机器人模板ID字段到chat_session表
ALTER TABLE chat_session
    ADD COLUMN system_message TEXT COMMENT '会话级系统消息' AFTER updated_time,
ADD COLUMN bot_template_id BIGINT COMMENT '使用的机器人模板ID' AFTER system_message;

-- 插入聊天机器人模板示例数据
INSERT INTO chat_bot_template (name, description, system_message, status) VALUES
                                                                              ('通用助手', '一个多用途的智能助手，可以回答各种问题', '你是一个多用途的智能助手，能够回答各种问题。请以简洁明了的方式提供准确的信息。', 1),
                                                                              ('技术专家', '专门解答编程和技术问题的专家', '你是一位资深的技术专家，专注于软件开发和编程领域。请提供专业、深入的技术解答，并给出实用的代码示例。', 1),
                                                                              ('写作导师', '帮助用户改进写作技巧和文案润色', '你是一位经验丰富的写作导师，擅长帮助用户提升写作技巧。请协助用户润色文本，提供创意建议，并指出可以改进的地方。', 1),
                                                                              ('语言学习伙伴', '辅助用户学习外语的虚拟伙伴', '你是一个专业的语言学习伙伴，帮助用户提高外语水平。请使用简单易懂的语言解释语法规则，并提供丰富的词汇和表达方式。', 1),
                                                                              ('心理咨询师', '提供心理健康支持和情绪疏导', '你是一位专业的心理咨询师，为用户提供心理健康支持。请以温和、理解的态度倾听用户的困扰，并提供积极的心理疏导建议。', 1);