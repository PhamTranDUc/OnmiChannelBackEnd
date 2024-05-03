package com.hum.chatapp.repository;

import com.hum.chatapp.dto.ConversationResponse;
import com.hum.chatapp.entity.Conversation;
import com.hum.chatapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Integer> {

    @Query("SELECT c FROM Conversation c WHERE (c.user1 = :user1 AND c.user2 = :user2) OR (c.user1 = :user2 AND c.user2 = :user1)")
    Optional<Conversation> findConversationByUsers(@Param("user1") User user1, @Param("user2") User user2);

//    @Query(
//            nativeQuery = true,
//            value = """
//                    SELECT
//                        C.conversation_id AS conversationId,
//                        U.id AS otherUserId,
//                        U.name AS otherUserName,
//                        M.message AS lastMessage,
//                        M.timestamp AS lastMessageTimestamp
//                    FROM conversation AS C
//
//                    INNER JOIN user AS U
//                    ON (C.user1_id = U.id OR C.user2_id = U.id) AND U.id != ?1
//
//                    LEFT JOIN (
//                        SELECT
//                            conversation_id,
//                            (SELECT message FROM message M2 WHERE M2.conversation_id = M.conversation_id ORDER BY M2.timestamp DESC LIMIT 1) AS message,
//                            MAX(timestamp) AS timestamp
//                        FROM message M
//                        GROUP BY conversation_id
//                    ) AS M
//                    ON C.conversation_id = M.conversation_id
//
//                    WHERE C.user1_id = ?1 OR C.user2_id = ?1
//                    ORDER BY M.timestamp DESC;
//                    """
//    )
//    List<ConversationResponse> findConversationsByUserId(String userId);
//@Query(
//        nativeQuery = true,
//        value = """
//            SELECT
//                C.conversation_id AS conversationId,
//                U.id AS otherUserId,
//                U.name AS otherUserName,
//                U.image AS otherUserImage,  -- Thêm cột hình ảnh của người dùng
//                M.message AS lastMessage,
//                M.timestamp AS lastMessageTimestamp
//            FROM conversation AS C
//
//            INNER JOIN user AS U
//            ON (C.user1_id = U.id OR C.user2_id = U.id) AND U.id != ?1
//
//            LEFT JOIN (
//                SELECT
//                    conversation_id,
//                    (SELECT message FROM message M2 WHERE M2.conversation_id = M.conversation_id ORDER BY M2.timestamp DESC LIMIT 1) AS message,
//                    MAX(timestamp) AS timestamp
//                FROM message M
//                GROUP BY conversation_id
//            ) AS M
//            ON C.conversation_id = M.conversation_id
//
//            WHERE C.user1_id = ?1 OR C.user2_id = ?1
//            ORDER BY M.timestamp DESC;
//            """
//               )
//           List<ConversationResponse> findConversationsByUserId(String userId);

    @Query(
            nativeQuery = true,
            value = """
        SELECT
            C.conversation_id AS conversationId,
            U.id AS otherUserId,
            U.name AS otherUserName,
            U.image AS otherUserImage,
            U2.name AS pageName, -- Thêm cột name của user
            M.message AS lastMessage,
            M.timestamp AS lastMessageTimestamp
        FROM conversation AS C
                                    
        INNER JOIN user AS U
        ON (C.user1_id = U.id OR C.user2_id = U.id) AND U.id != ?1
                                    
        LEFT JOIN user AS U2 -- Thêm INNER JOIN với bảng user để lấy thông tin user
        ON (C.user2_id = U2.id OR C.user1_id = U2.id) AND U2.id = ?1
                                    
        LEFT JOIN (
            SELECT
                conversation_id,
                (SELECT message FROM message M2 WHERE M2.conversation_id = M.conversation_id ORDER BY M2.timestamp DESC LIMIT 1) AS message,
                MAX(timestamp) AS timestamp
            FROM message M
            GROUP BY conversation_id
        ) AS M
        ON C.conversation_id = M.conversation_id
                                    
        WHERE C.user1_id = ?1 OR C.user2_id = ?1
        ORDER BY M.timestamp DESC;
    """
    )
    List<ConversationResponse> findConversationsByUserId(String userId);


}
