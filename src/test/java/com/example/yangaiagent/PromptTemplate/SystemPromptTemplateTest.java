package com.example.yangaiagent.PromptTemplate;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest
public class SystemPromptTemplateTest {

    @Resource
    private ChatModel chatModel;

    @Test
    public void testRender() {
        // 定义带有变量的模板
        String systemText = "你是一个专业的翻译助手，你的名字是{name}，你的爱好是{hobby}";
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemText);
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("name", "张三", "hobby", "运动"));

        String userText = "你好，我是张三，我喜欢运动,帮我翻译成英文";
        Message userMessage = new UserMessage(userText);

        Prompt prompt = new Prompt(List.of( systemMessage, userMessage));
        log.info("prompt: {}", prompt);

        List<Generation> results = chatModel.call(prompt).getResults();
        log.info("results: {}", results);

        Assertions.assertNotNull(results, "AI 响应结果不应为空");
        Assertions.assertFalse(results.isEmpty(), "响应结果列表不应为空");


    }
}
