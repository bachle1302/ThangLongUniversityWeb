package com.example.ThangLongUniversityWeb.controller;

import com.example.ThangLongUniversityWeb.entity.AcademicResult;
import com.example.ThangLongUniversityWeb.entity.Student;
import com.example.ThangLongUniversityWeb.entity.User;
import com.example.ThangLongUniversityWeb.repository.AcademicResultRepository;
import com.example.ThangLongUniversityWeb.repository.UserRepository;
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
@RequestMapping("/api/student/academic-results")
@RequiredArgsConstructor
@Tag(name = "Student - Xem Kết quả Học tập", description = "API sinh viên xem GPA/CPA")
@SecurityRequirement(name = "bearerAuth")
public class StudentAcademicResultController {

    private final AcademicResultRepository academicResultRepository;
    private final UserRepository userRepository;

    /**
     * Sinh viên xem kết quả học tập của mình
     */
    @Operation(summary = "Xem kết quả học tập của tôi")
    @GetMapping("/my-results")
    public ResponseEntity<?> getMyAcademicResults() {
        // Lấy sinh viên hiện tại đang đăng nhập
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user!"));

        Student currentStudent = currentUser.getStudent();
        if (currentStudent == null) {
            return ResponseEntity.status(403).body("Bạn không phải là sinh viên!");
        }

        List<AcademicResult> results = academicResultRepository.findByStudentIdOrderBySemesterDesc(currentStudent.getId());
        return ResponseEntity.ok(results);
    }
}
