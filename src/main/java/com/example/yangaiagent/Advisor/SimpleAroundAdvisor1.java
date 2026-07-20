package com.example.yangaiagent.Advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.*;
import reactor.core.publisher.Flux;

/**
  验证spring ai chat model的环绕拦截器advisor是否生效，以及advisor的执行顺序
  这边order越小，执行顺序越早，同时后置advisor的执行顺序越晚
 */
@Slf4j
public class SimpleAroundAdvisor1 implements CallAroundAdvisor, StreamAroundAdvisor {

    @Override
    public String getName() {
        return "SimpleBeforeAdvisor";
    }

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        log.info("SimpleBeforeAdvisor1 aroundCall");
        AdvisedResponse response = chain.nextAroundCall(advisedRequest);
        log.info("SimpleAroundAdvisor1 aroundCall after");
        return response;
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        return chain.nextAroundStream(advisedRequest);
    }
}
