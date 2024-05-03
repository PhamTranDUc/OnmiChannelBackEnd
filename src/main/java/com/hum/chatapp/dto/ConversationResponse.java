package com.hum.chatapp.dto;

import java.sql.Timestamp;

public interface ConversationResponse {

    Integer getConversationId();

    String getOtherUserId();

    String getOtherUserName();

    String getLastMessage();
    String getOtherUserImage();
    String getPageName();

    Timestamp getLastMessageTimestamp();
}
