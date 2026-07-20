package com.example.yangaiagent.ChatMemory;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * 这是用来测试的ChatMemory实现类
 */

public class MysqlChatMemory implements ChatMemory {

    @Override
    public void add(String conversationId, List<Message> messages) {

    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        return List.of();
    }

    @Override
    public void clear(String conversationId) {

    }
}
