# 数据库名
CREATE DATABASE IF NOT EXISTS yang_ai_agent;
USE yang_ai_agent;

# 对话历史表
CREATE TABLE IF NOT EXISTS chat_history (
                                            id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
                                            conversation_id VARCHAR(64) NOT NULL COMMENT '会话ID',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    message TEXT NOT NULL COMMENT '消息内容',
    is_delete TINYINT(1) DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_user_id (user_id),
    INDEX idx_is_delete (is_delete)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话历史表';
ALTER TABLE chat_history ADD COLUMN message_type VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '消息类型: USER/ASSISTANT/SYSTEM';
