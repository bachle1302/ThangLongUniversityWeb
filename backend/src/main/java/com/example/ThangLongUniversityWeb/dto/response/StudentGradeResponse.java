package com.example.ThangLongUniversityWeb.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Thong tin diem va sinh vien trong lop hoc phan")
public class StudentGradeResponse {
    @Schema(description = "ID ghi danh", example = "1")
    private Long enrollmentId;

    @Schema(description = "Ma sinh vien", example = "SV001")
    private String studentCode;

    @Schema(description = "Ten sinh vien", example = "Nguyen Van A")
    private String fullName;

    private String phone;

    private String email;

    private String className;

    private String advisorName;

    private String majorName;

    private String facultyName;

    @Schema(description = "Diem giua ky (0-10)", example = "7.5")
    private Float midTermScore;

    @Schema(description = "Diem cuoi ky (0-10)", example = "8.0")
    private Float finalScore;

    @Schema(description = "Tong diem", example = "7.75")
    private Float totalScore;

    @Schema(description = "Trang thai", example = "PASSED", allowableValues = {"REGISTERED", "PASSED", "FAILED", "CANCELED"})
    private String status;

    @Schema(description = "Trang thai ket qua hoc mon", example = "IN_PROGRESS")
    private String courseStatus;

    @Schema(description = "So buoi vang", example = "2")
    private Long absenceCount;
}
