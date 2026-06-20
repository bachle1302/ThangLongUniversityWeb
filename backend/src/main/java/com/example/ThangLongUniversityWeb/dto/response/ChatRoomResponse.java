package com.example.ThangLongUniversityWeb.dto.response;

import com.example.ThangLongUniversityWeb.enums.ConversationType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO để server trả về thông tin phòng chat
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomResponse {
    
    @Schema(description = "ID của phòng chat")
    private Long id;

    @Schema(description = "Tên phòng chat")
    private String name;

    @Schema(description = "Mô tả phòng chat")
    private String description;

    @Schema(description = "Loại phòng: PRIVATE, GROUP, CLASS_GROUP")
    private ConversationType type;

    @Schema(description = "URL ảnh đại diện")
    private String avatarUrl;

    @Schema(description = "ID của người tạo phòng")
    private Long creatorId;

    @Schema(description = "Username của người tạo")
    private String creatorUsername;

    @Schema(description = "Nội dung tin nhắn cuối cùng (preview)")
    private String lastMessagePreview;

    @Schema(description = "Người gửi tin nhắn cuối cùng")
    private String lastMessageSender;

    @Schema(description = "Thời gian tin nhắn cuối cùng")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastMessageTime;

    @Schema(description = "Số lượng thành viên hiện tại")
    private Integer memberCount;

    @Schema(description = "Số tin nhắn chưa đọc")
    private Integer unreadCount;

    @Schema(description = "Danh sách thành viên")
    private List<ChatRoomMemberResponse> members;

    @Schema(description = "Thời gian tạo phòng")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Trạng thái phòng (active hoặc đã xóa)")
    private Boolean isActive;
}
