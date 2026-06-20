package com.example.ThangLongUniversityWeb.dto.response;

import com.example.ThangLongUniversityWeb.enums.MessageType;
import com.example.ThangLongUniversityWeb.enums.MessageStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO để server trả về tin nhắn cho client
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponse {
    
    @Schema(description = "ID của tin nhắn")
    private Long id;

    @Schema(description = "ID của phòng chat")
    private Long chatRoomId;

    @Schema(description = "ID của người gửi")
    private Long senderId;

    @Schema(description = "Username của người gửi")
    private String senderUsername;
    private String senderCode;

    @Schema(description = "Tên đầy đủ của người gửi")
    private String senderFullName;

    @Schema(description = "Avatar URL của người gửi")
    private String senderAvatarUrl;

    @Schema(description = "Nội dung tin nhắn")
    private String content;

    @Schema(description = "Loại tin nhắn: TEXT, IMAGE, FILE")
    private MessageType type;

    @Schema(description = "Trạng thái tin nhắn: SENT, DELIVERED, READ")
    private MessageStatus status;

    @Schema(description = "URL của media (nếu loại là IMAGE hoặc FILE)")
    private String mediaUrl;

    private String fileName;

    private Long fileSize;

    @Schema(description = "Thời gian tạo tin nhắn")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Server timestamp (epoch millis) để sort ổn định trên FE")
    private Long createdAtEpochMs;

    @Schema(description = "Thời gian cập nhật lần cuối")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @Schema(description = "Server timestamp updatedAt (epoch millis)")
    private Long updatedAtEpochMs;
}
