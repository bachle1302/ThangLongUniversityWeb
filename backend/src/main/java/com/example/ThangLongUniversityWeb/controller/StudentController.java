package com.example.ThangLongUniversityWeb.controller;

import com.example.ThangLongUniversityWeb.dto.response.StudentSemesterResponse;
import com.example.ThangLongUniversityWeb.dto.response.StudentCourseRegistrationOverviewResponse;
import com.example.ThangLongUniversityWeb.dto.response.StudentDashboardResponse;
import com.example.ThangLongUniversityWeb.dto.response.LearningResultsResponse;
import com.example.ThangLongUniversityWeb.repository.SemesterRepository;
import com.example.ThangLongUniversityWeb.repository.StudentRepository;
import com.example.ThangLongUniversityWeb.service.CourseService;
import com.example.ThangLongUniversityWeb.service.EnrollmentRequestStatusService;
import com.example.ThangLongUniversityWeb.service.ExamSessionService;
import com.example.ThangLongUniversityWeb.service.GradeService;
import com.example.ThangLongUniversityWeb.service.StudentEnrollmentService;
import com.example.ThangLongUniversityWeb.service.RegistrationRoundService;
import com.example.ThangLongUniversityWeb.service.StudentService;
import com.example.ThangLongUniversityWeb.service.StudentTuitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@Tag(name = "Student - learning and enrollment")
@SecurityRequirement(name = "bearerAuth")
public class StudentController {

    private final StudentEnrollmentService studentEnrollmentService;
    private final StudentTuitionService studentTuitionService;
    private final EnrollmentRequestStatusService enrollmentRequestStatusService;
    private final SemesterRepository semesterRepository;
    private final StudentRepository studentRepository;
    private final CourseService courseService;
    private final GradeService gradeService;
    private final StudentService studentService;
    private final RegistrationRoundService registrationRoundService;
    private final ExamSessionService examSessionService;

    @Operation(summary = "Lay ho so sinh vien dang dang nhap")
    @GetMapping("/profile")
    public ResponseEntity<?> getStudentProfile(Authentication authentication) {
        return ResponseEntity.ok(studentService.getProfileByUsername(authentication.getName()));
    }

    @Operation(summary = "Du lieu tong hop cho student dashboard")
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(Authentication authentication, @RequestParam(required = false) Long semesterId) {
        List<StudentSemesterResponse> semesters = getStudentSemesters();
        StudentSemesterResponse currentSemester = resolveDashboardSemester(semesters, semesterId);

        if (currentSemester == null) {
            return ResponseEntity.ok(StudentDashboardResponse.builder()
                    .profile(studentService.getProfileByUsername(authentication.getName()))
                    .semesterGpa(0f)
                    .cumulativeGpa(0f)
                    .registeredCredits(0)
                    .earnedCredits(0)
                    .gradedCourseCount(0)
                    .activeCourseCount(0)
                    .upcomingExamCount(0)
                    .tuitionRemaining(0L)
                    .tuitionStatus("Khong co hoc ky")
                    .registrationStatus("Khong co hoc ky")
                    .build());
        }

        var learningResults = gradeService.getLearningResults(authentication.getName(), currentSemester.getId());
        var grades = studentEnrollmentService.getMyGrades(currentSemester.getId());
        var schedule = studentEnrollmentService.getMySchedule(currentSemester.getId());
        var tuition = studentTuitionService.getTuitionFee(currentSemester.getId());
        var exams = studentEnrollmentService.getMyExams(currentSemester.getId());
        int today = LocalDate.now().getDayOfWeek().getValue() + 1;
        LocalDateTime now = LocalDateTime.now();

        var todaySchedule = schedule.stream()
                .filter(item -> item.getSchedules() != null && item.getSchedules().stream()
                        .anyMatch(slot -> Objects.equals(slot.getDayOfWeek(), today)))
                .toList();
        var upcomingExams = exams.stream()
                .filter(exam -> exam.getExamAt() != null && !exam.getExamAt().isBefore(now))
                .sorted(Comparator.comparing(exam -> exam.getExamAt()))
                .toList();
        int registeredCredits = schedule.stream()
                .mapToInt(item -> item.getCredits() != null ? item.getCredits() : 0)
                .sum();
        int earnedCredits = learningResults.getCumulativeCredits() != null ? learningResults.getCumulativeCredits() : 0;
        int gradedCourseCount = (int) learningResults.getGrades().stream()
                .filter(grade -> grade.getTotalScore() != null && grade.getGradePoint() != null)
                .count();
        int activeCourseCount = (int) schedule.stream()
                .map(item -> item.getClassCode())
                .filter(Objects::nonNull)
                .distinct()
                .count();
        long tuitionRemaining = tuition.isPaid() ? 0L : tuition.getTotalAmount();
        Float semesterGpa = resolveDashboardSemesterGpa(learningResults);
        Float cumulativeGpa = learningResults.getCumulativeGpa() != null ? learningResults.getCumulativeGpa() : 0f;

        return ResponseEntity.ok(StudentDashboardResponse.builder()
                .profile(studentService.getProfileByUsername(authentication.getName()))
                .currentSemester(currentSemester)
                .learningResults(learningResults)
                .grades(grades)
                .tuition(tuition)
                .schedule(schedule)
                .todaySchedule(todaySchedule)
                .exams(exams)
                .upcomingExams(upcomingExams)
                .semesterGpa(semesterGpa)
                .cumulativeGpa(cumulativeGpa)
                .registeredCredits(registeredCredits)
                .earnedCredits(earnedCredits)
                .gradedCourseCount(gradedCourseCount)
                .activeCourseCount(activeCourseCount)
                .upcomingExamCount(upcomingExams.size())
                .tuitionRemaining(tuitionRemaining)
                .tuitionStatus(tuition.isPaid() ? "Da thanh toan" : "Chua thanh toan")
                .registrationStatus(currentSemester.isRegistrationOpen() ? "Dang mo" : "Da dong")
                .build());
    }

