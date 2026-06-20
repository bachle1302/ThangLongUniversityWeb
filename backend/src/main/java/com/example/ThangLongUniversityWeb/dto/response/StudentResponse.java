package com.example.ThangLongUniversityWeb.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@Schema(description = "Thông tin sinh viên")
public class StudentResponse {
    @Schema(description = "ID của sinh viên", example = "1")
    private Long id;
    
    @Schema(description = "Tên đăng nhập", example = "student001")
    private String username;
    
    @Schema(description = "Email", example = "student@thanglong.edu.vn")
    private String email;
    
    @Schema(description = "Mã sinh viên", example = "SV001")
    private String studentCode;
    
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
    
    @Schema(description = "Địa chỉ", example = "123 Đường ABC, Hà Nội")
    private String address;
    
    @Schema(description = "Năm học", example = "2022")
    private Integer academicYear;

    @Schema(description = "Khóa", example = "K36")
    private String cohort;

    // --- Homeroom info (thay thế className & advisor String) ---
    @Schema(description = "ID lớp hành chính", example = "1")
    private Long homeroomId;

    @Schema(description = "Tên lớp hành chính", example = "CNTT-K36A")
    private String className;

    @Schema(description = "ID cố vấn học tập", example = "5")
    private Long advisorId;

    @Schema(description = "Tên cố vấn học tập", example = "ThS. Nguyễn Minh Hoàng")
    private String advisorName;

    @Schema(description = "Mã giảng viên cố vấn", example = "GV001")
    private String advisorCode;

    @Schema(description = "Trạng thái sinh viên", example = "Đang học")
    private String status;

    @Schema(description = "Hệ đào tạo", example = "Đại học chính quy")
    private String trainingType;
    
    @Schema(description = "ID ngành học", example = "1")
    private Long majorId;
    
    @Schema(description = "Tên ngành học", example = "Công nghệ thông tin")
    private String majorName;
    
    @Schema(description = "Mã ngành học", example = "CNTT")
    private String majorCode;

    @Schema(description = "GPA học kỳ gần nhất", example = "3.50")
    private Float gpa;

    @Schema(description = "CPA tích lũy", example = "3.40")
    private Float cpa;

    @Schema(description = "Tổng tín chỉ tích lũy", example = "72")
    private Integer totalCredits;
}
