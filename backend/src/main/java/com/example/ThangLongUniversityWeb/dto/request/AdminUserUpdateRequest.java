package com.example.ThangLongUniversityWeb.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Yeu cau cap nhat tai khoan tu trang admin users")
public class AdminUserUpdateRequest {
    @NotBlank(message = "Ten dang nhap khong duoc de trong")
    @Size(min = 3, max = 50, message = "Ten dang nhap phai tu 3-50 ky tu")
    @Schema(example = "gv101")
    private String username;

    @NotBlank(message = "Email khong duoc de trong")
    @Email(message = "Email khong hop le")
    @Schema(example = "teacher1@tlu.edu.vn")
    private String email;

    @NotBlank(message = "Ho ten hien thi khong duoc de trong")
    @Size(max = 120, message = "Ho ten toi da 120 ky tu")
    @Schema(example = "Giang vien gv101")
    private String fullName;
}
