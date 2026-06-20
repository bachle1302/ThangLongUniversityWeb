package com.example.ThangLongUniversityWeb.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu đổi mật khẩu")
public class ChangePasswordRequest {

    @Schema(description = "Mật khẩu hiện tại", required = true)
    @NotBlank(message = "Mật khẩu hiện tại không được để trống")
    private String currentPassword;

    @Schema(description = "Mật khẩu mới", required = true, minLength = 6, maxLength = 100)
    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 6, max = 100, message = "Mật khẩu mới phải từ 6 đến 100 ký tự")
    private String newPassword;

    @Schema(description = "Xác nhận mật khẩu mới", required = true)
    @NotBlank(message = "Xác nhận mật khẩu mới không được để trống")
    private String confirmPassword;
}
