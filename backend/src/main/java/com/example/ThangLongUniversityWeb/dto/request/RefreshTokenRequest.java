package com.example.ThangLongUniversityWeb.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu lấy Access Token mới")
public class RefreshTokenRequest {
    @Schema(
            description = "Refresh Token để lấy Access Token mới",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            required = true
    )
    @NotBlank(message = "Refresh Token không được để trống")
    private String refreshToken;
}
