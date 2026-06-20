package com.example.ThangLongUniversityWeb.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
@Schema(description = "Yêu cầu tạo/cập nhật sinh viên")
public class StudentRequest {
    @Schema(
            description = "Tên đăng nhập",
            example = "student001",
            required = true
    )
    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 50, message = "Tên đăng nhập phải từ 3-50 ký tự")
    private String username;
    
    @Schema(
            description = "Mật khẩu (chỉ bắt buộc khi tạo mới)",
            example = "password123",
            required = true
    )
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 100, message = "Mật khẩu phải từ 6-100 ký tự")
    private String password;
    
    @Schema(
            description = "Email",
            example = "student@thanglong.edu.vn",
            required = true
    )
    @NotBlank(message = "Email không được để trống")
    private String email;

    @Schema(
            description = "Mã sinh viên",
            example = "SV001",
            required = true
    )
    @NotBlank(message = "Mã sinh viên không được để trống")
    private String studentCode;
    
    @Schema(
            description = "Họ tên",
            example = "Nguyễn Văn A",
            required = true
    )
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;
    
    @Schema(
            description = "Ngày sinh",
            example = "2000-01-15",
            required = true
    )
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
    
    @Schema(
            description = "ID ngành học",
            example = "1",
            required = true
    )
    private Long majorId;
    
    @Schema(
            description = "Năm học",
            example = "2022",
            required = true
    )
    private Integer academicYear;

    @Schema(description = "Khóa", example = "K36")
    private String cohort;

    @Schema(description = "ID lớp hành chính (không bắt buộc, có thể gán sau)", example = "1")
    private Long homeroomId;

    @Schema(description = "Trạng thái sinh viên", example = "Đang học")
    private String status;

    @Schema(description = "Hệ đào tạo", example = "Đại học chính quy")
    private String trainingType;
    
    @Schema(
            description = "Địa chỉ",
            example = "123 Đường ABC, Hà Nội"
    )
    private String address;
}
