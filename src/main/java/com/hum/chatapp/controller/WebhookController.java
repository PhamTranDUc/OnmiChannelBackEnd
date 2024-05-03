package com.hum.chatapp.controller;


import com.hum.chatapp.dto.MessageRequest;
import com.hum.chatapp.dto.MessageResponse;
import com.hum.chatapp.dto.WebHookPayLoad;
import com.hum.chatapp.entity.Conversation;
import com.hum.chatapp.entity.Message;
import com.hum.chatapp.entity.User;
import com.hum.chatapp.facebook.FacebookService;
import com.hum.chatapp.repository.ConversationRepository;
import com.hum.chatapp.repository.MessageRepository;
import com.hum.chatapp.repository.UserRepository;
import com.hum.chatapp.service.MessageSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WebhookController {

//    @Autowired
//    private ChatMessageService chatMessageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageSocketService messageSocketService;

    @Autowired
    private MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private FacebookService facebookService;

    private static final String VERIFY_TOKEN = "12345";
    @GetMapping
    public String handleGetRequest(@RequestParam("hub.mode") String mode,
                                   @RequestParam("hub.verify_token") String token,
                                   @RequestParam("hub.challenge") String challenge) {
        System.out.println("-------------- New Request GEThub.verify_token --------------");

        System.out.println("Body: mode=" + mode + ", token=" + token + ", challenge=" + challenge);

        if ("subscribe".equals(mode) && VERIFY_TOKEN.equals(token)) {
            System.out.println("WEBHOOK_VERIFIED");
            return challenge;
        } else {
            System.out.println("Responding with 403 Forbidden");
            return "403 Forbidden";
        }
    }
    @PostMapping
    public void handlePostRequest(@RequestBody WebHookPayLoad payload) {
//        System.out.println("-------------- New Request POST --------------");

        for (WebHookPayLoad.Entry entry : payload.getEntry()) {

            for (WebHookPayLoad.Messaging messaging : entry.getMessaging()) {


                System.out.println("Message ID: " + messaging.getMessage().getMid());
                System.out.println("Message Text: " + messaging.getMessage().getText());

                System.out.println("Sender ID: " + messaging.getSender().getId());

                System.out.println("Recipient ID: " + messaging.getRecipient().getId());

                long timestamp = messaging.getTimestamp();
                LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
                System.out.println("Time: " + dateTime );

                Message chatMessage= new Message();
                chatMessage.setTimestamp(dateTime);

                Optional<User> userSender = userRepository.findById(messaging.getSender().getId());
                if (userSender.isPresent()){
                    chatMessage.setSender(userSender.get());
                }else {
                    User newUserSender= facebookService.saveUserForFacebookId(messaging.getSender().getId());
                    chatMessage.setSender(newUserSender);
                }

                Optional<User> userRecipient = userRepository.findById(messaging.getRecipient().getId());
                if (userRecipient.isPresent()){
                    chatMessage.setReceiver(userRecipient.get());
                }else {
                    User newUser= new User(messaging.getRecipient().getId(),"admin","avatar");
                    userRepository.save(newUser);

                }


                Integer conversationId;



                Optional<Conversation> conversation=
                        conversationRepository.findConversationByUsers(chatMessage.getSender(),chatMessage.getReceiver());
                 if(conversation.isPresent()){
                     conversationId=conversation.get().getConversationId();
                     chatMessage.setConversation(conversation.get());
                 }else {
                     Conversation newConversation= new Conversation(chatMessage.getSender(),chatMessage.getReceiver());
                    Conversation conversationAfterSave= conversationRepository.save(newConversation);
                     conversationId=conversationAfterSave.getConversationId();
                     chatMessage.setConversation(conversationAfterSave);
                 }


                chatMessage.setMessage(messaging.getMessage().getText());
                Message messageDB=messageRepository.save(chatMessage);

                MessageRequest ms=MessageRequest.builder()
                        .conversationId(conversationId)
                        .senderId(chatMessage.getSender().getId())
                        .receiverId(chatMessage.getReceiver().getId())
                        .message(chatMessage.getMessage())
                        .timestamp(chatMessage.getTimestamp())
                        .build();

                messageSocketService.saveMessage(ms);



            }
        }

        System.out.println("Body:"+ payload);    }




}
