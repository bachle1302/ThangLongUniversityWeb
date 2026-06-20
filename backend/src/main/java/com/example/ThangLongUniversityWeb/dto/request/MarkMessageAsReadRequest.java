package com.example.ThangLongUniversityWeb.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO để đánh dấu tin nhắn/phòng chat là đã đọc
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarkMessageAsReadRequest {
    
    @Schema(description = "ID của phòng chat", example = "1")
    private Long chatRoomId;

    @Schema(description = "ID của tin nhắn cuối cùng đã đọc (nếu null thì đánh dấu toàn bộ phòng)")
    private Long messageId;
}
