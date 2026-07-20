package com.example.yangaiagent.App;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;


@Slf4j
@Component
public class LoverCoserApp {

    private final ChatClient loverChatClient;

    public LoverCoserApp(ChatModel dashboardChatModel) throws IOException {
        ClassPathResource resource = new ClassPathResource("prompt/LoverCoser-prompt");
        String prompt = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        InMemoryChatMemory inMemoryChatMemory = new InMemoryChatMemory();
        this.loverChatClient = ChatClient.builder(dashboardChatModel)
                .defaultSystem(prompt)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(inMemoryChatMemory)
                )
                .build();
    }

    /**
     * 执行对话请求，支持多用户会话隔离的聊天记忆
     * 
     * @param message 用户输入的消息
     * @param chatId  会话唯一标识（用于区分不同用户的对话记忆）
     * @return AI 回复内容
     * 
     * 核心流程说明：
     * 1. .advisors(spec -> ...) 是请求级配置，为本次请求设置参数
     * 2. spec 是框架传入的配置对象，相当于"参数传递载体"
     * 3. CHAT_MEMORY_CONVERSATION_ID_KEY = 会话ID，告诉记忆系统"当前服务哪个用户"
     * 4. CHAT_MEMORY_RETRIEVE_SIZE_KEY = 历史消息数量，告诉记忆系统"要查几条历史"
     * 这里参数的key是预定义的，不能自定义，然后与全局配置中的参数key对应
     * 
     * 与全局配置的关系：
     * - 全局配置（defaultAdvisors）：注册了 MessageChatMemoryAdvisor 这个"记忆顾问"
     * - 请求级配置（此处）：告诉顾问"这次服务的客户是谁，要查多少历史"
     * - 顾问根据这些参数，从 InMemoryChatMemory 中检索对应会话的历史消息
     * - 最后将历史消息注入到请求中，让 AI 能够"记住"之前的对话
     */
    public String doChat(String message, String chatId) {
        ChatResponse response = loverChatClient
                .prompt()
                .user(message)
                // 设置本次请求的记忆参数
                // spec 是框架传入的配置对象，通过 param(key, value) 设置参数
                .advisors(spec -> spec
                        // 会话唯一标识：区分不同用户的对话记忆
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        // 历史消息数量：每次调用携带最近10条历史
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

}