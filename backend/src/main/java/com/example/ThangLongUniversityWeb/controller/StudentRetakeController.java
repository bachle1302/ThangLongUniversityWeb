package com.example.ThangLongUniversityWeb.controller;

import com.example.ThangLongUniversityWeb.dto.request.RetakeRegistrationRequest;
import com.example.ThangLongUniversityWeb.dto.response.StudentRetakeOverviewResponse;
import com.example.ThangLongUniversityWeb.dto.response.StudentSemesterResponse;
import com.example.ThangLongUniversityWeb.service.SemesterService;
import com.example.ThangLongUniversityWeb.service.StudentRetakeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/student/retakes")
@RequiredArgsConstructor
@Tag(name = "Student - Dang ky thi lai / thi nang diem")
@SecurityRequirement(name = "bearerAuth")
public class StudentRetakeController {

    private final StudentRetakeService studentRetakeService;
    private final SemesterService semesterService;

    @Operation(summary = "Tong hop du lieu dang ky thi lai / nang diem")
    @GetMapping("/overview")
    public ResponseEntity<?> getOverview(@RequestParam(required = false) Long semesterId) {
        List<StudentSemesterResponse> semesters = semesterService.getAllSemestersReadOnly();
        StudentSemesterResponse currentSemester = resolveSemester(semesters, semesterId);
        if (currentSemester == null) {
            return ResponseEntity.ok(StudentRetakeOverviewResponse.builder()
                    .semesters(semesters)
                    .eligibleCourses(List.of())
                    .requests(List.of())
                    .readonly(true)
                    .registrationStatus("Khong co hoc ky")
                    .build());
        }

        boolean readonly = !currentSemester.isRetakeOpen() || currentSemester.isRetakeLocked();
        return ResponseEntity.ok(StudentRetakeOverviewResponse.builder()
                .semesters(semesters)
                .currentSemester(currentSemester)
                .eligibleCourses(studentRetakeService.getEligibleCourses(currentSemester.getId()))
                .requests(studentRetakeService.getMyRequests(currentSemester.getId()))
                .readonly(readonly)
                .registrationStatus(readonly ? "Da dong" : "Dang mo")
                .build());
    }

    @Operation(summary = "Lay danh sach mon du dieu kien thi lai / thi nang diem")
    @GetMapping("/eligible-courses")
    public ResponseEntity<?> getEligibleCourses(@RequestParam(required = false) Long semesterId) {
        return ResponseEntity.ok(studentRetakeService.getEligibleCourses(semesterId));
    }

    @Operation(summary = "Dang ky thi lai / thi nang diem")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RetakeRegistrationRequest request) {
        return ResponseEntity.ok(studentRetakeService.register(request));
    }

    @Operation(summary = "Bo chon dang ky thi lai / thi nang diem dang PENDING")
    @DeleteMapping("/{examRegistrationId}")
    public ResponseEntity<?> cancel(@PathVariable Long examRegistrationId) {
        return ResponseEntity.ok(studentRetakeService.cancel(examRegistrationId));
    }

    @Operation(summary = "Lay danh sach dang ky thi lai / thi nang diem cua sinh vien")
    @GetMapping("/my-requests")
    public ResponseEntity<?> getMyRequests(@RequestParam(required = false) Long semesterId) {
        return ResponseEntity.ok(studentRetakeService.getMyRequests(semesterId));
    }

    private StudentSemesterResponse resolveSemester(List<StudentSemesterResponse> semesters, Long semesterId) {
        if (semesterId != null) {
            return semesters.stream()
                    .filter(semester -> Objects.equals(semester.getId(), semesterId))
                    .findFirst()
                    .orElse(null);
        }

        LocalDate today = LocalDate.now();
        return semesters.stream()
                .filter(semester -> semester.getStartDate() != null
                        && semester.getEndDate() != null
                        && !today.isBefore(semester.getStartDate())
                        && !today.isAfter(semester.getEndDate()))
                .findFirst()
                .or(() -> semesters.stream().filter(StudentSemesterResponse::isRetakeOpen).findFirst())
                .or(() -> semesters.stream()
                        .filter(semester -> semester.getStartDate() != null && !semester.getStartDate().isAfter(today))
                        .max(Comparator.comparing(StudentSemesterResponse::getStartDate)))
                .orElse(semesters.isEmpty() ? null : semesters.get(semesters.size() - 1));
    }
}
