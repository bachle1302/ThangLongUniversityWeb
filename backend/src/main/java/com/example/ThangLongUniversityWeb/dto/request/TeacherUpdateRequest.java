package com.example.ThangLongUniversityWeb.dto.request;

import com.example.ThangLongUniversityWeb.enums.TeacherStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Yêu cầu cập nhật thông tin giảng viên (không cần mật khẩu)")
public class TeacherUpdateRequest {

    @Schema(description = "Email")
    private String email;

    @Schema(description = "Họ tên")
    private String fullName;

    @Schema(description = "Ngày sinh")
    private LocalDate dob;

    @Schema(description = "Giới tính")
    private String gender;

    @Schema(description = "Số điện thoại")
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

    @Schema(description = "Học vị")
    private String degree;

    @Schema(description = "Địa chỉ (tổng quát)")
    private String address;

    @Schema(description = "Trạng thái")
    private TeacherStatus status;
}
