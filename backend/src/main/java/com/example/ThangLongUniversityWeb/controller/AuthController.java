package com.example.ThangLongUniversityWeb.controller;

import com.example.ThangLongUniversityWeb.dto.request.LoginRequest;
import com.example.ThangLongUniversityWeb.dto.request.RefreshTokenRequest;
import com.example.ThangLongUniversityWeb.dto.request.ChangePasswordRequest;
import com.example.ThangLongUniversityWeb.dto.response.AuthResponse;
import com.example.ThangLongUniversityWeb.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor // Constructor Injection
@Tag(name = "Authentication", description = "API xác thực người dùng")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Đăng nhập hệ thống",
            description = "Đăng nhập bằng username và password để nhận JWT tokens"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Đăng nhập thành công",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                              "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                              "role": "ADMIN"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Sai thông tin đăng nhập",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2024-01-01T10:00:00",
                                              "message": "Invalid username or password",
                                              "status": 401
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dữ liệu đầu vào không hợp lệ",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2024-01-01T10:00:00",
                                              "message": "Validation failed",
                                              "errors": ["username: Tên đăng nhập không được để trống"],
                                              "status": 400
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin đăng nhập",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "username": "admin",
                                              "password": "password123"
                                            }
                                            """
                            )
                    )
            )
            @RequestBody LoginRequest loginRequest) {
        // Gọi service xử lý
        AuthResponse response = authService.login(loginRequest);
        // Trả refresh token trong body để FE tự set cookie/local storage theo nhu cầu
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Làm mới token", description = "Dùng refresh token để lấy access token mới")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody(required = false) RefreshTokenRequest request
    ) {
        String refreshToken = extractRefreshToken(authorizationHeader, null, request);
        AuthResponse response = authService.refreshToken(refreshToken);
        // Rotation: response đã chứa refreshToken mới trong body
        return ResponseEntity.ok(response);
    }

    private String extractRefreshToken(String authorizationHeader, String refreshTokenCookie, RefreshTokenRequest request) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7).trim();
        }

        if (refreshTokenCookie != null && !refreshTokenCookie.isBlank()) {
            return refreshTokenCookie;
        }

        if (request != null) {
            return request.getRefreshToken();
        }

        return null;
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody(required = false) RefreshTokenRequest request
    ) {
        String refreshToken = extractRefreshToken(authorizationHeader, null, request);

        authService.logout(refreshToken);

        return ResponseEntity.ok("Logout successful");
    }

    @Operation(summary = "Đổi mật khẩu", description = "Đổi mật khẩu cho người dùng hiện tại")
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody @jakarta.validation.Valid ChangePasswordRequest request) {
        org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        authService.changePassword(username, request);
        return ResponseEntity.ok(java.util.Map.of("message", "Đổi mật khẩu thành công. Vui lòng đăng nhập lại."));
    }
}
