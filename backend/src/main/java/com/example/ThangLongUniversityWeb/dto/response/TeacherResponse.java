package com.example.ThangLongUniversityWeb.dto.response;

import com.example.ThangLongUniversityWeb.enums.TeacherStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin giảng viên")
public class TeacherResponse {

    private Long id;

    @Schema(description = "Tên đăng nhập")
    private String username;

    @Schema(description = "Email")
    private String email;

    @Schema(description = "Mã giảng viên", example = "GV001")
    private String teacherCode;

    @Schema(description = "Họ tên", example = "Nguyễn Văn A")
    private String fullName;

    @Schema(description = "Ngày sinh", example = "1985-05-15")
    private LocalDate dob;

    @Schema(description = "Giới tính", example = "Nam")
    private String gender;

    @Schema(description = "Số điện thoại", example = "0987654321")
    private String phone;

    @Schema(description = "Số CMND/CCCD")
    private String nationalId;

    @Schema(description = "Nơi sinh")
    private String placeOfBirth;

    @Schema(description = "Quê quán")
    private String hometown;

    @Schema(description = "Địa chỉ thường trú")
    private String permanentAddress;

    @Schema(description = "Địa chỉ hiện tại")
    private String currentAddress;

    @Schema(description = "Liên hệ khẩn cấp")
    private String emergencyContact;

    @Schema(description = "ID khoa/bộ môn", example = "1")
    private Long departmentId;

    @Schema(description = "Mã khoa/bộ môn", example = "CNTT")
    private String departmentCode;

    @Schema(description = "Tên khoa/bộ môn", example = "Khoa Công nghệ thông tin")
    private String departmentName;

    @Schema(description = "Học vị", example = "Thạc sĩ")
    private String degree;

    @Schema(description = "Địa chỉ (tổng quát)")
    private String address;

    @Schema(description = "Trạng thái", example = "DANG_GIANG_DAY")
    private TeacherStatus status;
}
