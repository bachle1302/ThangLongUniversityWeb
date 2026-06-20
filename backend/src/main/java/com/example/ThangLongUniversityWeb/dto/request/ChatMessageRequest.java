package com.example.ThangLongUniversityWeb.dto.request;

import com.example.ThangLongUniversityWeb.enums.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO để client gửi tin nhắn qua WebSocket hoặc REST API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageRequest {
    
    @Schema(description = "ID của phòng chat", example = "1")
    private Long chatRoomId;

    @Schema(description = "Nội dung tin nhắn", example = "Xin chào!")
    private String content;

    @Schema(description = "Loại tin nhắn: TEXT, IMAGE, FILE", example = "TEXT")
    @Builder.Default
    private MessageType type = MessageType.TEXT;

    @Schema(description = "URL của media (nếu loại là IMAGE hoặc FILE)", example = "https://example.com/image.jpg")
    private String mediaUrl;

    private String fileName;

    private Long fileSize;
}
