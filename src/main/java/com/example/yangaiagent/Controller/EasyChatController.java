package com.example.yangaiagent.Controller;

import com.example.yangaiagent.ChatMemory.SimpleChatMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EasyChatController {

    private final ChatClient easyChatClient;

    public EasyChatController(ChatClient.Builder chatClientBuilder){
        this.easyChatClient = chatClientBuilder.defaultSystem("你是一个喜欢回答中夹杂颜文字表达自己的情绪的ai智能助手")
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(new SimpleChatMemory())
                )
                .build();
    }

    @GetMapping("/chat")
    public String chat(String message) {
        return this.easyChatClient.prompt()
                .user(message)
                .call()
                .content();
    }


}
