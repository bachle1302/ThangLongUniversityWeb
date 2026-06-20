package com.example.ThangLongUniversityWeb.controller;

import com.example.ThangLongUniversityWeb.dto.request.AdminOverrideEnrollmentRequest;
import com.example.ThangLongUniversityWeb.enums.EnrollmentStatus;
import com.example.ThangLongUniversityWeb.service.AdminEnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/enrollments")
@RequiredArgsConstructor
@Tag(name = "Admin - Enrollments")
@SecurityRequirement(name = "bearerAuth")
public class AdminEnrollmentController {
    private final AdminEnrollmentService adminEnrollmentService;

    @Operation(summary = "Danh sách đăng ký (pagination + filter)")
    @GetMapping
    public ResponseEntity<?> search(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Long classSectionId,
            @RequestParam(required = false) EnrollmentStatus status,
            Pageable pageable
    ) {
        Page<?> page = adminEnrollmentService.search(semesterId, classSectionId, status, pageable);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Admin override đăng ký (vượt sĩ số / bỏ qua trùng lịch)")
    @PostMapping("/override")
    public ResponseEntity<?> override(@RequestBody AdminOverrideEnrollmentRequest request) {
        return ResponseEntity.ok(adminEnrollmentService.overrideEnrollment(request));
    }

    @Operation(summary = "Admin khoa/chot danh sach dang ky dang chon trong hoc ky")
    @PostMapping("/lock-semester/{semesterId}")
    public ResponseEntity<?> lockSemester(@PathVariable Long semesterId) {
        int lockedCount = adminEnrollmentService.lockPendingEnrollments(semesterId);
        return ResponseEntity.ok("Da chot " + lockedCount + " dang ky cho hoc ky " + semesterId + ".");
    }

    @Operation(summary = "Admin khoa/chot danh sach dang ky thi lai dang chon trong hoc ky")
    @PostMapping("/lock-retakes/{semesterId}")
    public ResponseEntity<?> lockRetakes(@PathVariable Long semesterId) {
        int lockedCount = adminEnrollmentService.lockPendingRetakes(semesterId);
        return ResponseEntity.ok("Da chot " + lockedCount + " dang ky thi lai/nang diem cho hoc ky " + semesterId + ".");
    }
}