    @Operation(summary = "Tong hop du lieu dang ky hoc phan")
    @GetMapping("/course-registration/overview")
    public ResponseEntity<?> getCourseRegistrationOverview(@RequestParam(required = false) Long semesterId) {
        List<StudentSemesterResponse> semesters = getStudentSemesters();
        StudentSemesterResponse currentSemester = resolveDashboardSemester(semesters, semesterId);
        if (currentSemester == null) {
            return ResponseEntity.ok(StudentCourseRegistrationOverviewResponse.builder()
                    .semesters(semesters)
                    .availableClasses(List.of())
                    .selectedEnrollments(List.of())
                    .readonly(true)
                    .registrationStatus("Khong co hoc ky")
                    .build());
        }

        boolean readonly = !currentSemester.isRegistrationOpen() || currentSemester.isLocked();
        return ResponseEntity.ok(StudentCourseRegistrationOverviewResponse.builder()
                .semesters(semesters)
                .currentSemester(currentSemester)
                .availableClasses(studentEnrollmentService.getAvailableClasses(currentSemester.getId()))
                .selectedEnrollments(studentEnrollmentService.getSelectedEnrollments(currentSemester.getId()))
                .readonly(readonly)
                .registrationStatus(readonly ? "Da dong" : "Dang mo")
                .build());
    }

    private Float resolveDashboardSemesterGpa(LearningResultsResponse learningResults) {
        if (learningResults.getSemesterCredits() != null
                && learningResults.getSemesterCredits() > 0
                && learningResults.getSemesterGpa() != null) {
            return learningResults.getSemesterGpa();
        }

        return learningResults.getSemesterSummaries().stream()
                .filter(summary -> summary.getTotalCredits() != null && summary.getTotalCredits() > 0)
                .findFirst()
                .map(LearningResultsResponse.SemesterGpaSummary::getSemesterGpa)
                .orElse(0f);
    }

    @Operation(summary = "Lay danh sach hoc ky cho sinh vien")
    @GetMapping("/semesters")
    public ResponseEntity<?> getSemesters() {
        return ResponseEntity.ok(getStudentSemesters());
    }

