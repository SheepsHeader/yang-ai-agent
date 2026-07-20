package com.example.yangaiagent.Advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.model.MessageAggregator;
import reactor.core.publisher.Flux;

/**
 * 自定义日志 Advisor - 责任链模式实现
 * 
 * 【责任链模式说明】
 * Spring AI 的 Advisor 体系采用责任链模式（Chain of Responsibility）：
 * 1. 多个 Advisor 按顺序组成一条链条
 * 2. 每个 Advisor 可以在请求前后添加自定义逻辑
 * 3. 通过 chain.nextAroundCall()/chain.nextAroundStream() 传递请求给下一个节点
 * 4. 链条末端是实际的 AI 模型调用
 * 
 * 【执行流程示例】
 * 请求 → MyLoggerAdvisor → MessageChatMemoryAdvisor → AI模型 → 返回响应
 *         ↓                     ↓                     ↓
 *      打印日志              注入历史消息          执行实际调用
 * 
 * 本类实现两个接口：
 * - CallAroundAdvisor：处理同步调用（一次性返回完整响应）
 * - StreamAroundAdvisor：处理流式调用（分块返回响应）
 */
@Slf4j
public class MyLoggerAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;  // 执行顺序，数字越小优先级越高
    }

    /**
     * 请求前置处理：打印用户输入日志
     */
    private AdvisedRequest before(AdvisedRequest request) {
        log.info("AI Request: {}", request.userText());
        return request;
    }

    /**
     * 响应后置处理：打印 AI 回复日志
     */
    private void observeAfter(AdvisedResponse advisedResponse) {
        log.info("AI Response: {}", advisedResponse.response().getResult().getOutput().getText());
    }

    /**
     * 同步调用的责任链处理
     * 
     * 【责任链执行机制】
     * chain.nextAroundCall(request) 会递归调用链条中的下一个 Advisor，
     * 直到到达链条末端（AI模型），然后将结果沿链条反向返回。
     * 
     * 执行流程：
     * 1. before() - 请求前打印日志
     * 2. chain.nextAroundCall() - 传递给下一个拦截器（递归调用整个链条）
     * 3. observeAfter() - 响应后打印日志
     * 4. 返回响应给上一层
     */
    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        // ① 请求前置处理
        advisedRequest = this.before(advisedRequest);
        
        // ② 传递给下一个拦截器（递归调用，最终到达AI模型）
        // 这是阻塞调用，会等待整个链条执行完毕才返回
        AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);
        
        // ③ 响应后置处理
        this.observeAfter(advisedResponse);
        
        // ④ 返回响应给上一层
        return advisedResponse;
    }

    /**
     * 流式调用的责任链处理
     * 
     * 【流式响应特殊处理】
     * 流式响应是分块返回的，需要使用 MessageAggregator 将多个分块聚合为完整响应，
     * 然后再执行 observeAfter 回调打印完整日志。
     * 
     * 执行流程：
     * 1. before() - 请求前打印日志
     * 2. chain.nextAroundStream() - 获取流式响应（Flux<AdvisedResponse>）
     * 3. MessageAggregator.aggregateAdvisedResponse() - 聚合分块响应
     * 4. this::observeAfter - 聚合完成后执行回调打印日志
     */
    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        // ① 请求前置处理
        advisedRequest = this.before(advisedRequest);
        
        // ② 获取流式响应（多个分块）
        Flux<AdvisedResponse> advisedResponses = chain.nextAroundStream(advisedRequest);
        
        // ③ 聚合流式响应并在完成后执行回调
        // MessageAggregator：将多个分块响应合并为完整响应
        // this::observeAfter：方法引用，作为回调函数在聚合完成后执行
        return (new MessageAggregator()).aggregateAdvisedResponse(advisedResponses, this::observeAfter);
    }
}