package com.example.ThangLongUniversityWeb.dto.response;

import com.example.ThangLongUniversityWeb.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Thong tin tai khoan cho trang quan ly users cua admin")
public class AdminUserManagementResponse {
    @Schema(example = "1")
    private Long id;

    @Schema(example = "admin")
    private String username;

    @Schema(example = "admin@tlu.edu.vn")
    private String email;

    @Schema(example = "ADMIN")
    private Role role;

    @Schema(example = "true")
    private boolean active;

    @Schema(example = "Quan tri he thong")
    private String fullName;

    @Schema(example = "2")
    private Long profileId;

    @Schema(example = "2026-05-24T07:45:00")
    private LocalDateTime createdAt;

    @Schema(example = "2026-05-24T09:30:00")
    private LocalDateTime lastLoginAt;
}
