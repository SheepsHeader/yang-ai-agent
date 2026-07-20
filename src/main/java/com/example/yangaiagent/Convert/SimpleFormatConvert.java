package com.example.yangaiagent.Convert;

import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.StructuredOutputConverter;

import java.util.Map;

public class SimpleFormatConvert {

    public static void main(String[] args) {
        StructuredOutputConverter outputConverter = new StructuredOutputConverter() {
            @Override
            public String getFormat() {
                return "1111";
            }

            @Override
            public @Nullable Object convert(Object source) {
                return null;
            }
        };

        String userInputTemplate = """
        ... 用户文本输入 ....
        {format}
        """; // 用户输入，包含一个“format”占位符。
        Prompt prompt = new Prompt(
                new PromptTemplate(
                        userInputTemplate,
                        Map.of( "format", outputConverter.getFormat()) // 用转换器的格式替换“format”占位符
                ).createMessage());
        System.out.println(prompt);
    }



}
