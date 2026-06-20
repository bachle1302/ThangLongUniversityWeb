package com.example.ThangLongUniversityWeb.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu tạo/cập nhật phòng học")
public class RoomRequest {
    @Schema(
            description = "Tên phòng",
            example = "A301",
            required = true
    )
    @NotBlank(message = "Tên phòng không được để trống")
    private String name;
    
    @Schema(
            description = "Sức chứa của phòng",
            example = "60",
            required = true,
            minimum = "1"
    )
    @NotNull(message = "Sức chứa không được để trống")
    @Min(value = 1, message = "Sức chứa phải lớn hơn 0")
    private Integer capacity;

    @Schema(
            description = "Loai phong: LECTURE, LAB, AUDITORIUM",
            example = "LECTURE"
    )
    private String type;

    @Schema(
            description = "Trang thai phong: AVAILABLE, MAINTENANCE",
            example = "AVAILABLE"
    )
    private String status;
}
