package com.example.yangaiagent.ChatMemory;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
*zz这是测试的ChatMemory实现类：SimpleChatMemory内存实现
 */
public class SimpleChatMemory implements ChatMemory {
    private final Map<String, List<Message>> memory = new HashMap<>();

    @Override
    public void add(String conversationId, List<Message> messages) {
        memory.put("default", messages);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        return memory.getOrDefault("default", List.of());
    }

    @Override
    public void clear(String conversationId) {
        memory.remove("default");
    }
}
