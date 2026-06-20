package com.example.ThangLongUniversityWeb.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Phản hồi xác thực")
public class AuthResponse {

    @Schema(
            description = "Access token JWT",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String accessToken;

    @Schema(
            description = "Refresh token để lấy access token mới",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String refreshToken;

    @Schema(
            description = "Vai trò của người dùng",
            example = "ADMIN",
            allowableValues = {"ADMIN", "STUDENT", "TEACHER"}
    )
    private String role;
}
