package com.example.ThangLongUniversityWeb.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Interceptor để validate JWT token khi WebSocket handshake
 * Client cần gửi token qua query parameter: ws://localhost:8080/ws/chat?token=JWT_TOKEN
 * Hoặc qua header: Authorization: Bearer JWT_TOKEN
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtils jwtUtils;

    /**
     * Gọi trước khi WebSocket handshake
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            // Cách 1: Lấy token từ query parameter
            String token = null;
            var params = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams();
            token = params.getFirst("token");

            // Cách 2: Lấy token từ header Authorization (không thể trong ws://, nhưng có thể trong headers)
            if (token == null) {
                String authHeader = request.getHeaders().getFirst("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                }
            }

            // Validate token
            if (token == null || token.isEmpty()) {
                log.warn("❌ WebSocket handshake failed: Không có token");
                response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                return false;
            }

            // Extract username từ token
            String username = jwtUtils.extractUsername(token);
            if (username == null || !jwtUtils.isTokenValid(token, username)) {
                log.warn("❌ WebSocket handshake failed: Token không hợp lệ");
                response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                return false;
            }

            // Lưu username vào attributes để sử dụng sau
            attributes.put("username", username);
            log.info("✅ WebSocket handshake thành công: {}", username);

            return true;
        } catch (Exception e) {
            log.error("Lỗi WebSocket handshake: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Gọi sau khi WebSocket handshake thành công
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("❌ WebSocket connection failed: {}", exception.getMessage());
        }
    }
}
