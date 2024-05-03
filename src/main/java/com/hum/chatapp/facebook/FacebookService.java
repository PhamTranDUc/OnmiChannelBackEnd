package com.hum.chatapp.facebook;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.hum.chatapp.constants.AuthenToken;
import com.hum.chatapp.dto.MessageRequest;
import com.hum.chatapp.dto.UserFacebookResponse;
import com.hum.chatapp.entity.Conversation;
import com.hum.chatapp.entity.Message;
import com.hum.chatapp.entity.User;
import com.hum.chatapp.repository.ConversationRepository;
import com.hum.chatapp.repository.MessageRepository;
import com.hum.chatapp.repository.UserRepository;
import com.hum.chatapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class FacebookService {



    private final UserRepository userRepository;

    private final ConversationRepository conversationRepository;
    private  final UserService userService;


    private final MessageRepository messageRepository;

    public List<BoxChat> getListAllBoxChatFacebookByPageId() throws JsonProcessingException {
        // String accessToken="EAAaTklbb2ZC0BOygonqtZBO1AThZADPZAg0TCk4Y9nTBEwj6v9iRA2JMBSTgiF2gyBkudiEO2pYFPZBBVv2ix5YYQciXTtZAo97UlRPUUgRwLgmn26ZCys0ZAIG6vxlneyj9yII6ks9FvaHb7XMxqL6FbLXziaMZAcYx2okOP5vTVKdI1uQOyo1zC5XOUlAk2IWQqOhISLo0hJDZCdxPeit8auRp5f8QZDZD";
        String url = "https://graph.facebook.com/v19.0/299553599899354/conversations?platform=MESSENGER&access_token=";
        url=url+ AuthenToken.ACCESS_TOKEN;
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<ResponseListBoxChat> responseEntity = restTemplate.getForEntity(url, ResponseListBoxChat.class);
        List<BoxChat> listBoxChat= new ArrayList<>();
        for(ResponseListBoxChat.DataEntry dataEntry: responseEntity.getBody().getData()){
            BoxChat boxChat= new BoxChat();
            boxChat.setId(dataEntry.getId());
            ResponseInforBoxChat responseInforBoxChat= getInforBoxChatById(dataEntry.getId());
           ResponseInforBoxChat.DataEntry dataEntryInforBoxChat= responseInforBoxChat.getMessages().getDataInfor();
            boxChat.setSenderId(dataEntryInforBoxChat.getFrom().getId());
            boxChat.setUserName(dataEntryInforBoxChat.getFrom().getName());
            boxChat.setNamePage(dataEntryInforBoxChat.getTo().getData().get(0).getName());
            boxChat.setRecipientId(dataEntryInforBoxChat.getTo().getData().get(0).getId());
            listBoxChat.add(boxChat);
        }
        return listBoxChat;
    }

    public ResponseInforBoxChat getInforBoxChatById(String id) throws JsonProcessingException {
        String url="https://graph.facebook.com/v19.0/"+id;
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("fields", "messages{message,from,to,id}")
                .queryParam("access_token", AuthenToken.ACCESS_TOKEN);

        HttpHeaders headers = new HttpHeaders();

        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<ResponseInforBoxChat> response = restTemplate.exchange(
                builder.build().encode().toUri(),
                HttpMethod.GET,
                entity,
                ResponseInforBoxChat.class);
        return response.getBody();
    }


    public User saveUserForFacebookId(String id){
        String url="https://graph.facebook.com/"+id+"?fields=name,picture&access_token="+AuthenToken.ACCESS_TOKEN;
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<UserFacebookResponse> responseEntity = restTemplate.getForEntity(url, UserFacebookResponse.class);
        User user= new User(responseEntity.getBody().getId(),responseEntity.getBody().getName(),responseEntity.getBody().getPicture().getData().getUrl());
       return userRepository.save(user);
    }

    public void sendMessagesByConversationId(MessageRequest messageRequest){
        User customer=userService.findUserById(messageRequest.getReceiverId());
        User page= userService.findUserById(messageRequest.getSenderId());
        Conversation conversation=conversationRepository.findConversationByUsers(customer,page).get();
        Message message= Message.builder()
                .message(messageRequest.getMessage())
                .sender(page)
                .receiver(customer)
                .conversation(conversation)
                .timestamp(LocalDateTime.now())
                .build();
        messageRepository.save(message);
    }

    public void sendMessageToFaceBookById(String id,String messenger){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(AuthenToken.ACCESS_TOKEN);

        String SEND_API_URL="https://graph.facebook.com/v19.0/me/messages";
        String requestBody = "{"
                + "\"recipient\":{\"id\":\"" + id + "\"},"
                + "\"message\":{\"text\":\""+ messenger+ "\"}"
                + "}";
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(SEND_API_URL, request, String.class);
    }

}
