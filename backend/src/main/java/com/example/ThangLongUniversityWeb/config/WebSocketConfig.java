package com.example.ThangLongUniversityWeb.config;

import com.example.ThangLongUniversityWeb.security.WebSocketHandshakeInterceptor;
import com.example.ThangLongUniversityWeb.security.WebSocketChannelInterceptor;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Cấu hình WebSocket STOMP cho Real-time Chat
 * 
 * Kiến trúc:
 * - Client kết nối qua: ws://localhost:8080/ws/chat
 * - Subscribe để nhận tin nhắn: /topic/chatroom/{chatRoomId}
 * - Send tin nhắn đến: /app/chat/send
 * - Online status: /topic/users/online
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketHandshakeInterceptor handshakeInterceptor;
    private final WebSocketChannelInterceptor channelInterceptor;

    @Value("${app.cors.allowed-origins:http://localhost:*,http://127.0.0.1:*,http://localhost:3000,http://localhost:8080}")
    private String allowedOrigins;

    /**
     * Cấu hình Message Broker:
     * - /topic: để broadcast tin nhắn (pub-sub pattern)
     * - /queue: để gửi tin nhắn private cho một user
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Simple Message Broker (dùng cho development)
        // Cho production, cân nhắc dùng RabbitMQ hoặc Redis broker
        config.enableSimpleBroker("/topic", "/queue")
                .setTaskScheduler(heartbeatScheduler())
                .setHeartbeatValue(new long[]{25000, 25000});

        // Prefix cho các message được gửi từ client đến server
        config.setApplicationDestinationPrefixes("/app");

        // Cho phép gửi tin nhắn riêng tư đến user: /user/username/queue/messages
        config.setUserDestinationPrefix("/user");
    }

    /**
     * Đăng ký STOMP endpoint - nơi client kết nối
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String[] origins = java.util.Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(o -> !o.isBlank())
                .toArray(String[]::new);

        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns(origins)
                .addInterceptors(handshakeInterceptor)
                .withSockJS()
                .setHeartbeatTime(25000);
    }

    /**
     * Cấu hình Channel - nơi xử lý các incoming messages
     * Thêm interceptor để validate JWT token trên mỗi message
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(channelInterceptor);
    }
    @Bean
    public TaskScheduler heartbeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1); // Với chat đơn giản, 1 thread là đủ
        scheduler.setThreadNamePrefix("ws-heartbeat-thread-");
        scheduler.initialize(); // QUAN TRỌNG: Phải có dòng này để tránh lỗi IllegalStateException
        return scheduler;
    }       
}
