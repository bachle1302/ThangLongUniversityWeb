package com.example.ThangLongUniversityWeb.controller;

import com.example.ThangLongUniversityWeb.dto.request.MajorRequest;
import com.example.ThangLongUniversityWeb.service.MajorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/majors")
@RequiredArgsConstructor
@Tag(name = "Admin - Quản lý Ngành học", description = "Quản lý danh mục ngành học")
@SecurityRequirement(name = "bearerAuth")
public class MajorManagementController {

    private final MajorService majorService;

    @Operation(summary = "Lấy danh sách tất cả ngành học")
    @GetMapping
    public ResponseEntity<?> getAllMajors() {
        return ResponseEntity.ok(majorService.getAllMajors());
    }

    @Operation(summary = "Thêm mới một ngành học")
    @PostMapping
    public ResponseEntity<?> createMajor(@RequestBody MajorRequest request) {
        return ResponseEntity.ok(majorService.createMajor(request));
    }

    @Operation(summary = "Cập nhật thông tin ngành học")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMajor(@PathVariable Long id, @RequestBody MajorRequest request) {
        return ResponseEntity.ok(majorService.updateMajor(id, request));
    }

    @Operation(summary = "Xóa một ngành học")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMajor(@PathVariable Long id) {
        majorService.deleteMajor(id);
        return ResponseEntity.ok("Xóa ngành học thành công!");
    }
}
