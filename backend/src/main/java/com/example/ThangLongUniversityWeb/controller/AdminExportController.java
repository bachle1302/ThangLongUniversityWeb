package com.example.ThangLongUniversityWeb.controller;

import com.example.ThangLongUniversityWeb.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/export")
@RequiredArgsConstructor
@Tag(name = "Admin - Export", description = "Xuất dữ liệu ra Excel")
@SecurityRequirement(name = "bearerAuth")
public class AdminExportController {

    private final ExportService exportService;

    @Operation(summary = "Xuất danh sách đăng ký học phần theo học kỳ ra Excel")
    @GetMapping("/enrollments/semester/{semesterId}")
    public ResponseEntity<byte[]> exportEnrollments(@PathVariable Long semesterId) {
        byte[] data = exportService.exportEnrollmentsToExcel(semesterId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"enrollments-semester-" + semesterId + ".xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @Operation(summary = "Xuất lịch thi theo học kỳ ra Excel")
    @GetMapping("/exam-schedules/semester/{semesterId}")
    public ResponseEntity<byte[]> exportExamSchedules(@PathVariable Long semesterId) {
        byte[] data = exportService.exportExamSchedulesToExcel(semesterId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"exam-schedules-semester-" + semesterId + ".xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @Operation(summary = "Xuất danh sách đăng ký thi lại theo học kỳ ra Excel")
    @GetMapping("/retakes/semester/{semesterId}")
    public ResponseEntity<byte[]> exportRetakes(@PathVariable Long semesterId) {
        byte[] data = exportService.exportRetakesToExcel(semesterId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"retakes-semester-" + semesterId + ".xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }
}
