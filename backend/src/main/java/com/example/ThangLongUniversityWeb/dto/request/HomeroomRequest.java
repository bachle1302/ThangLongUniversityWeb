package com.example.ThangLongUniversityWeb.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu tạo/cập nhật lớp hành chính")
public class HomeroomRequest {
    @Schema(description = "Tên lớp hành chính", example = "CNTT-K36A", required = true)
    @NotBlank(message = "Tên lớp không được để trống")
    private String className;

    @Schema(description = "ID cố vấn học tập (giảng viên)", example = "5")
    private Long advisorId;

    @Schema(description = "ID ngành học", example = "1")
    private Long majorId;

    @Schema(description = "Niên khóa", example = "2024")
    private Integer academicYear;

    @Schema(description = "Khóa", example = "K36")
    private String cohort;
}
