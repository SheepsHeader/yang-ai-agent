package com.example.yangaiagent.Advisor;

import org.springframework.ai.chat.client.advisor.api.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义 RE2（Re-Reading）Advisor
 * 
 * 【核心原理】
 * 通过让模型"重读问题"来提高推理能力的提示词工程技巧。
 * 
 * 【模板+参数模式说明】
 * userText 和 userParams 是配合使用的：
 * - userText：作为模板字符串，包含占位符 {参数名}
 * - userParams：作为参数映射，存储实际值
 * - Spring AI 会自动将 {参数名} 替换为 userParams 中的对应值
 * 
 * 【执行效果】
 * 原始输入："什么是人工智能？"
 * 处理后发送给模型：
 * """
 * 什么是人工智能？
 * Read the question again: 什么是人工智能？
 * """
 */
public class ReReadingAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {


    /**
     * 请求前置处理：实现 RE2 重读技巧
     * 
     * 【处理流程】
     * 1. 将原始用户问题存入参数映射（key = "re2_input_query"）
     * 2. 使用模板格式构建新的 userText，包含占位符 {re2_input_query}
     * 3. Spring AI 自动替换占位符，生成最终发送给模型的文本
     */
    private AdvisedRequest before(AdvisedRequest advisedRequest) {
        // ① 获取现有参数并添加新参数
        // 将原始用户问题存入 userParams，key 为 "re2_input_query"
        Map<String, Object> advisedUserParams = new HashMap<>(advisedRequest.userParams());
        advisedUserParams.put("re2_input_query", advisedRequest.userText());

        // ② 构建新的请求
        // userText 作为模板，包含 {re2_input_query} 占位符
        // userParams 作为数据源，提供实际值
        return AdvisedRequest.from(advisedRequest)
                .userText("""
                        {re2_input_query}
                        Read the question again: {re2_input_query}
                        """)
                .userParams(advisedUserParams)
                .build();
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        return chain.nextAroundCall(this.before(advisedRequest));
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        return chain.nextAroundStream(this.before(advisedRequest));
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }
}