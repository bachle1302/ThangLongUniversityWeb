package com.example.ThangLongUniversityWeb.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin phòng học")
public class RoomResponse {
    @Schema(
            description = "ID của phòng",
            example = "1"
    )
    private Long id;
    
    @Schema(
            description = "Tên phòng",
            example = "A301"
    )
    private String name;
    
    @Schema(
            description = "Sức chứa",
            example = "60"
    )
    private Integer capacity;

    @Schema(
            description = "Loai phong",
            example = "LECTURE"
    )
    private String type;

    @Schema(
            description = "Trang thai phong",
            example = "AVAILABLE"
    )
    private String status;
}
