package com.example.ThangLongUniversityWeb.controller;

import com.example.ThangLongUniversityWeb.dto.request.ChatMessageRequest;
import com.example.ThangLongUniversityWeb.dto.request.TypingIndicatorMessage;
import com.example.ThangLongUniversityWeb.dto.response.ChatMessageResponse;
import com.example.ThangLongUniversityWeb.service.ChatMessageService;
import com.example.ThangLongUniversityWeb.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * WebSocket Message Handler cho Real-time Chat
 * 
 * Các endpoint:
 * - /app/chat/send -> gửi tin nhắn
 * - /app/chat/{chatRoomId}/typing -> gửi typing indicator
 * - /topic/chatroom/{chatRoomId} -> nhận tin nhắn từ phòng
 * - /topic/users/online -> trạng thái online
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final CustomUserDetailsService userDetailsService;

    /**
     * Gửi tin nhắn qua WebSocket
     * Client gửi POST: /app/chat/send với payload ChatMessageRequest
     * Server broadcast đến /topic/chatroom/{chatRoomId}
     */
    @MessageMapping("/chat/send")
    public ChatMessageResponse sendMessage(@Payload ChatMessageRequest request, 
                                          SimpMessageHeaderAccessor headerAccessor) {
        try {
            // Lấy username từ session attributes (được set ở handshake)
            String username = (String) headerAccessor.getSessionAttributes().get("username");
            log.info("📨 Nhận tin nhắn từ user: {}, Phòng: {}", username, request.getChatRoomId());

            // IMPORTANT: Handler runs on executor threads; don't rely on channel interceptor ThreadLocal.
            // Force SecurityContext for this message based on handshake username.
            if (username == null || username.isBlank()) {
                throw new RuntimeException("WebSocket session chưa có username (handshake/auth lỗi)");
            }
            var userDetails = userDetailsService.loadUserByUsername(username);
            var auth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Lưu tin nhắn vào database
            ChatMessageResponse response = chatMessageService.sendMessage(request);

            // Broadcast đúng room destination (không dùng @SendTo với template var)
            messagingTemplate.convertAndSend("/topic/chatroom/" + request.getChatRoomId(), response);
            return response;
        } catch (Exception e) {
            log.error("❌ Lỗi khi gửi tin nhắn: {}", e.getMessage());
            throw new RuntimeException("Gửi tin nhắn thất bại: " + e.getMessage());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * Gửi typing indicator
     * Khi client gõ tin nhắn, gửi: /app/chat/{chatRoomId}/typing
     * Broadcast đến: /topic/chatroom/{chatRoomId}/typing
     */
    @MessageMapping("/chat/{chatRoomId}/typing")
    public void sendTypingIndicator(@DestinationVariable Long chatRoomId,
                                    @Payload TypingIndicatorMessage typingMessage,
                                    SimpMessageHeaderAccessor headerAccessor) {
        try {
            String username = (String) headerAccessor.getSessionAttributes().get("username");
            typingMessage.setUsername(username);

            log.debug("⌨️ User {} đang gõ trong phòng {}", username, chatRoomId);

            // Broadcast typing indicator đến phòng
            messagingTemplate.convertAndSend("/topic/chatroom/" + chatRoomId + "/typing", typingMessage);
        } catch (Exception e) {
            log.error("Lỗi typing indicator: {}", e.getMessage());
        }
    }

    /**
     * Đánh dấu tin nhắn là đã đọc
     * Client gửi: /app/chat/{chatRoomId}/read
     */
    @MessageMapping("/chat/{chatRoomId}/read")
    public void markMessagesAsRead(@DestinationVariable Long chatRoomId,
                                   SimpMessageHeaderAccessor headerAccessor) {
        try {
            String username = (String) headerAccessor.getSessionAttributes().get("username");
            log.info("✅ User {} đã xem phòng chat {}", username, chatRoomId);

            // Cập nhật vào database (nếu cần)
            // chatMessageService.markAsRead(...);
        } catch (Exception e) {
            log.error("Lỗi mark as read: {}", e.getMessage());
        }
    }

    /**
     * User online/offline
     * Khi connect: /app/chat/user/online
     * Broadcast đến: /topic/users/online
     */
    @MessageMapping("/chat/user/online")
    public void userOnline(SimpMessageHeaderAccessor headerAccessor) {
        try {
            String username = (String) headerAccessor.getSessionAttributes().get("username");
            String sessionId = headerAccessor.getSessionId();

            log.info("👋 User {} vừa online, SessionID: {}", username, sessionId);

            // Broadcast user online status
            messagingTemplate.convertAndSend("/topic/users/online", 
                new UserStatusMessage(username, "online", System.currentTimeMillis()));
        } catch (Exception e) {
            log.error("Lỗi user online: {}", e.getMessage());
        }
    }

    /**
     * Lớp trợ để gửi trạng thái user
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserStatusMessage {
        private String username;
        private String status;  // online, offline, idle
        private Long timestamp;
    }
}
