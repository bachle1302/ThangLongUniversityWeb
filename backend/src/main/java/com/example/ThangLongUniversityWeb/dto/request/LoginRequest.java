package com.example.ThangLongUniversityWeb.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu đăng nhập")
public class LoginRequest {

    @Schema(
            description = "Tên đăng nhập",
            example = "admin",
            required = true,
            minLength = 3,
            maxLength = 50
    )
    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 50, message = "Tên đăng nhập phải từ 3-50 ký tự")
    private String username;

    @Schema(
            description = "Mật khẩu",
            example = "password123",
            required = true,
            minLength = 6,
            maxLength = 100
    )
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 100, message = "Mật khẩu phải từ 6-100 ký tự")
    private String password;
}