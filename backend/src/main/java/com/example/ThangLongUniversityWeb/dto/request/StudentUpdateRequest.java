package com.example.ThangLongUniversityWeb.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDate;

/**
 * Request DTO dùng cho cập nhật sinh viên (không yêu cầu password).
 */
@Data
@Schema(description = "Yêu cầu cập nhật thông tin sinh viên")
public class StudentUpdateRequest {

    @Schema(description = "Email", example = "student@thanglong.edu.vn")
    private String email;

    @Schema(description = "Họ tên", example = "Nguyễn Văn A")
    private String fullName;

    @Schema(description = "Ngày sinh", example = "2000-01-15")
    private LocalDate dob;

    @Schema(description = "Giới tính", example = "Nam")
    private String gender;

    @Schema(description = "Số điện thoại", example = "0987654321")
    private String phone;

    @Schema(description = "Số CCCD/CMND", example = "001204000789")
    private String nationalId;

    @Schema(description = "Nơi sinh", example = "Hà Nội")
    private String placeOfBirth;

    @Schema(description = "Quê quán", example = "Thanh Trì, Hà Nội")
    private String hometown;

    @Schema(description = "Địa chỉ thường trú")
    private String permanentAddress;

    @Schema(description = "Nơi ở hiện tại")
    private String currentAddress;

    @Schema(description = "Liên hệ khẩn cấp", example = "Nguyễn Văn B - 0912345678")
    private String emergencyContact;

    @Schema(description = "ID ngành học", example = "1")
    private Long majorId;

    @Schema(description = "Năm học", example = "2022")
    private Integer academicYear;

    @Schema(description = "Khóa", example = "K36")
    private String cohort;

    @Schema(description = "ID lớp hành chính (gán hoặc chuyển lớp)", example = "1")
    private Long homeroomId;

    @Schema(description = "Trạng thái sinh viên", example = "Đang học")
    private String status;

    @Schema(description = "Hệ đào tạo", example = "Đại học chính quy")
    private String trainingType;

    @Schema(description = "Địa chỉ", example = "123 Đường ABC, Hà Nội")
    private String address;
}
