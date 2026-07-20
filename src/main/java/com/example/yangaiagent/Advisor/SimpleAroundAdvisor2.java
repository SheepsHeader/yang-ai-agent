package com.example.yangaiagent.Advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;

@Slf4j
public class SimpleAroundAdvisor2 implements CallAroundAdvisor {

    @Override
    public String getName() {
        return "SimpleBeforeAdvisor2";
    }

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        log.info("SimpleBeforeAdvisor2 aroundCall");
        AdvisedResponse response = chain.nextAroundCall(advisedRequest);
        log.info("SimpleAroundAdvisor2 aroundCall after");
        return response;
    }
}