    private List<StudentSemesterResponse> getStudentSemesters() {
        return semesterRepository.findAll().stream()
                .sorted(Comparator.comparing(s -> s.getStartDate() == null ? LocalDate.MIN : s.getStartDate()))
                .map(s -> StudentSemesterResponse.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .startDate(s.getStartDate())
                        .endDate(s.getEndDate())
                        .registrationOpen(s.isRegistrationOpen())
                        .locked(s.isLocked())
                        .examPublished(s.isExamPublished())
                        .retakeOpen(s.isRetakeOpen())
                        .retakeLocked(s.isRetakeLocked())
                        .activeRegistrationRoundId(resolveActiveRoundId(s.getId()))
                        .activeRegistrationRoundName(resolveActiveRoundName(s.getId()))
                        .activeRegistrationRoundNumber(resolveActiveRoundNumber(s.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    private Long resolveActiveRoundId(Long semesterId) {
        var round = registrationRoundService.getOpenRound(semesterId);
        return round != null ? round.getId() : null;
    }

    private String resolveActiveRoundName(Long semesterId) {
        var round = registrationRoundService.getOpenRound(semesterId);
        return round != null ? round.getName() : null;
    }

    private Integer resolveActiveRoundNumber(Long semesterId) {
        var round = registrationRoundService.getOpenRound(semesterId);
        return round != null ? round.getRoundNumber() : null;
    }

    private StudentSemesterResponse resolveDashboardSemester(List<StudentSemesterResponse> semesters, Long semesterId) {
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
                .or(() -> semesters.stream().filter(StudentSemesterResponse::isRegistrationOpen).findFirst())
                .or(() -> semesters.stream()
                        .filter(semester -> semester.getStartDate() != null && !semester.getStartDate().isAfter(today))
                        .max(Comparator.comparing(StudentSemesterResponse::getStartDate)))
                .orElse(semesters.isEmpty() ? null : semesters.get(semesters.size() - 1));
    }

    @Operation(summary = "Xem danh sach lop hoc phan trong mot hoc ky")
    @GetMapping("/classes/semester/{semesterId}")
    public ResponseEntity<?> getAvailableClasses(@PathVariable Long semesterId) {
        return ResponseEntity.ok(studentEnrollmentService.getAvailableClasses(semesterId));
    }

    @Operation(summary = "Dang ky vao mot lop hoc phan")
    @PostMapping("/enroll/{classSectionId}")
    public ResponseEntity<?> registerClass(@PathVariable Long classSectionId) {
        return ResponseEntity.ok(studentEnrollmentService.registerClass(classSectionId));
    }

    @Operation(summary = "Danh sach hoc phan dang chon trong mot hoc ky")
    @GetMapping("/enrollments/selected")
    public ResponseEntity<?> getSelectedEnrollments(@RequestParam Long semesterId) {
        return ResponseEntity.ok(studentEnrollmentService.getSelectedEnrollments(semesterId));
    }

    @Operation(summary = "Huy dang ky lop hoc phan")
    @DeleteMapping("/enroll/{classSectionId}")
    public ResponseEntity<?> cancelClass(@PathVariable Long classSectionId) {
        return ResponseEntity.ok(studentEnrollmentService.cancelClass(classSectionId));
    }

    @Operation(summary = "Xem thoi khoa bieu ca nhan trong mot hoc ky")
    @GetMapping("/my-schedule/{semesterId}")
    public ResponseEntity<?> getMySchedule(@PathVariable Long semesterId) {
        return ResponseEntity.ok(studentEnrollmentService.getMySchedule(semesterId));
    }

    @Operation(summary = "Xem diem tong hop")
    @GetMapping("/grades")
    public ResponseEntity<?> getMyGrades(@RequestParam(required = false) Long semesterId) {
        return ResponseEntity.ok(studentEnrollmentService.getMyGrades(semesterId));
    }

    @Operation(summary = "Xem lich thi theo hoc ky")
    @GetMapping("/exams")
    public ResponseEntity<?> getMyExams(@RequestParam Long semesterId) {
        var username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        var student = studentRepository.findByUser_Username(username)
                .orElseThrow(() -> new RuntimeException("Khong tim thay thong tin sinh vien cua tai khoan nay."));
        var examSeats = examSessionService.getStudentExams(student.getId(), semesterId);
        if (!examSeats.isEmpty()) {
            return ResponseEntity.ok(examSeats);
        }
        return ResponseEntity.ok(studentEnrollmentService.getMyExams(semesterId));
    }

    @Operation(summary = "Check trang thai xu ly dang ky hoc phan")
    @GetMapping("/enrollments/status/{requestId}")
    public ResponseEntity<?> getEnrollmentStatus(@PathVariable String requestId) {
        return ResponseEntity.ok(enrollmentRequestStatusService.getStatus(requestId));
    }

    @Operation(summary = "Xem hoa don hoc phi")
    @GetMapping("/tuition/{semesterId}")
    public ResponseEntity<?> getTuitionFee(@PathVariable Long semesterId) {
        return ResponseEntity.ok(studentTuitionService.getTuitionFee(semesterId));
    }

    @Operation(summary = "Tao link thanh toan VNPAY")
    @PostMapping("/tuition/{semesterId}/vnpay-url")
    public ResponseEntity<?> getVNPayUrl(@PathVariable Long semesterId, HttpServletRequest request) {
        String paymentUrl = studentTuitionService.createVNPayUrl(semesterId, request);
        return ResponseEntity.ok(paymentUrl);
    }

    @Operation(summary = "Nhan ket qua tra ve tu VNPAY")
    @GetMapping("/tuition/vnpay-return")
    public ResponseEntity<?> vnpayReturn(HttpServletRequest request) {
        String result = studentTuitionService.processVNPayReturn(request);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Chuong trinh dao tao - Tat ca mon hoc trong truong")
    @GetMapping("/curriculum")
    public ResponseEntity<?> getCurriculum() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @Operation(summary = "Chuong trinh dao tao - Mon hoc theo nganh cua sinh vien dang nhap")
    @GetMapping("/curriculum/my-major")
    public ResponseEntity<?> getCurriculumByMajor(
            org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.ok(courseService.getCoursesByStudentMajor(authentication.getName()));
    }

    @Operation(summary = "Ket qua hoc tap tong hop: bang diem + GPA/CPA (co the loc theo hoc ky)")
    @GetMapping("/learning-results")
    public ResponseEntity<?> getLearningResults(
            org.springframework.security.core.Authentication authentication,
            @RequestParam(required = false) Long semesterId) {
        return ResponseEntity.ok(gradeService.getLearningResults(authentication.getName(), semesterId));
    }
}
