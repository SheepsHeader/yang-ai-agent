package com.example.yangaiagent.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistory {

    private Long id;

    private String conversationId;

    private String userId;

    private String message;

    private String messageType;

    private Integer isDelete;

    private LocalDateTime createTime;
}