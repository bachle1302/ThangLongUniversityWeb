package com.example.ThangLongUniversityWeb.controller;

import com.example.ThangLongUniversityWeb.entity.AcademicResult;
import com.example.ThangLongUniversityWeb.entity.User;
import com.example.ThangLongUniversityWeb.repository.AcademicResultRepository;
import com.example.ThangLongUniversityWeb.repository.UserRepository;
import com.example.ThangLongUniversityWeb.service.AcademicResultService;
import com.example.ThangLongUniversityWeb.service.GradeLockingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/academic-results")
@RequiredArgsConstructor
@Tag(name = "Admin - Quản lý Kết quả Học tập", description = "Các API admin tính toán GPA/CPA")
@SecurityRequirement(name = "bearerAuth")
public class AdminAcademicResultController {

    private final AcademicResultService academicResultService;
    private final GradeLockingService gradeLockingService;
    private final AcademicResultRepository academicResultRepository;
    private final UserRepository userRepository;

    /**
     * Tính GPA cho một học kỳ của sinh viên
     */
    @Operation(summary = "Tính GPA học kỳ cho sinh viên")
    @PostMapping("/calculate-semester-gpa")
    public ResponseEntity<?> calculateSemesterGPA(@RequestParam Long studentId, @RequestParam Long semesterId) {
        // Kiểm tra quyền Admin
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user!"));

        if (currentUser.getRole() != com.example.ThangLongUniversityWeb.enums.Role.ADMIN) {
            return ResponseEntity.status(403).body("Chỉ Admin mới có quyền tính GPA!");
        }

        AcademicResult result = academicResultService.calculateSemesterGPA(studentId, semesterId);
        return ResponseEntity.ok(result);
    }

    /**
     * Tính CPA tích lũy cho sinh viên
     */
    @Operation(summary = "Tính CPA tích lũy cho sinh viên")
    @PostMapping("/calculate-cumulative-gpa")
    public ResponseEntity<?> calculateCumulativeGPA(@RequestParam Long studentId) {
        // Kiểm tra quyền Admin
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user!"));

        if (currentUser.getRole() != com.example.ThangLongUniversityWeb.enums.Role.ADMIN) {
            return ResponseEntity.status(403).body("Chỉ Admin mới có quyền tính CPA!");
        }

        AcademicResult result = academicResultService.calculateCumulativeGPA(studentId);
        return ResponseEntity.ok(result);
    }

    /**
     * Khóa điểm cho tất cả lớp trong học kỳ và tự động tính GPA
     */
    @Operation(summary = "Khóa điểm toàn bộ học kỳ và tính GPA")
    @PostMapping("/lock-semester-grades/{semesterId}")
    public ResponseEntity<?> lockSemesterGrades(@PathVariable Long semesterId) {
        // Kiểm tra quyền Admin
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user!"));

        if (currentUser.getRole() != com.example.ThangLongUniversityWeb.enums.Role.ADMIN) {
            return ResponseEntity.status(403).body("Chỉ Admin mới có quyền khóa điểm!");
        }

        gradeLockingService.lockAllGradesInSemester(semesterId);
        return ResponseEntity.ok("Đã khóa điểm toàn bộ học kỳ và bắt đầu tính GPA/CPA!");
    }

    /**
     * Lấy kết quả học tập của sinh viên
     */
    @Operation(summary = "Xem kết quả học tập của sinh viên")
    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getStudentAcademicResults(@PathVariable Long studentId) {
        List<AcademicResult> results = academicResultRepository.findByStudentIdOrderBySemesterDesc(studentId);
        return ResponseEntity.ok(results);
    }
}
