package com.hum.chatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageRequest {
    Integer conversationId;
    String senderId;
    String receiverId;
    String message;
    LocalDateTime timestamp;
}
