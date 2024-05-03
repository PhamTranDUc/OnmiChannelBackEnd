
package com.hum.chatapp.controller;
import com.hum.chatapp.dto.ApiResponse;
import com.hum.chatapp.dto.ConversationResponse;
import com.hum.chatapp.dto.LoginRequest;
import com.hum.chatapp.entity.User;
import com.hum.chatapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller class that handles user-related HTTP requests and interactions.
 * Routes:
 * - POST /user/register: Register a new user in the system.
 * - POST /user/login: Login a user based on their email address.
 * - GET /user/all: Retrieve a list of all users in the system.
 * - GET /user/except/{userId}: Retrieve a list of all users except the user with a specific user ID.
 * - GET /user/conversation/id: Find or create a conversation ID for a pair of users based on their user IDs.
 */
@RequiredArgsConstructor
@CrossOrigin("*")
@RestController
@RequestMapping("/user")
public class UserController {
    final UserService userService;

    /**
     * Register a new user in the system.
     *
     * @param user The User object representing the user to be registered.
     * @return ResponseEntity containing an ApiResponse indicating the result of the registration.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody User user) {
        return userService.saveUser(user);
    }

    /**
     * Login a user based on their email address.
     *
     * @param loginRequest The LoginRequest object containing the user's email for login.
     * @return ResponseEntity containing an ApiResponse with user information if the login is successful.
     */
    @PostMapping("/login")
    public User login(@RequestBody LoginRequest loginRequest) {
        return userService.findUserById(loginRequest.getId());
    }

    @GetMapping("/login/test")
    public User login2(@RequestParam(value = "id") String id) {
        return userService.findUserById(id);
    }

    /**
     * Retrieve a list of all users in the system.
     *
     * @return ResponseEntity containing an ApiResponse with a list of User objects representing all users.
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse> findAllUsers() {
        return userService.findAllUsers();
    }

    /**
     * Retrieve a list of all users except the user with a specific user ID.
     *
     * @param userId The ID of the user to be excluded from the list.
     * @return ResponseEntity containing an ApiResponse with a list of User objects representing all users except the specified user.
     */
    @GetMapping("/except/{userId}")
    public ResponseEntity<ApiResponse> findAllUsersExceptThisUserId(@PathVariable String userId) {
        return userService.findAllUsersExceptThisUserId(userId);
    }

    @GetMapping("/boxchat/{userId}")
    public ResponseEntity<ApiResponse> getAllBoxChat(@PathVariable String userId) {
//        List<ConversationResponse> listConversationResponse= userService.getListConversationResponse(userId);
        return userService.getListConversationResponse(userId);

    }

    /**
     * Find or create a conversation ID for a pair of users based on their user IDs.
     *
     * @param user1Id The ID of the first user in the conversation.
     * @param user2Id The ID of the second user in the conversation.
     * @return ResponseEntity containing an ApiResponse with the conversation ID for the user pair.
     */
    @GetMapping("/conversation/id")
    public ResponseEntity<ApiResponse> findConversationIdByUser1IdAndUser2Id(@RequestParam String user1Id, @RequestParam String user2Id) {
        return userService.findConversationIdByUser1IdAndUser2Id(user1Id, user2Id);
    }





}