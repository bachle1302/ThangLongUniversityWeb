package com.example.ThangLongUniversityWeb.dto.request;

import com.example.ThangLongUniversityWeb.enums.ConversationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO để client tạo hoặc cập nhật phòng chat
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomRequest {
    
    @Schema(description = "Tên phòng chat", example = "Lớp SE1901")
    private String name;

    @Schema(description = "Mô tả phòng chat", example = "Phòng chat cho lớp SE1901")
    private String description;

    @Schema(description = "Loại phòng: PRIVATE (1-1), GROUP, CLASS_GROUP", example = "GROUP")
    @Builder.Default
    private ConversationType type = ConversationType.PRIVATE;

    @Schema(description = "URL ảnh đại diện")
    private String avatarUrl;

    @Schema(description = "Danh sách ID của các thành viên cần thêm vào phòng (bỏ qua cho PRIVATE)")
    private List<Long> memberIds;

    /**
     * Cho chat 1-1: chứa ID của người nhận
     * Cho group: chứa danh sách ID thành viên
     */
    @Schema(description = "ID của người bạn muốn chat (dành cho PRIVATE type)")
    private Long recipientId;
}
