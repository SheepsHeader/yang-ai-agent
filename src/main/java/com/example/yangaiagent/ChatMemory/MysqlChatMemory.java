package com.example.yangaiagent.ChatMemory;

import com.example.yangaiagent.entity.ChatHistory;
import com.example.yangaiagent.repository.ChatHistoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 基于 MySQL 的 ChatMemory 实现。
 * <p>
 * <h3>架构分层</h3>
 * <pre>
 *   ChatMemory（缓存语义 —— 接口由 Spring AI 定义）
 *     └── MysqlChatMemory（适配器，负责 Message ⟷ 数据库实体 的转换）
 *           └── ChatHistoryRepository（数据访问封装）
 *                 └── ChatHistoryMapper（MyBatis SQL）
 * </pre>
 * 不在 ChatMemory 中直接注入 Mapper 的原因：
 * <ul>
 *   <li>ChatMemory 关注的是"缓存读写"，Mapper 关注的是"SQL 执行"，职责不同。</li>
 *   <li>未来切换持久层（JPA / JdbcTemplate / Redis）只需替换 Repository，不改 ChatMemory。</li>
 *   <li>单元测试中 mock Repository 远比 mock MyBatis 注解接口容易。</li>
 * </ul>
 *
 * <h3>Message 的三种具体实现</h3>
 * Spring AI 中 {@link Message} 是<b>接口</b>（不可直接 new），提供了三种内置实现：
 * <table>
 *   <tr><th>实现类</th><th>MessageType</th><th>用途</th></tr>
 *   <tr><td>{@link UserMessage}</td><td>USER</td><td>用户输入的提问</td></tr>
 *   <tr><td>{@link AssistantMessage}</td><td>ASSISTANT</td><td>AI 模型的回复</td></tr>
 *   <tr><td>{@link SystemMessage}</td><td>SYSTEM</td><td>系统提示词（system prompt）</td></tr>
 * </table>
 * 这些类都继承自 {@code AbstractMessage}（实现了 Message 接口），
 * 因此需要在本类中根据存储的 messageType 字符串重建对应的具体实例。
 *
 * <h3>关于 {@code .<Message>map(...)} 的显式类型见证</h3>
 * switch 表达式各分支返回的具体类型（UserMessage / AssistantMessage / SystemMessage）
 * 的共同父类是 {@code AbstractMessage}，编译器推断 map 的输出为 {@code Stream<AbstractMessage>}。
 * 然而 <b>Java 泛型是不变的（invariant）</b>：即使 AbstractMessage 实现了 Message，
 * {@code List<AbstractMessage>} 也不是 {@code List<Message>} 的子类型。
 * 因此必须通过 {@code .<Message>map(...)} 显式告诉编译器：请将流元素类型向上转型为 Message，
 * 这样 {@code .toList()} 才能正确返回 {@code List<Message>}。
 *
 * @see org.springframework.ai.chat.memory.ChatMemory
 * @see org.springframework.ai.chat.messages.Message
 */
@Component
@AllArgsConstructor
public class MysqlChatMemory implements ChatMemory {

    private final ChatHistoryRepository chatHistoryRepository;

    /**
     * 保存消息到数据库。
     * <p>
     * 写入时同时保存 {@code message.getMessageType().name()}，
     * 以便读取时能重建正确的 Message 子类实例。
     */
    @Override
    public void add(String conversationId, List<Message> messages) {
        for (Message message : messages) {
            chatHistoryRepository.save(
                    conversationId,
                    "1",
                    message.getText(),
                    message.getMessageType().name()
            );
        }
    }

    /**
     * 从数据库读取消息并重建为 Spring AI 的 Message 实例。
     * <p>
     * 注意 {@code .<Message>map(...)}：必须显式指定泛型类型为 Message，
     * 否则 switch 表达式返回的 AbstractMessage 会导致编译错误
     * "required: List&lt;Message&gt;, provided: List&lt;AbstractMessage&gt;"。
     * 原因详见类级 Javadoc。
     */
    @Override
    public List<Message> get(String conversationId, int lastN) {
        List<ChatHistory> chatHistories = chatHistoryRepository
                .findByConversationId(conversationId, "1");

        return chatHistories.stream()
                // 显式指定 <Message> 是必须的 —— Java 泛型不变，List<AbstractMessage> ≠ List<Message>
                .<Message>map(chatHistory -> {
                    String text = chatHistory.getMessage();
                    String type = chatHistory.getMessageType();
                    // type 可能为 null：旧数据未填充 message_type 字段，兜底为 UserMessage
                    return switch (type != null ? type : "") {
                        case "USER" -> new UserMessage(text);
                        case "ASSISTANT" -> new AssistantMessage(text);
                        case "SYSTEM" -> new SystemMessage(text);
                        // 兼容历史数据或未知类型，兜底为 UserMessage
                        default -> new UserMessage(text);
                    };
                })
                .toList();
    }

    @Override
    public void clear(String conversationId) {
        chatHistoryRepository.deleteByConversationId(conversationId, "1");
    }
}
