-- 更新chat_bot_template表的status字段注释，支持三种状态
-- 0: 停用（不可用）
-- 1: 公开（用户可见）
-- 2: 内部（仅供系统内部流程使用，用户不可见）

ALTER TABLE chat_bot_template 
MODIFY COLUMN status TINYINT DEFAULT 1 COMMENT '状态：0-停用，1-公开（用户可见），2-内部（系统流程专用）';
