package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.dto.request.RetakeRegistrationRequest;
import com.example.ThangLongUniversityWeb.dto.response.RetakeEligibleCourseResponse;
import com.example.ThangLongUniversityWeb.dto.response.RetakeRegisteredItemResponse;
import com.example.ThangLongUniversityWeb.dto.response.RetakeRegistrationResponse;
import com.example.ThangLongUniversityWeb.dto.response.RetakeRequestResponse;
import com.example.ThangLongUniversityWeb.entity.ClassSection;
import com.example.ThangLongUniversityWeb.entity.Course;
import com.example.ThangLongUniversityWeb.entity.Enrollment;
import com.example.ThangLongUniversityWeb.entity.Grade;
import com.example.ThangLongUniversityWeb.entity.Student;
import com.example.ThangLongUniversityWeb.entity.SystemSettings;
import com.example.ThangLongUniversityWeb.entity.Semester;
import com.example.ThangLongUniversityWeb.enums.CourseStudyStatus;
import com.example.ThangLongUniversityWeb.enums.EnrollmentStatus;
import com.example.ThangLongUniversityWeb.enums.EnrollmentType;
import com.example.ThangLongUniversityWeb.entity.ExamRegistration;
import com.example.ThangLongUniversityWeb.entity.RegistrationRound;
import com.example.ThangLongUniversityWeb.repository.ExamRegistrationRepository;
import com.example.ThangLongUniversityWeb.repository.EnrollmentRepository;
import com.example.ThangLongUniversityWeb.repository.GradeRepository;
import com.example.ThangLongUniversityWeb.repository.SemesterRepository;
import com.example.ThangLongUniversityWeb.repository.StudentRepository;
import com.example.ThangLongUniversityWeb.repository.SystemSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StudentRetakeService {

    /** Diem duoi nguong nay => thi lai (RETAKE) */
    private static final float RETAKE_THRESHOLD = 4.0f;
    /** Diem >= RETAKE va < nguong nay => thi nang diem (IMPROVE) */
    private static final float IMPROVE_MAX_EXCLUSIVE = 8.0f;
    /** Key trong bang system_settings */
    public static final String KEY_RETAKE_FEE = "retake_fee_per_course";
    public static final String KEY_MAX_RETAKE_ATTEMPTS = "max_retake_attempts";
    public static final String KEY_MAX_IMPROVE_ATTEMPTS = "max_improve_attempts";
    /** Gia tri mac dinh khi chua cau hinh */
    public static final long DEFAULT_RETAKE_FEE = 200_000L;
    public static final int DEFAULT_MAX_RETAKE_ATTEMPTS = 2;
    public static final int DEFAULT_MAX_IMPROVE_ATTEMPTS = 1;

    private final StudentRepository studentRepository;
    private final GradeRepository gradeRepository;
    private final ExamRegistrationRepository examRegistrationRepository;
    private final SystemSettingsRepository systemSettingsRepository;
    private final SemesterRepository semesterRepository;
    private final RegistrationRoundService registrationRoundService;
    private final SemesterRealtimeService semesterRealtimeService;

    // ─────────────────────────────────────────────────────────────────────────
    // Lay phi thi lai tu system_settings (fallback mac dinh 200k)
    // ─────────────────────────────────────────────────────────────────────────
    public long getRetakeFee() {
        return systemSettingsRepository.findById(KEY_RETAKE_FEE)
                .map(SystemSettings::getValue)
                .map(v -> {
                    try { return Long.parseLong(v); } catch (NumberFormatException e) { return DEFAULT_RETAKE_FEE; }
                })
                .orElse(DEFAULT_RETAKE_FEE);
    }

    public int getMaxRetakeAttempts() {
        return systemSettingsRepository.findById(KEY_MAX_RETAKE_ATTEMPTS)
                .map(SystemSettings::getValue)
                .map(v -> {
                    try { return Integer.parseInt(v); } catch (NumberFormatException e) { return DEFAULT_MAX_RETAKE_ATTEMPTS; }
                })
                .orElse(DEFAULT_MAX_RETAKE_ATTEMPTS);
    }

    public int getMaxImproveAttempts() {
        return systemSettingsRepository.findById(KEY_MAX_IMPROVE_ATTEMPTS)
                .map(SystemSettings::getValue)
                .map(v -> {
                    try { return Integer.parseInt(v); } catch (NumberFormatException e) { return DEFAULT_MAX_IMPROVE_ATTEMPTS; }
                })
                .orElse(DEFAULT_MAX_IMPROVE_ATTEMPTS);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. Lay danh sach mon du dieu kien thi lai / nang diem
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<RetakeEligibleCourseResponse> getEligibleCourses(Long semesterId) {
        if (semesterId == null) {
            throw new RuntimeException("Can chon hoc ky de xem mon du dieu kien thi lai / thi nang diem.");
        }
        Student student = getCurrentStudent();
        long fee = getRetakeFee();
        return latestCompletedGradesByCourse(student.getId()).stream()
                .filter(this::isEligible)
                .filter(grade -> isOpenForRegistration(student.getId(), grade, semesterId))
                .map(grade -> mapEligibleCourse(student.getId(), grade, fee, semesterId))
                .toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Dang ky thi lai theo danh sach courseId
    //    Flow: kiem tra dieu kien → tim ClassSection co lich thi → tao Enrollment + Grade moi
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional
    public RetakeRegistrationResponse register(RetakeRegistrationRequest request) {
        if (request == null || request.getCourseIds() == null || request.getCourseIds().isEmpty()) {
            throw new RuntimeException("Can chon it nhat mot mon hoc de dang ky thi lai / thi nang diem.");
        }

        Student student = getCurrentStudent();
        Semester semester = semesterRepository.findById(request.getSemesterId())
                .orElseThrow(() -> new RuntimeException("Khong tim thay hoc ky dang ky thi lai."));
        RegistrationRound round = registrationRoundService.getOpenRound(request.getSemesterId(), "RETAKE");
        if (round == null || !round.isRegistrationOpen() || round.isLocked()) {
            throw new RuntimeException("Không có đợt đăng ký thi lại/nâng điểm nào đang mở.");
        }

        boolean isValidSlot = false;
        if (round.getTimeSlots() == null || round.getTimeSlots().isEmpty()) {
            isValidSlot = true; // No constraints
        } else {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            for (var slot : round.getTimeSlots()) {
                if (now.isBefore(slot.getStartTime()) || now.isAfter(slot.getEndTime())) {
                    continue;
                }
                boolean majorMatch = true;
                if (slot.getAllowedMajorIds() != null && !slot.getAllowedMajorIds().isBlank()) {
                    List<Long> allowedMajors = java.util.Arrays.stream(slot.getAllowedMajorIds().split(","))
                            .map(String::trim)
                            .map(Long::parseLong)
                            .toList();
                    if (student.getMajor() == null || !allowedMajors.contains(student.getMajor().getId())) {
                        majorMatch = false;
                    }
                }
                
                boolean cohortMatch = true;
                if (slot.getAllowedCohorts() != null && !slot.getAllowedCohorts().isBlank()) {
                    List<String> allowedCohorts = java.util.Arrays.stream(slot.getAllowedCohorts().split(","))
                            .map(String::trim)
                            .toList();
                    if (student.getCohort() == null || !allowedCohorts.contains(student.getCohort())) {
                        cohortMatch = false;
                    }
                }
                
                if (majorMatch && cohortMatch) {
                    isValidSlot = true;
                    break;
                }
            }
        }
        
        if (!isValidSlot) {
            throw new RuntimeException("Thời gian hiện tại không nằm trong phân luồng đăng ký dành cho ngành và khóa của bạn.");
        }

        long feePerCourse = getRetakeFee();

        // Build map ket qua moi nhat theo courseId
        Map<Long, Grade> latestByCourse = new LinkedHashMap<>();
        for (Grade g : latestCompletedGradesByCourse(student.getId())) {
            latestByCourse.put(g.getEnrollment().getClassSection().getCourse().getId(), g);
        }

        List<RetakeRegisteredItemResponse> results = new ArrayList<>();

        for (Long courseId : request.getCourseIds().stream().filter(Objects::nonNull).distinct().toList()) {
            Grade latestGrade = latestByCourse.get(courseId);
            if (latestGrade == null) {
                throw new RuntimeException("Ban chua co ket qua mon hoc ID=" + courseId + " de dang ky thi lai.");
            }
            if (!isEligible(latestGrade)) {
                Course course = latestGrade.getEnrollment().getClassSection().getCourse();
                throw new RuntimeException("Mon " + course.getName() + " khong du dieu kien thi lai / thi nang diem.");
            }

            Course course = latestGrade.getEnrollment().getClassSection().getCourse();
            // Xác định loại đăng ký dựa theo courseStatus
            CourseStudyStatus courseStatus = latestGrade.getEnrollment().getCourseStatus();
            if (courseStatus == CourseStudyStatus.REPEAT_COURSE) {
                throw new RuntimeException("Môn " + course.getName() + " yêu cầu học lại, không thể chỉ thi lại.");
            }
            if (courseStatus == CourseStudyStatus.BANNED_FROM_EXAM) {
                throw new RuntimeException("Môn " + course.getName() + " bị cấm thi do nghỉ quá buổi, phải học lại.");
            }
            EnrollmentType enrollmentType = courseStatus == CourseStudyStatus.RETAKE_EXAM
                    ? EnrollmentType.RETAKE : EnrollmentType.IMPROVE;

            ExamRegistration existing = examRegistrationRepository.findByStudentIdAndCourseIdAndSemesterId(
                            student.getId(), course.getId(), semester.getId())
                    .orElse(null);
            if (existing != null && existing.getStatus() == EnrollmentStatus.REGISTERED) {
                throw new RuntimeException("Ban da duoc chot dang ky thi mon " + course.getName() + " truoc do roi.");
            }
            if (existing != null && existing.getStatus() == EnrollmentStatus.PENDING) {
                results.add(mapRegisteredItem(existing));
                continue;
            }

            validateAttemptLimits(student.getId(), course.getId(), semester.getId(), enrollmentType);

            int nextAttempt = computeNextExamAttempt(student.getId(), course.getId());

            ExamRegistration examReg = existing != null ? existing : new ExamRegistration();
            examReg.setStudent(student);
            examReg.setSemester(semester);
            examReg.setCourse(course);
            examReg.setOriginalGrade(latestGrade);
            examReg.setStatus(EnrollmentStatus.PENDING);
            examReg.setRegistrationType(enrollmentType);
            examReg.setFeeCharged(feePerCourse);
            examReg.setAttemptNumber(nextAttempt);
            examReg.setRegistrationRound(round);
            results.add(mapRegisteredItem(examRegistrationRepository.save(examReg)));
        }

        semesterRealtimeService.publishAfterCommit(semester.getId(), "RETAKE_REGISTRATIONS");
        return new RetakeRegistrationResponse(results, (long) results.size() * feePerCourse);
    }

    @Transactional
    public String cancel(Long examRegistrationId) {
        Student student = getCurrentStudent();
        ExamRegistration reg = examRegistrationRepository.findById(examRegistrationId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay dang ky thi lai / nang diem."));
        if (!reg.getStudent().getId().equals(student.getId())) {
            throw new RuntimeException("Ban khong co quyen bo chon dang ky nay.");
        }
        RegistrationRound round = reg.getRegistrationRound();
        if (round == null || !round.isRegistrationOpen() || round.isLocked()) {
            throw new RuntimeException("Da het han bo chon thi lai / nang diem.");
        }
        if (reg.getStatus() != EnrollmentStatus.PENDING) {
            throw new RuntimeException("Chi co the bo chon dang ky thi lai / nang diem o trang thai PENDING.");
        }

        String courseName = getCourse(reg).getName();
        Long semesterId = reg.getSemester().getId();
        examRegistrationRepository.delete(reg);
        semesterRealtimeService.publishAfterCommit(semesterId, "RETAKE_REGISTRATIONS");
        return "Da bo chon thi lai / nang diem mon " + courseName + ".";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. Xem danh sach da dang ky thi lai / nang diem
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<RetakeRequestResponse> getMyRequests(Long semesterId) {
        Student student = getCurrentStudent();
        return examRegistrationRepository.findRetakeRequests(
                        student.getId(),
                        semesterId,
                        List.copyOf(EnumSet.of(EnrollmentType.RETAKE, EnrollmentType.IMPROVE)))
                .stream()
                .map(this::mapRequest)
                .toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private Student getCurrentStudent() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return studentRepository.findByUser_Username(username)
                .orElseThrow(() -> new RuntimeException("Khong tim thay thong tin sinh vien cua tai khoan nay!"));
    }

    private List<Grade> latestCompletedGradesByCourse(Long studentId) {
        Map<Long, Grade> latestByCourse = new LinkedHashMap<>();
        for (Grade grade : gradeRepository.findCompletedGradesByStudentIdForRetake(studentId)) {
            Long courseId = grade.getEnrollment().getClassSection().getCourse().getId();
            latestByCourse.putIfAbsent(courseId, grade);
        }
        return new ArrayList<>(latestByCourse.values());
    }

    private boolean isEligible(Grade grade) {
        // Dựa vào courseStatus của enrollment thay vì totalScore thô
        CourseStudyStatus courseStatus = grade.getEnrollment().getCourseStatus();
        return courseStatus == CourseStudyStatus.RETAKE_EXAM
                || (courseStatus == CourseStudyStatus.PASSED
                    && grade.getTotalScore() != null
                    && grade.getTotalScore() < IMPROVE_MAX_EXCLUSIVE);
    }

    private static final List<EnrollmentStatus> COUNTED_ATTEMPT_STATUSES = List.of(EnrollmentStatus.REGISTERED);

    private void validateAttemptLimits(Long studentId, Long courseId, Long semesterId, EnrollmentType enrollmentType) {
        if (enrollmentType == EnrollmentType.RETAKE) {
            long priorAttempts = examRegistrationRepository
                    .countByStudentIdAndCourseIdAndRegistrationTypeAndSemesterIdNotAndStatusIn(
                            studentId, courseId, EnrollmentType.RETAKE, semesterId, COUNTED_ATTEMPT_STATUSES);
            if (priorAttempts >= getMaxRetakeAttempts()) {
                throw new RuntimeException("Ban da dat so lan thi lai toi da (" + getMaxRetakeAttempts() + " lan) cho mon nay.");
            }
        } else {
            long priorImprove = examRegistrationRepository
                    .countByStudentIdAndCourseIdAndRegistrationTypeAndSemesterIdNotAndStatusIn(
                            studentId, courseId, EnrollmentType.IMPROVE, semesterId, COUNTED_ATTEMPT_STATUSES);
            if (priorImprove >= getMaxImproveAttempts()) {
                throw new RuntimeException("Ban chi duoc thi nang diem toi da " + getMaxImproveAttempts() + " lan cho mon nay.");
            }
        }
    }

    private boolean isOpenForRegistration(Long studentId, Grade grade, Long semesterId) {
        Long courseId = grade.getEnrollment().getClassSection().getCourse().getId();
        if (examRegistrationRepository.findByStudentIdAndCourseIdAndSemesterId(studentId, courseId, semesterId).isPresent()) {
            return false;
        }
        EnrollmentType enrollmentType = grade.getEnrollment().getCourseStatus() == CourseStudyStatus.RETAKE_EXAM
                ? EnrollmentType.RETAKE : EnrollmentType.IMPROVE;
        try {
            validateAttemptLimits(studentId, courseId, semesterId, enrollmentType);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private int computeNextExamAttempt(Long studentId, Long courseId) {
        long prior = examRegistrationRepository.countByStudentIdAndCourseIdAndRegistrationTypeAndStatusIn(
                studentId, courseId, EnrollmentType.RETAKE, COUNTED_ATTEMPT_STATUSES)
                + examRegistrationRepository.countByStudentIdAndCourseIdAndRegistrationTypeAndStatusIn(
                studentId, courseId, EnrollmentType.IMPROVE, COUNTED_ATTEMPT_STATUSES);
        return (int) prior + 1;
    }

    private RetakeEligibleCourseResponse mapEligibleCourse(Long studentId, Grade grade, long fee, Long semesterId) {
        Enrollment enrollment = grade.getEnrollment();
        Course course = enrollment.getClassSection().getCourse();
        String type = grade.getTotalScore() < RETAKE_THRESHOLD ? "RETAKE" : "IMPROVE";
        return new RetakeEligibleCourseResponse(
                grade.getId(),
                enrollment.getId(),
                course.getId(),
                course.getCode(),
                course.getName(),
                course.getCredits(),
                grade.getTotalScore(),
                computeNextExamAttempt(studentId, course.getId()),
                type,
                fee
        );
    }

    private RetakeRequestResponse mapRequest(ExamRegistration reg) {
        ClassSection cs = reg.getClassSection();
        Course course = getCourse(reg);
        Semester semester = getSemester(reg);
        return new RetakeRequestResponse(
                reg.getId(), // Return ExamRegistration ID instead of Enrollment ID
                cs != null ? cs.getId() : null,
                cs != null ? cs.getClassCode() : "Chua xep lop thi",
                course.getId(),
                course.getCode(),
                course.getName(),
                semester.getId(),
                semester.getName(),
                reg.getStatus() != null ? reg.getStatus().name() : null,
                reg.getRegistrationType() != null ? reg.getRegistrationType().name() : null,
                reg.getAttemptNumber(),
                reg.getOriginalGrade().getTotalScore() // Show original grade total score
        );
    }

    private RetakeRegisteredItemResponse mapRegisteredItem(ExamRegistration reg) {
        ClassSection examSection = reg.getClassSection();
        Course course = getCourse(reg);
        RetakeRegisteredItemResponse item = new RetakeRegisteredItemResponse();
        item.setCourseId(course.getId());
        item.setCourseCode(course.getCode());
        item.setCourseName(course.getName());
        item.setCredits(course.getCredits());
        item.setRegistrationType(reg.getRegistrationType() != null ? reg.getRegistrationType().name() : null);
        item.setAttemptNumber(reg.getAttemptNumber());
        item.setFeeCharged(reg.getFeeCharged());
        if (examSection != null && examSection.getExamAt() != null) {
            item.setExamAt(examSection.getExamAt()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            item.setExamRoom(examSection.getExamRoom());
        }
        return item;
    }

    private Course getCourse(ExamRegistration reg) {
        return reg.getCourse() != null ? reg.getCourse() : reg.getClassSection().getCourse();
    }

    private Semester getSemester(ExamRegistration reg) {
        return reg.getSemester() != null ? reg.getSemester() : reg.getClassSection().getSemester();
    }
}
