package com.example.ThangLongUniversityWeb.controller;

import com.example.ThangLongUniversityWeb.dto.request.AdminUserUpdateRequest;
import com.example.ThangLongUniversityWeb.dto.request.AdminResetPasswordRequest;
import com.example.ThangLongUniversityWeb.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users") // KHÁC với /api/users nhé
@RequiredArgsConstructor
@Tag(name = "Admin - Quản lý Tài khoản (User)", description = "Các API quản lý tài khoản hệ thống")
@SecurityRequirement(name = "bearerAuth")
public class UserManagementController {

    private final UserService userService;       // Gọi qua Service để Ghi

    @Operation(summary = "Lấy danh sách TOÀN BỘ tài khoản trong hệ thống")
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.listUsersForAdmin());
    }

    @Operation(summary = "Tạo thêm một Admin mới")
    @PostMapping("/admin")
    public ResponseEntity<?> createAdmin(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email) {
        return ResponseEntity.ok(userService.createAdmin(username, password, email));
    }

    @Operation(summary = "Khóa hoặc Mở khóa tài khoản (Ban/Unban)")
    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id) {
        return ResponseEntity.ok(userService.toggleUserStatus(id));
    }

    @Operation(summary = "Cap nhat thong tin tai khoan")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody @Valid AdminUserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @Operation(summary = "Xóa tài khoản Admin")
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> deleteAdminUser(@PathVariable Long id) {
        userService.deleteAdminUser(id);
        return ResponseEntity.ok("Đã xóa Admin thành công!");
    }

    @Operation(summary = "Admin reset mật khẩu cho người dùng")
    @PutMapping("/{id}/reset-password")
    public ResponseEntity<?> resetPassword(
            @PathVariable Long id,
            @RequestBody @Valid AdminResetPasswordRequest request) {
        userService.resetUserPassword(id, request.getNewPassword());
        return ResponseEntity.ok(java.util.Map.of("message", "Đã reset mật khẩu thành công!"));
    }
}