package com.example.ThangLongUniversityWeb.controller;

import com.example.ThangLongUniversityWeb.dto.request.DepartmentRequest;
import com.example.ThangLongUniversityWeb.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/departments")
@RequiredArgsConstructor
@Tag(name = "Admin - Quản lý Khoa/Bộ môn", description = "Quản lý danh mục khoa/bộ môn")
@SecurityRequirement(name = "bearerAuth")
public class DepartmentManagementController {

    private final DepartmentService departmentService;

    @Operation(summary = "Lấy danh sách tất cả khoa/bộ môn")
    @GetMapping
    public ResponseEntity<?> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @Operation(summary = "Thêm mới một khoa/bộ môn")
    @PostMapping
    public ResponseEntity<?> createDepartment(@RequestBody DepartmentRequest request) {
        return ResponseEntity.ok(departmentService.createDepartment(request));
    }

    @Operation(summary = "Cập nhật thông tin khoa/bộ môn")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDepartment(@PathVariable Long id, @RequestBody DepartmentRequest request) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, request));
    }

    @Operation(summary = "Xóa một khoa/bộ môn")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok("Xóa khoa/bộ môn thành công!");
    }
}
