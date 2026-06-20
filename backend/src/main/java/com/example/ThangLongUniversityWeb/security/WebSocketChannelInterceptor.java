package com.example.ThangLongUniversityWeb.security;

import com.example.ThangLongUniversityWeb.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Ensure SecurityContext is available for each inbound STOMP message.
 * This is required because WebSocket traffic doesn't go through JwtAuthenticationFilter.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private final CustomUserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // Only for STOMP frames that represent a client message.
        if (accessor.getCommand() == null && accessor.getMessageType() != SimpMessageType.MESSAGE) {
            return message;
        }

        try {
            Object usernameObj = accessor.getSessionAttributes() != null
                    ? accessor.getSessionAttributes().get("username")
                    : null;
            String username = usernameObj instanceof String ? (String) usernameObj : null;

            if (username != null) {
                var userDetails = userDetailsService.loadUserByUsername(username);
                var auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                // For debugging: you can enable this if needed
                // log.debug("No username in WS session for command={}", accessor.getCommand());
            }
        } catch (Exception e) {
            log.warn("WebSocketChannelInterceptor auth set failed: {}", e.getMessage());
        }

        return message;
    }
}

