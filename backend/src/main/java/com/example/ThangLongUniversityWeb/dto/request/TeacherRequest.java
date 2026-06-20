package com.example.ThangLongUniversityWeb.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
@Schema(description = "Yêu cầu tạo/cập nhật giảng viên")
public class TeacherRequest {
    @Schema(
            description = "Tên đăng nhập",
            example = "teacher001",
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
            example = "teacher@thanglong.edu.vn",
            required = true
    )
    @NotBlank(message = "Email không được để trống")
    private String email;

    @Schema(
            description = "Mã giảng viên",
            example = "GV001",
            required = true
    )
    @NotBlank(message = "Mã giảng viên không được để trống")
    private String teacherCode;
    
    @Schema(
            description = "Họ tên",
            example = "Thầy Nguyễn Văn B",
            required = true
    )
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;
    
    @Schema(
            description = "Ngày sinh",
            example = "1985-05-15"
    )
    private LocalDate dob;
    
    @Schema(
            description = "ID khoa/bộ môn",
            example = "1"
    )
    private Long departmentId;
    
    @Schema(
            description = "Học vị",
            example = "Thạc sĩ"
    )
    private String degree;
    
    @Schema(
            description = "Địa chỉ",
            example = "456 Đường XYZ, Hà Nội"
    )
    private String address;
    
    @Schema(
            description = "Số điện thoại",
            example = "0987654321"
    )
    private String phone;
}