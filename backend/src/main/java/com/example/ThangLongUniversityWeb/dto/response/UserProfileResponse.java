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
@Schema(description = "Thông tin hồ sơ người dùng")
public class UserProfileResponse {
    @Schema(description = "Tên đăng nhập", example = "student001")
    private String username;

    @Schema(description = "Email", example = "student@thanglong.edu.vn")
    private String email;

    @Schema(description = "Vai trò người dùng", example = "STUDENT", allowableValues = {"STUDENT", "TEACHER", "ADMIN"})
    private String role;

    @Schema(description = "Họ tên", example = "Nguyễn Văn A")
    private String fullName;

    @Schema(description = "Mã sinh viên hoặc mã giảng viên", example = "SV001")
    private String code;

    @Schema(description = "Ngành học (SV) hoặc Học vị (GV)", example = "Công nghệ thông tin")
    private String majorOrDegree;

    @Schema(description = "URL ảnh đại diện")
    private String avatarUrl;

    // --- Thông tin cá nhân ---
    @Schema(description = "Giới tính", example = "Nam")
    private String gender;

    @Schema(description = "Ngày sinh (ISO date)", example = "2004-09-12")
    private String dateOfBirth;

    @Schema(description = "Tuổi", example = "21")
    private Integer age;

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

    @Schema(description = "Số điện thoại", example = "0987654321")
    private String phone;

    @Schema(description = "Liên hệ khẩn cấp", example = "Nguyễn Văn B - 0912345678")
    private String emergencyContact;

    // --- Thông tin học vụ (Student) ---
    @Schema(description = "Khóa học", example = "K36")
    private String cohort;

    @Schema(description = "Lớp hành chính", example = "CNTT-K36A")
    private String className;

    @Schema(description = "Niên khóa", example = "2022 - 2026")
    private String academicYear;

    @Schema(description = "Cố vấn học tập", example = "ThS. Nguyễn Minh Hoàng")
    private String advisor;

    @Schema(description = "Trạng thái sinh viên", example = "Đang học")
    private String status;

    @Schema(description = "Hệ đào tạo", example = "Đại học chính quy")
    private String trainingType;

    // --- Thông tin chuyên môn (Teacher) ---
    @Schema(description = "Khoa/Bộ môn", example = "Khoa Công nghệ thông tin")
    private String department;
}
