package com.hum.chatapp.controller;

import com.hum.chatapp.dto.MessageRequest;
import com.hum.chatapp.dto.MessageResponse;
import com.hum.chatapp.entity.Conversation;
import com.hum.chatapp.entity.Message;
import com.hum.chatapp.entity.User;
import com.hum.chatapp.facebook.FacebookService;
import com.hum.chatapp.repository.ConversationRepository;
import com.hum.chatapp.repository.MessageRepository;
import com.hum.chatapp.repository.UserRepository;
import com.hum.chatapp.service.MessageSocketService;
import com.hum.chatapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

/**
 * Controller class that handles real-time messaging using WebSocket communication.
 * Routes:
 * - /user: Send user conversations to a specific user by their user ID through a web socket.
 * - /conv: Send messages of a specific conversation to the connected users through a web socket.
 * - /sendMessage: Save a new message using a web socket.
 * - /deleteConversation: Delete a conversation by its unique conversation ID using a web socket.
 * - /deleteMessage: Delete a message by its unique message ID within a conversation using a web socket.
 */
@RequiredArgsConstructor
@CrossOrigin("*")
@Controller
public class MessageSocketController {

    private final MessageSocketService socketService;
    private final FacebookService facebookService;
    private final UserService userService;

    private final ConversationRepository conversationRepository;

    private final MessageRepository messageRepository;

//    public MessageSocketController(UserService userService, ConversationRepository conversationRepository,MessageRepository messageRepository,MessageSocketService socketService){
//        this.userService= userService;
//        this.conversationRepository=conversationRepository;
//        this.messageRepository = messageRepository;
//        this.socketService = socketService;
//    }
    /**
     * Send user conversations to a specific user by their user ID through a web socket.
     *
     * @param userId The ID of the user for whom to send conversations.
     */
    @MessageMapping("/user")
    public void sendUserConversationByUserId(String userId) {
        socketService.sendUserConversationByUserId(userId);
    }

    /**
     * Send messages of a specific conversation to the connected users through a web socket.
     *
     * @param conversationId The ID of the conversation for which to send messages.
     */
    @MessageMapping("/conv")
    public void sendMessagesByConversationId(int conversationId) {
        socketService.sendMessagesByConversationId(conversationId);
    }

    @MessageMapping("/conv-ms")
    public void getBoxMessage(int conversationId) {
        socketService.getBoxMessage(conversationId);
    }

    /**
     * Save a new message using a web socket.
     *
     * @param message The MessageRequest object containing the message details to be saved.
     */
    @MessageMapping("/sendMessage")
    public void saveMessage(MessageRequest message) {
        socketService.saveMessage(message);
    }

    /**
     * Delete a conversation by its unique conversation ID using a web socket.
     *
     * @param payload A Map containing the conversationId, user1Id, and user2Id for the
     *                conversation to be deleted and notify listener.
     */
    @MessageMapping("/deleteConversation")
    public void deleteConversation(Map<String, Object> payload) {
        int conversationId = (int) payload.get("conversationId");
        String user1 =  payload.get("user1Id").toString();
        String user2 =  payload.get("user2Id").toString();
        socketService.deleteConversationByConversationId(conversationId);
        socketService.sendUserConversationByUserId(user1);
        socketService.sendUserConversationByUserId(user2);
    }

    /**
     * Delete a message by its unique message ID within a conversation using a web socket.
     *
     * @param payload A Map containing the conversationId and messageId for the message
     *                to be deleted and notify listener.
     */
    @MessageMapping("/deleteMessage")
    public void deleteMessage(Map<String, Object> payload) {
        int conversationId = (int) payload.get("conversationId");
        int messageId = (int) payload.get("messageId");
        socketService.deleteMessageByMessageId(conversationId, messageId);
    }

    @PostMapping("/saveMessageReplyCustomer")
    public ResponseEntity<?> saveMessageReplyCustomer(@RequestBody MessageRequest messageRequest) {
        facebookService.sendMessagesByConversationId(messageRequest);
        facebookService.sendMessageToFaceBookById(messageRequest.getReceiverId(),messageRequest.getMessage());
        return ResponseEntity.status(HttpStatus.OK).body("ok");

    }

}
