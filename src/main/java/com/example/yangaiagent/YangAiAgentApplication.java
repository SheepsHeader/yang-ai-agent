package com.example.yangaiagent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.yangaiagent.Mapper")
public class YangAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(YangAiAgentApplication.class, args);
    }

}