package com.example.yangaiagent.Controller;

import com.example.yangaiagent.Advisor.SimpleAroundAdvisor1;
import com.example.yangaiagent.Advisor.SimpleAroundAdvisor2;
import com.example.yangaiagent.App.LoveReport;
import com.example.yangaiagent.App.LoverCoserApp;
import com.example.yangaiagent.ChatMemory.SimpleChatMemory;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class EasyChatController {

    private final ChatClient easyChatClient;

    @Resource
    private LoverCoserApp loverCoserApp;

    public EasyChatController(ChatClient.Builder chatClientBuilder){
        this.easyChatClient = chatClientBuilder.defaultSystem("你是一个喜欢回答中夹杂颜文字表达自己的情绪的ai智能助手")
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(new SimpleChatMemory()),
                        new SimpleAroundAdvisor2(),
                        new SimpleAroundAdvisor1()
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

    @GetMapping("/chat/loverReturnReport")
    public LoveReport chatLoverReturnReport(String message, String chatId) {
        return loverCoserApp.doChatReturnReport(message, chatId);
    }

    @GetMapping("/chat/lover")
    public String chatLover(String message, String chatId) {
        return loverCoserApp.doChat(message, chatId);
    }

    @GetMapping("/chat/loverRag")
    public String chatLoverRag(String message, String chatId) {
        return loverCoserApp.doChatWithRag(message, chatId);
    }


}
