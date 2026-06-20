package com.example.ThangLongUniversityWeb.controller;

import com.example.ThangLongUniversityWeb.entity.ExamRegistration;
import com.example.ThangLongUniversityWeb.entity.ClassSection;
import com.example.ThangLongUniversityWeb.enums.EnrollmentStatus;
import com.example.ThangLongUniversityWeb.repository.ExamRegistrationRepository;
import com.example.ThangLongUniversityWeb.repository.ClassSectionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/exam-registrations")
@RequiredArgsConstructor
@Tag(name = "Admin - Quản lý Đăng ký Thi lại", description = "Xem và thống kê đăng ký thi lại/nâng điểm")
@SecurityRequirement(name = "bearerAuth")
public class AdminExamRegistrationController {

    private final ExamRegistrationRepository examRegistrationRepository;
    private final ClassSectionRepository classSectionRepository;

    @Operation(summary = "Danh sách đăng ký thi lại theo học kỳ (filter theo status)")
    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam Long semesterId,
            @RequestParam(required = false) String status
    ) {
        EnrollmentStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = EnrollmentStatus.valueOf(status);
            } catch (IllegalArgumentException ignored) {
            }
        }

        List<ExamRegistration> registrations = statusEnum != null
                ? examRegistrationRepository.findBySemesterIdAndStatus(semesterId, statusEnum)
                : examRegistrationRepository.findBySemesterId(semesterId);

        List<Map<String, Object>> result = registrations.stream().map(r -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", r.getId());
            item.put("studentId", r.getStudent().getId());
            item.put("studentCode", r.getStudent().getStudentCode());
            item.put("studentName", r.getStudent().getFullName());
            var classSection = r.getClassSection();
            var course = r.getCourse() != null ? r.getCourse() : (classSection != null ? classSection.getCourse() : null);
            var semester = r.getSemester() != null ? r.getSemester() : (classSection != null ? classSection.getSemester() : null);
            if (course != null) {
                item.put("courseName", course.getName());
                item.put("courseCode", course.getCode());
                item.put("credits", course.getCredits());
            }
            if (semester != null) {
                item.put("semesterId", semester.getId());
                item.put("semesterName", semester.getName());
            }
            item.put("classSectionId", classSection != null ? classSection.getId() : null);
            item.put("classCode", classSection != null ? classSection.getClassCode() : null);
            item.put("classAssigned", classSection != null);
            item.put("status", r.getStatus() != null ? r.getStatus().name() : null);
            item.put("registrationType", r.getRegistrationType() != null ? r.getRegistrationType().name() : null);
            item.put("feeCharged", r.getFeeCharged());
            item.put("attemptNumber", r.getAttemptNumber());
            item.put("examAt", classSection != null && classSection.getExamAt() != null ? classSection.getExamAt().toString() : null);
            item.put("examRoom", classSection != null ? classSection.getExamRoom() : null);
            item.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : null);
            return item;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Tổng hợp đăng ký thi lại theo học kỳ")
    @GetMapping("/semester/{semesterId}/summary")
    public ResponseEntity<?> summary(@PathVariable Long semesterId) {
        List<ExamRegistration> all = examRegistrationRepository.findBySemesterId(semesterId);

        long pending = all.stream().filter(r -> r.getStatus() == EnrollmentStatus.PENDING).count();
        long registered = all.stream().filter(r -> r.getStatus() == EnrollmentStatus.REGISTERED).count();
        long totalFee = all.stream()
                .filter(r -> r.getFeeCharged() != null)
                .mapToLong(ExamRegistration::getFeeCharged)
                .sum();

        Map<String, Object> summary = new HashMap<>();
        summary.put("semesterId", semesterId);
        summary.put("total", all.size());
        summary.put("pending", pending);
        summary.put("registered", registered);
        summary.put("totalFeeCharged", totalFee);

        return ResponseEntity.ok(summary);
    }

    @Operation(summary = "Cap nhat lop hoc phan gan cho sinh vien thi lai")
    @PutMapping("/{registrationId}/class-section")
    public ResponseEntity<?> updateClassSection(
            @PathVariable Long registrationId,
            @RequestParam Long classSectionId
    ) {
        ExamRegistration reg = examRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đăng ký thi lại."));
        ClassSection cs = classSectionRepository.findById(classSectionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học phần."));

        var course = reg.getCourse() != null ? reg.getCourse() : (reg.getClassSection() != null ? reg.getClassSection().getCourse() : null);
        var semester = reg.getSemester() != null ? reg.getSemester() : (reg.getClassSection() != null ? reg.getClassSection().getSemester() : null);

        if (course == null || !cs.getCourse().getId().equals(course.getId())) {
            throw new RuntimeException("Lớp học phần không thuộc môn học này.");
        }
        if (semester == null || !cs.getSemester().getId().equals(semester.getId())) {
            throw new RuntimeException("Lớp học phần không thuộc học kỳ này.");
        }

        reg.setClassSection(cs);
        examRegistrationRepository.save(reg);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("classCode", cs.getClassCode());
        response.put("classSectionId", cs.getId());
        return ResponseEntity.ok(response);
    }
}
