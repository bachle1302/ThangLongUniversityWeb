package com.example.ThangLongUniversityWeb.controller;

import com.example.ThangLongUniversityWeb.dto.request.AttendanceRecordRequest;
import com.example.ThangLongUniversityWeb.dto.response.AttendanceSessionResponse;
import com.example.ThangLongUniversityWeb.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/classes/{classSectionId}/attendance-sessions")
@RequiredArgsConstructor
@Tag(name = "Teacher - Điểm danh", description = "Quản lý điểm danh theo buổi học")
@SecurityRequirement(name = "bearerAuth")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Operation(summary = "Lấy danh sách buổi điểm danh của lớp")
    @GetMapping
    public ResponseEntity<List<AttendanceSessionResponse>> getSessions(
            @PathVariable Long classSectionId) {
        return ResponseEntity.ok(attendanceService.getSessions(classSectionId));
    }

    @Operation(summary = "Lấy hoặc tạo buổi điểm danh theo số thứ tự")
    @GetMapping("/{sessionNumber}")
    public ResponseEntity<AttendanceSessionResponse> getSession(
            @PathVariable Long classSectionId,
            @PathVariable Integer sessionNumber) {
        return ResponseEntity.ok(attendanceService.getOrCreateSession(classSectionId, sessionNumber));
    }

    @Operation(summary = "Lưu hàng loạt bản ghi điểm danh cho một buổi")
    @PutMapping("/{sessionNumber}/records")
    public ResponseEntity<AttendanceSessionResponse> saveRecords(
            @PathVariable Long classSectionId,
            @PathVariable Integer sessionNumber,
            @RequestBody List<AttendanceRecordRequest> records) {
        return ResponseEntity.ok(attendanceService.saveRecords(classSectionId, sessionNumber, records));
    }

    @Operation(summary = "Khoá buổi điểm danh, không cho sửa thêm")
    @PostMapping("/{sessionNumber}/lock")
    public ResponseEntity<AttendanceSessionResponse> lockSession(
            @PathVariable Long classSectionId,
            @PathVariable Integer sessionNumber) {
        return ResponseEntity.ok(attendanceService.lockSession(classSectionId, sessionNumber));
    }
}
