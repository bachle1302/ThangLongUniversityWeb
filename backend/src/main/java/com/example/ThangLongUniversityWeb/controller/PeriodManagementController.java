package com.example.ThangLongUniversityWeb.controller;

import com.example.ThangLongUniversityWeb.dto.request.PeriodRequest;
import com.example.ThangLongUniversityWeb.service.PeriodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/periods")
@RequiredArgsConstructor
@Tag(name = "Admin - Quản lý Tiết học", description = "Quản lý định nghĩa các tiết học")
@SecurityRequirement(name = "bearerAuth")
public class PeriodManagementController {

    private final PeriodService periodService;

    @Operation(summary = "Lấy danh sách tất cả tiết học")
    @GetMapping
    public ResponseEntity<?> getAllPeriods() {
        return ResponseEntity.ok(periodService.getAllPeriods());
    }

    @Operation(summary = "Thêm mới một tiết học")
    @PostMapping
    public ResponseEntity<?> createPeriod(@RequestBody PeriodRequest request) {
        return ResponseEntity.ok(periodService.createPeriod(request));
    }

    @Operation(summary = "Cập nhật một tiết học")
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePeriod(@PathVariable Long id, @RequestBody PeriodRequest request) {
        return ResponseEntity.ok(periodService.updatePeriod(id, request));
    }

    @Operation(summary = "Xóa một tiết học")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePeriod(@PathVariable Long id) {
        periodService.deletePeriod(id);
        return ResponseEntity.ok("Xóa tiết học thành công!");
    }
}