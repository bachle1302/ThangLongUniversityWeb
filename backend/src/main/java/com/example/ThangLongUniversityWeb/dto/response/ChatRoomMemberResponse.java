package com.example.ThangLongUniversityWeb.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO về thành viên của một phòng chat
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomMemberResponse {
    
    @Schema(description = "ID của thành viên trong phòng chat")
    private Long id;

    @Schema(description = "ID người dùng")
    private Long userId;

    @Schema(description = "Username")
    private String username;
    private String code;

    @Schema(description = "Tên đầy đủ")
    private String fullName;

    @Schema(description = "URL ảnh đại diện")
    private String avatarUrl;

    @Schema(description = "Vai trò của người dùng: STUDENT, TEACHER, ADMIN")
    private String role;

    @Schema(description = "Thời điểm gia nhập phòng chat")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime joinedAt;

    @Schema(description = "Thời gian lần cuối xem phòng chat")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastReadAt;

    @Schema(description = "Số tin nhắn chưa đọc")
    private Integer unreadCount;

    @Schema(description = "Có đang online không")
    private Boolean isOnline;
}
