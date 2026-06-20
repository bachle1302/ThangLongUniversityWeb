package com.example.ThangLongUniversityWeb.controller;

import com.example.ThangLongUniversityWeb.dto.request.GradeRequest;
import com.example.ThangLongUniversityWeb.dto.response.GradeResponse;
import com.example.ThangLongUniversityWeb.entity.ClassSection;
import com.example.ThangLongUniversityWeb.entity.Enrollment;
import com.example.ThangLongUniversityWeb.entity.ExamRegistration;
import com.example.ThangLongUniversityWeb.entity.Teacher;
import com.example.ThangLongUniversityWeb.entity.User;
import com.example.ThangLongUniversityWeb.repository.ClassSectionRepository;
import com.example.ThangLongUniversityWeb.repository.EnrollmentRepository;
import com.example.ThangLongUniversityWeb.repository.ExamRegistrationRepository;
import com.example.ThangLongUniversityWeb.repository.UserRepository;
import com.example.ThangLongUniversityWeb.entity.ExamRegistration;
import com.example.ThangLongUniversityWeb.enums.CourseStudyStatus;
import com.example.ThangLongUniversityWeb.enums.EnrollmentStatus;
import com.example.ThangLongUniversityWeb.service.GradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/grades")
@RequiredArgsConstructor
@Tag(name = "Teacher - Quản lý Điểm", description = "Các API nhập điểm cho sinh viên")
@SecurityRequirement(name = "bearerAuth")
public class TeacherGradeController {

    private final GradeService gradeService;
    private final EnrollmentRepository enrollmentRepository;
    private final ClassSectionRepository classSectionRepository;
    private final UserRepository userRepository;
    private final ExamRegistrationRepository examRegistrationRepository;

    @Operation(summary = "Nhập/cập nhật điểm cho sinh viên")
    @PutMapping("/{enrollmentId}")
    @Transactional
    public ResponseEntity<?> updateStudentGrade(
            @PathVariable Long enrollmentId,
            @RequestBody GradeRequest request) {

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy enrollment!"));

        ClassSection originalClassSection = enrollment.getClassSection();
        Teacher currentTeacher = getCurrentTeacher();

        List<ExamRegistration> activeRegistrations = examRegistrationRepository
                .findActiveByOriginalEnrollmentId(enrollmentId, EnrollmentStatus.REGISTERED);

        boolean isAuthorized = false;
        boolean isClosed = false;

        for (ExamRegistration reg : activeRegistrations) {
            ClassSection gradingSection = reg.getClassSection();
            if (gradingSection != null
                    && gradingSection.getTeacher() != null
                    && gradingSection.getTeacher().getId().equals(currentTeacher.getId())) {
                isAuthorized = true;
                isClosed = gradingSection.getStatus() == com.example.ThangLongUniversityWeb.enums.ClassSectionStatus.CANCELLED
                        || gradingSection.isGradeLocked();
                if (!isClosed) {
                    break;
                }
            }
        }

        if (!isAuthorized && originalClassSection.getTeacher().getId().equals(currentTeacher.getId())) {
            if (activeRegistrations.isEmpty()) {
                isAuthorized = true;
                isClosed = originalClassSection.getStatus() == com.example.ThangLongUniversityWeb.enums.ClassSectionStatus.CANCELLED
                        || originalClassSection.isGradeLocked();
            }
        }

        if (!isAuthorized) {
            return ResponseEntity.status(403).body("Bạn không có quyền nhập điểm cho enrollment này!");
        }

        if (isClosed) {
            return ResponseEntity.badRequest().body("Lớp đã khóa điểm hoặc bị hủy, không thể nhập điểm!");
        }

        CourseStudyStatus courseStatus = enrollment.getCourseStatus();
        if (courseStatus == CourseStudyStatus.BANNED_FROM_EXAM) {
            return ResponseEntity.badRequest().body("Sinh viên bị cấm thi do nghỉ quá buổi, không thể cập nhật điểm.");
        }
        if (courseStatus == CourseStudyStatus.REPEAT_COURSE) {
            return ResponseEntity.badRequest().body("Sinh viên phải học lại, không thể cập nhật điểm.");
        }

        request.setEnrollmentId(enrollmentId);
        GradeResponse response = gradeService.updateGrade(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lấy bảng điểm của lớp")
    @GetMapping("/class/{classSectionId}")
    public ResponseEntity<?> getClassGrades(@PathVariable Long classSectionId) {
        ClassSection classSection = classSectionRepository.findById(classSectionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp!"));

        Teacher currentTeacher = getCurrentTeacher();

        if (!classSection.getTeacher().getId().equals(currentTeacher.getId())) {
            return ResponseEntity.status(403).body("Bạn không phải là giảng viên dạy lớp này!");
        }

        List<GradeResponse> grades = gradeService.getClassSectionGrades(classSectionId);
        return ResponseEntity.ok(grades);
    }

    @Operation(summary = "Khóa toàn bộ điểm của một lớp học phần")
    @PostMapping("/class/{classSectionId}/lock")
    @Transactional
    public ResponseEntity<?> lockClassGrades(@PathVariable Long classSectionId) {
        ClassSection classSection = classSectionRepository.findById(classSectionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp!"));

        Teacher currentTeacher = getCurrentTeacher();

        if (!classSection.getTeacher().getId().equals(currentTeacher.getId())) {
            return ResponseEntity.status(403).body("Bạn không phải là giảng viên dạy lớp này!");
        }

        classSection.setGradeLocked(true);
        classSectionRepository.save(classSection);
        return ResponseEntity.ok("Đã khóa điểm lớp " + classSectionId);
    }

    private Teacher getCurrentTeacher() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user!"));
        Teacher currentTeacher = currentUser.getTeacher();
        if (currentTeacher == null) {
            throw new RuntimeException("Bạn không phải là giảng viên!");
        }
        return currentTeacher;
    }
}
