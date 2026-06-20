package com.example.ThangLongUniversityWeb.controller;

import com.example.ThangLongUniversityWeb.dto.request.GradeRequest;
import com.example.ThangLongUniversityWeb.service.DashboardService;
import com.example.ThangLongUniversityWeb.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@Tag(name = "Giảng Viên - Quản lý lớp & Chấm điểm")
@SecurityRequirement(name = "bearerAuth")
public class TeacherController {

    private final TeacherService teacherService;
    private final DashboardService dashboardService;

    @Operation(summary = "Du lieu tong hop cho teacher dashboard")
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.ok(dashboardService.getTeacherDashboard(authentication.getName()));
    }

    @Operation(summary = "Lay danh sach hoc ky cho giang vien")
    @GetMapping("/semesters")
    public ResponseEntity<?> getSemesters() {
        return ResponseEntity.ok(teacherService.getSemesters());
    }

    @Operation(summary = "Xem danh sách các lớp được phân công dạy trong học kỳ")
    @GetMapping("/my-classes/semester/{semesterId}")
    public ResponseEntity<?> getMyClasses(@PathVariable Long semesterId) {
        return ResponseEntity.ok(teacherService.getMyClasses(semesterId));
    }

    @Operation(summary = "Xem danh sách sinh viên trong 1 lớp học phần")
    @GetMapping("/classes/{classSectionId}/students")
    public ResponseEntity<?> getStudentsInClass(@PathVariable Long classSectionId) {
        return ResponseEntity.ok(teacherService.getStudentsInClass(classSectionId));
    }

    @Operation(summary = "Nhập điểm / Cập nhật điểm cho sinh viên")
    @PutMapping("/enrollments/{enrollmentId}/grade")
    public ResponseEntity<?> gradeStudent(
            @PathVariable Long enrollmentId,
            @RequestBody GradeRequest request) {
        return ResponseEntity.ok(teacherService.gradeStudent(enrollmentId, request));
    }
}
