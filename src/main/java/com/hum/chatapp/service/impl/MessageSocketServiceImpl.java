package com.hum.chatapp.service.impl;

import com.hum.chatapp.dto.*;
import com.hum.chatapp.entity.Conversation;
import com.hum.chatapp.entity.Message;
import com.hum.chatapp.entity.User;
import com.hum.chatapp.repository.ConversationRepository;
import com.hum.chatapp.repository.MessageRepository;
import com.hum.chatapp.repository.UserRepository;
import com.hum.chatapp.service.MessageSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the MessageSocketService interface that handles real-time messaging functionality using web sockets.
 */
@Service
@RequiredArgsConstructor
public class MessageSocketServiceImpl implements MessageSocketService {
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    /**
     * Send user conversations to a specific user by their user ID through a web socket.
     *
     * @param userId The ID of the user for whom to send conversations.
     */
    //trả về danh sách các box chat với id của người dùng đó (mỗi box chat
    // mỗi box chat trả về bao gồm  ID của cuộc trò chuyện,
    // ID của người dùng đối tác, tên của người dùng đối tác, tin nhắn cuối cùng)
    @Override
    public void sendUserConversationByUserId(String userId) {
        List<ConversationResponse> conversation = conversationRepository.findConversationsByUserId(userId);
        messagingTemplate.convertAndSend(
                "/topic/user/".concat(userId),
                WebSocketResponse.builder()
                        .type("ALL")
                        .data(conversation)
                        .build()
        );
    }

    /**
     * Send messages of a specific conversation to the connected users through a web socket.
     *
     * @param conversationId The ID of the conversation for which to send messages.
     */
    //lấy ra list các message trong box chat cụ thể bằng id của cuộc trò chuyện
    @Override
    public void sendMessagesByConversationId(int conversationId) {
        Conversation conversation = new Conversation();
        conversation.setConversationId(conversationId);
        List<Message> messageList = messageRepository.findAllByConversation(conversation);
        List<MessageResponse> messageResponseList = messageList.stream()
                .map((message -> MessageResponse.builder()
                        .messageId(message.getMessageId())
                        .message(message.getMessage())
//                        .timestamp(Date.from(message.getTimestamp().atZone(ZoneId.systemDefault()).toInstant()))
                        .senderId(message.getSender().getId())
                        .receiverId(message.getReceiver().getId())
                        .build())
                ).toList();

        messagingTemplate.convertAndSend("/topic/conv/".concat(String.valueOf(conversationId)), WebSocketResponse.builder()
                .type("ALL")
                .data(messageResponseList)
                .build()
        );
    }


    @Override
    public void getBoxMessage(int conversationId) {
        Conversation conversation = new Conversation();
        conversation.setConversationId(conversationId);
        List<Message> messageList = messageRepository.findAllByConversation(conversation);
        List<MessageResponse> messageResponseList = messageList.stream()
                .map((message -> MessageResponse.builder()
                                .messageId(message.getMessageId())
                                .message(message.getMessage())
//                        .timestamp(Date.from(message.getTimestamp().atZone(ZoneId.systemDefault()).toInstant()))
                                .senderId(message.getSender().getId())
                                .receiverId(message.getReceiver().getId())
                                .build())
                ).toList();
        messagingTemplate.convertAndSend("/topic/conv-ms/".concat(String.valueOf(conversationId)), WebSocketResponse.builder()
                .type("ALL")
                .data(messageResponseList)
                .build()
        );
    }

    /**
     * Save a new message using a web socket.
     *
     * @param msg The MessageRequest object containing the message details to be saved.
     */
    //Lưu tin nhắn mới và truyền nó qua socket
    @Override
    public void saveMessage(MessageRequest msg) {
        Optional<User> optionalUserSender= userRepository.findById(msg.getSenderId());
        if(optionalUserSender.isPresent()){
            User sender = optionalUserSender.get();
        }
//        else {
//            User userSender= new User(msg.getSenderId(),ms)
//        }

        User sender = userRepository.findById(msg.getSenderId()).get();
        User receiver = userRepository.findById(msg.getReceiverId()).get();
        Conversation conversation = conversationRepository.findConversationByUsers(sender, receiver).get();
        Message newMessage = new Message();
        newMessage.setMessage(msg.getMessage());
//        newMessage.setTimestamp(msg.getTimestamp());
        newMessage.setConversation(conversation);
        newMessage.setSender(sender);
        newMessage.setReceiver(receiver);
//        Message savedMessage = messageRepository.save(newMessage);
        // notify listener
        MessageResponse res = MessageResponse.builder()
                .messageId(newMessage.getMessageId())
                .message(newMessage.getMessage())
         //       .timestamp(Date.from(savedMessage.getTimestamp().atZone(ZoneId.systemDefault()).toInstant()))
                .senderId(newMessage.getSender().getId())
                .receiverId(newMessage.getReceiver().getId())
                .build();
        messagingTemplate.convertAndSend("/topic/conv/".concat(msg.getConversationId().toString()),
                WebSocketResponse.builder()
                        .type("ADDED")
                        .data(res)
                        .build()
        );
        sendUserConversationByUserId(msg.getSenderId());
        sendUserConversationByUserId(msg.getReceiverId());
    }

    /**
     * Delete a conversation by its unique conversation ID using a web socket.
     *
     * @param conversationId The ID of the conversation to be deleted.
     */
    @Transactional
    @Override
    public void deleteConversationByConversationId(int conversationId) {
        Conversation c = new Conversation();
        c.setConversationId(conversationId);
        messageRepository.deleteAllByConversation(c);
        conversationRepository.deleteById(conversationId);
    }

    /**
     * Delete a message by its unique message ID within a conversation using a web socket.
     *
     * @param conversationId The ID of the conversation to notify its listener.
     * @param messageId      The ID of the message to be deleted.
     */
    @Override
    public void deleteMessageByMessageId(int conversationId, int messageId) {
        messageRepository.deleteById(messageId);
        // notify listener
        sendMessagesByConversationId(conversationId);
    }
}
