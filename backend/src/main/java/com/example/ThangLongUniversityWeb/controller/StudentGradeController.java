package com.example.ThangLongUniversityWeb.controller;

import com.example.ThangLongUniversityWeb.dto.response.GradeResponse;
import com.example.ThangLongUniversityWeb.entity.Student;
import com.example.ThangLongUniversityWeb.entity.User;
import com.example.ThangLongUniversityWeb.repository.UserRepository;
import com.example.ThangLongUniversityWeb.service.GradeService;
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
@RequestMapping("/api/student/grades")
@RequiredArgsConstructor
@Tag(name = "Student - Xem Bảng Điểm", description = "Các API xem bảng điểm của sinh viên")
@SecurityRequirement(name = "bearerAuth")
public class StudentGradeController {

    private final GradeService gradeService;
    private final UserRepository userRepository;

    /**
     * Lấy bảng điểm của sinh viên theo học kỳ
     */
    @Operation(summary = "Lấy bảng điểm theo học kỳ")
    @GetMapping("/semester/{semesterId}")
    public ResponseEntity<?> getGradesBySemester(@PathVariable Long semesterId) {
        // Lấy sinh viên hiện tại đang đăng nhập
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user!"));
        Student currentStudent = currentUser.getStudent();

        if (currentStudent == null) {
            return ResponseEntity.status(403).body("Bạn không phải là sinh viên!");
        }

        List<GradeResponse> grades = gradeService.getStudentGradesBySemester(currentStudent.getId(), semesterId);
        return ResponseEntity.ok(grades);
    }

    /**
     * Lấy bảng điểm của sinh viên tất cả kỳ
     */
    @Operation(summary = "Lấy bảng điểm tất cả học kỳ")
    @GetMapping("/my-grades")
    public ResponseEntity<?> getAllGrades() {
        // Lấy sinh viên hiện tại đang đăng nhập
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user!"));
        Student currentStudent = currentUser.getStudent();

        if (currentStudent == null) {
            return ResponseEntity.status(403).body("Bạn không phải là sinh viên!");
        }

        List<GradeResponse> grades = gradeService.getStudentAllGrades(currentStudent.getId());
        return ResponseEntity.ok(grades);
    }
}
