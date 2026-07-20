package com.example.yangaiagent.ChatModel;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


/**
*CommandLineRunner的作用是，在spring boot应用启动完成后，执行run方法，用于初始化应用状态
 */
@Component
public class SpringAiAiInvoke implements CommandLineRunner {

    @Resource
    private ChatModel dashscopeChatModel;

    @Override
    public void run(String... args) throws Exception {
        AssistantMessage output = dashscopeChatModel.call(new Prompt("你好，我是杨宏"))
                .getResult()
                .getOutput();
        System.out.println(output.getText());
    }
}
