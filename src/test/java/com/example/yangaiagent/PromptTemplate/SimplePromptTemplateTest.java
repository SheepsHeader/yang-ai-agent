package com.example.yangaiagent.PromptTemplate;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试 SimplePromptTemplate 类
 * JUnit 测试方法中必须包含至少一个断言（Assertions.assertEquals、Assertions.assertTrue、Assertions.assertThrows 等），不能只调用 log.info()
 * 打印结果就结束。没有断言的测试永远不会失败，等于没有测。
 */

@Slf4j
public class SimplePromptTemplateTest {

    @Test
    public void testRender() {
        // 定义带有变量的模板
        String template = "你好，{name}。今天是{day}，天气{weather}。";

        // 创建模板对象
        PromptTemplate promptTemplate = new PromptTemplate(template);

        // 准备变量映射
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "杨宏");
        variables.put("day", "星期一");
        variables.put("weather", "晴朗");

        // 生成最终提示文本
        String prompt = promptTemplate.render(variables);
        log.info(prompt);

        // 验证渲染结果
        String expected = "你好，杨宏。今天是星期一，天气晴朗。";
        Assertions.assertEquals(expected, prompt);
    }

    @Test
    public void testRenderWithMissingVariable() {
        // 模板中的变量未全部提供时，应抛出异常
        String template = "你好，{name}。今天是{day}。";

        PromptTemplate promptTemplate = new PromptTemplate(template);

        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "杨宏");
        // 故意不提供 day

        Assertions.assertThrows(IllegalStateException.class, () -> {
            promptTemplate.render(variables);
        });
    }

}
