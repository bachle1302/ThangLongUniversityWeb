package com.example.ThangLongUniversityWeb.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Thông tin lớp hành chính")
public class HomeroomResponse {
    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "Tên lớp", example = "CNTT-K36A")
    private String className;

    @Schema(description = "ID cố vấn học tập", example = "5")
    private Long advisorId;

    @Schema(description = "Mã giảng viên cố vấn", example = "GV001")
    private String advisorCode;

    @Schema(description = "Tên cố vấn học tập", example = "ThS. Nguyễn Văn A")
    private String advisorName;

    @Schema(description = "ID ngành học", example = "1")
    private Long majorId;

    @Schema(description = "Tên ngành học", example = "Công nghệ thông tin")
    private String majorName;

    @Schema(description = "Niên khóa", example = "2024")
    private Integer academicYear;

    @Schema(description = "Khóa", example = "K36")
    private String cohort;

    @Schema(description = "Số sinh viên trong lớp", example = "35")
    private Long studentCount;

    @Schema(description = "Trạng thái hoạt động", example = "true")
    private Boolean isActive;
}
