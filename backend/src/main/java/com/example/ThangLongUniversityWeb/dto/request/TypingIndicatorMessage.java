package com.example.ThangLongUniversityWeb.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO để gửi thông báo khi người dùng đang gõ tin nhắn (Typing indicator)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypingIndicatorMessage {
    
    @Schema(description = "ID của phòng chat")
    private Long chatRoomId;

    @Schema(description = "Username của người đang gõ")
    private String username;

    @Schema(description = "Tên đầy đủ của người đang gõ")
    private String fullName;

    @Schema(description = "Loại hành động: 'typing' hoặc 'stop_typing'", example = "typing")
    private String action;
}
