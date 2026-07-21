package com.example.yangaiagent.repository;

import com.example.yangaiagent.Mapper.ChatHistoryMapper;
import com.example.yangaiagent.entity.ChatHistory;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 聊天历史数据访问层，封装 MyBatis Mapper。
 * <p>
 * 设计目的：
 * <ul>
 *   <li><b>解耦</b>：ChatMemory 不直接依赖 Mapper，未来换 JPA / JdbcTemplate / Redis 只改这一层。</li>
 *   <li><b>可测性</b>：mock 普通接口比 mock MyBatis 注解类容易得多。</li>
 *   <li><b>单一职责</b>：Repository 只管数据存取，ChatMemory 只管缓存语义适配。</li>
 * </ul>
 */
@Repository
@AllArgsConstructor
public class ChatHistoryRepository {

    private final ChatHistoryMapper chatHistoryMapper;

    /**
     * 保存一条聊天记录。
     *
     * @param conversationId 会话 ID
     * @param userId         用户 ID
     * @param text           消息文本
     * @param messageType    消息类型（USER / ASSISTANT / SYSTEM）
     */
    public void save(String conversationId, String userId, String text, String messageType) {
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setConversationId(conversationId);
        chatHistory.setUserId(userId);
        chatHistory.setIsDelete(0);
        chatHistory.setMessage(text);
        chatHistory.setMessageType(messageType);
        chatHistoryMapper.insert(chatHistory);
    }

    /**
     * 按会话 ID 和用户 ID 查询聊天历史。
     *
     * @param conversationId 会话 ID
     * @param userId         用户 ID
     * @return 消息列表，按时间升序
     */
    public List<ChatHistory> findByConversationId(String conversationId, String userId) {
        return chatHistoryMapper.selectByConversationIdAndUserId(conversationId, userId);
    }

    public void deleteByConversationId(String conversationId, String number) {
        chatHistoryMapper.deleteByConversationIdAndUserId(conversationId, number);
    }
}
