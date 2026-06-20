package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.dto.request.GradeRequest;
import com.example.ThangLongUniversityWeb.dto.response.GradeResponse;
import com.example.ThangLongUniversityWeb.dto.response.LearningResultsResponse;
import com.example.ThangLongUniversityWeb.dto.response.LearningResultsResponse.SemesterGpaSummary;
import com.example.ThangLongUniversityWeb.entity.*;
import com.example.ThangLongUniversityWeb.enums.EnrollmentStatus;
import com.example.ThangLongUniversityWeb.enums.EnrollmentType;
import com.example.ThangLongUniversityWeb.repository.AttendanceRecordRepository;
import com.example.ThangLongUniversityWeb.repository.EnrollmentRepository;
import com.example.ThangLongUniversityWeb.repository.GradeRepository;
import com.example.ThangLongUniversityWeb.repository.SemesterRepository;
import com.example.ThangLongUniversityWeb.repository.StudentRepository;
import com.example.ThangLongUniversityWeb.repository.ExamRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final GradeRepository gradeRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final SemesterRepository semesterRepository;
    private final ExamRegistrationRepository examRegistrationRepository;
    private final CourseOutcomeService courseOutcomeService;
    private final AttendanceRecordRepository attendanceRecordRepository;

    /**
     * Tạo hoặc cập nhật điểm cho sinh viên
     */
    @Transactional
    public GradeResponse updateGrade(GradeRequest request) {
        Enrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy enrollment!"));

        ExamRegistration activeRegistration = findActiveExamRegistration(enrollment.getId());

        Grade grade = gradeRepository.findByEnrollmentId(request.getEnrollmentId())
                .orElseGet(Grade::new);

        if (activeRegistration != null) {
            validateRetakeGradeRequest(request, grade);
        }

        grade.setEnrollment(enrollment);
        if (activeRegistration != null) {
            grade.setParticipationScore(grade.getParticipationScore());
            grade.setMidtermScore(grade.getMidtermScore());
            grade.setFinalScore(grade.getFinalScore());
            grade.setRetestScore(request.getRetestScore());
            grade.prepareGradeCalculation(activeRegistration.getRegistrationType());
        } else {
            grade.setParticipationScore(request.getParticipationScore());
            grade.setMidtermScore(request.getMidTermScore());
            grade.setFinalScore(request.getFinalScore());
            grade.setRetestScore(request.getRetestScore());
            grade.prepareGradeCalculation(null);
        }

        Grade savedGrade = gradeRepository.save(grade);

        enrollment.setGrade(savedGrade);
        courseOutcomeService.recalculate(enrollment);

        return mapToResponse(enrollment, savedGrade, activeRegistration);
    }

    private ExamRegistration findActiveExamRegistration(Long enrollmentId) {
        List<ExamRegistration> registrations = examRegistrationRepository
                .findActiveByOriginalEnrollmentId(enrollmentId, EnrollmentStatus.REGISTERED);
        return registrations.isEmpty() ? null : registrations.getFirst();
    }

    private void validateRetakeGradeRequest(GradeRequest request, Grade grade) {
        if (request.getRetestScore() == null
                && request.getParticipationScore() == null
                && request.getMidTermScore() == null
                && request.getFinalScore() == null) {
            return;
        }
        if ((request.getParticipationScore() != null && !request.getParticipationScore().equals(grade.getParticipationScore()))
                || (request.getMidTermScore() != null && !request.getMidTermScore().equals(grade.getMidtermScore()))
                || (request.getFinalScore() != null && !request.getFinalScore().equals(grade.getFinalScore()))) {
            throw new RuntimeException("Sinh vien dang ky thi lai/nang diem chi duoc cap nhat diem thi lai.");
        }
    }

    /**
     * Lấy bảng điểm của sinh viên theo học kỳ
     */
    @Transactional(readOnly = true)
    public List<GradeResponse> getStudentGradesBySemester(Long studentId, Long semesterId) {
        return enrollmentRepository.findByStudentIdAndClassSection_SemesterId(studentId, semesterId).stream()
                .filter(this::isVisibleInStudentGrades)
                .sorted(studentEnrollmentComparator())
                .map(enrollment -> mapToResponse(enrollment, enrollment.getGrade()))
                .collect(Collectors.toList());
    }

    /**
     * Lấy bảng điểm của sinh viên tất cả kỳ
     */
    @Transactional(readOnly = true)
    public List<GradeResponse> getStudentAllGrades(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .filter(this::isVisibleInStudentGrades)
                .sorted(studentEnrollmentComparator())
                .map(enrollment -> mapToResponse(enrollment, enrollment.getGrade()))
                .collect(Collectors.toList());
    }

    /**
     * Lấy bảng điểm của lớp (cho giảng viên xem)
     * Bao gồm cả sinh viên học chính thức và sinh viên đăng ký thi lại/cải thiện vào lớp này.
     */
    @Transactional(readOnly = true)
    public List<GradeResponse> getClassSectionGrades(Long classSectionId) {
        // 1. Sinh viên học chính thức (qua Enrollment)
        List<GradeResponse> regularGrades = enrollmentRepository.findByClassSectionId(classSectionId).stream()
                .filter(enrollment -> enrollment.getStatus() != EnrollmentStatus.PENDING)
                .map(enrollment -> mapToResponse(enrollment, enrollment.getGrade()))
                .collect(Collectors.toList());

        // 2. Sinh viên thi lại/cải thiện (qua ExamRegistration)
        List<GradeResponse> retakeGrades = examRegistrationRepository.findByClassSectionIdAndStatus(classSectionId, EnrollmentStatus.REGISTERED).stream()
                .map(reg -> {
                    GradeResponse res = mapToResponse(reg.getOriginalGrade().getEnrollment(), reg.getOriginalGrade(), reg);
                    res.setEnrollmentType(reg.getRegistrationType() != null ? reg.getRegistrationType().name() : "RETAKE");
                    return res;
                })
                .collect(Collectors.toList());

        regularGrades.addAll(retakeGrades);
        return regularGrades;
    }

    /**
     * Lay ket qua hoc tap tong hop: bang diem + GPA/CPA cho sinh vien (portal)
     */
    @Transactional(readOnly = true)
    public LearningResultsResponse getLearningResults(String username, Long semesterId) {
        var student = studentRepository.findByUser_Username(username)
                .orElseThrow(() -> new RuntimeException("Khong tim thay sinh vien!"));
        Long studentId = student.getId();

        List<GradeResponse> allGradeResponses = mergeEnrollmentAndExamGrades(
                getStudentAllGrades(studentId),
                getStudentExamRegistrationGrades(studentId, null));
        List<GradeResponse> allGrades = allGradeResponses.stream()
                .filter(this::hasCompletedGrade)
                .collect(Collectors.toList());

        List<GradeResponse> rawGrades = semesterId != null
                ? mergeEnrollmentAndExamGrades(
                        getStudentGradesBySemester(studentId, semesterId),
                        getStudentExamRegistrationGrades(studentId, semesterId))
                : allGradeResponses;
        List<GradeResponse> grades = dedupeGradeRows(rawGrades);
        List<GradeResponse> completedGradesForSelectedSemester = grades.stream()
                .filter(this::hasCompletedGrade)
                .collect(Collectors.toList());

        Map<Long, List<GradeResponse>> gradesBySemester = allGrades.stream()
                .filter(g -> g.getSemesterId() != null)
                .collect(Collectors.groupingBy(
                        GradeResponse::getSemesterId,
                        LinkedHashMap::new,
                        Collectors.toList()));

        List<Map.Entry<Long, List<GradeResponse>>> chronologicalSemesters = gradesBySemester.entrySet().stream()
                .sorted(Comparator.comparing(e -> findSemesterOrderDate(e.getKey())))
                .collect(Collectors.toList());

        Map<Long, GradeResponse> bestGradesByCourse = new LinkedHashMap<>();
        List<SemesterGpaSummary> semesterSummaries = chronologicalSemesters.stream()
                .map(entry -> {
                    entry.getValue().forEach(grade -> bestGradesByCourse.merge(
                            grade.getCourseId(),
                            grade,
                            this::betterGrade));

                    List<GradeResponse> cumulativeBestGrades = List.copyOf(bestGradesByCourse.values());
                    return SemesterGpaSummary.builder()
                            .semesterId(entry.getKey())
                            .semesterName(entry.getValue().isEmpty() ? null : entry.getValue().get(0).getSemesterName())
                            .semesterGpa(round2(computeGpa(entry.getValue())))
                            .cumulativeGpa(round2(computeGpa(cumulativeBestGrades)))
                            .totalCredits(sumAttemptedCredits(entry.getValue()))
                            .cumulativeCredits(sumPassedCredits(cumulativeBestGrades))
                            .build();
                })
                .sorted(Comparator.comparing(SemesterGpaSummary::getSemesterId).reversed())
                .collect(Collectors.toList());

        List<GradeResponse> bestGrades = getBestGradesByCourse(allGrades);
        Float semGpa = semesterId != null ? round2(computeGpa(completedGradesForSelectedSemester)) : null;
        Float cumGpa = round2(computeGpa(bestGrades));
        Integer semCredits = semesterId != null ? sumAttemptedCredits(completedGradesForSelectedSemester) : null;
        Integer cumCredits = sumPassedCredits(bestGrades);
        String semesterName = semesterId != null
                ? grades.stream().findFirst().map(GradeResponse::getSemesterName)
                        .orElseGet(() -> semesterRepository.findById(semesterId).map(Semester::getName).orElse(null))
                : null;

        return LearningResultsResponse.builder()
                .semesterId(semesterId)
                .semesterName(semesterName)
                .semesterGpa(semGpa)
                .cumulativeGpa(cumGpa)
                .semesterCredits(semCredits)
                .cumulativeCredits(cumCredits)
                .grades(grades)
                .semesterSummaries(semesterSummaries)
                .build();
    }

    private boolean hasCompletedGrade(GradeResponse grade) {
        if (grade.getExamRegistrationId() != null) {
            return grade.getRetestScore() != null
                    && grade.getTotalScore() != null
                    && grade.getGradePoint() != null
                    && grade.getCredits() != null;
        }
        return grade.getTotalScore() != null && grade.getGradePoint() != null && grade.getCredits() != null;
    }

    private boolean isDisplayableGrade(GradeResponse grade) {
        if (grade.getExamRegistrationId() != null) {
            return true;
        }
        return grade.getTotalScore() != null
                || grade.getParticipationScore() != null
                || grade.getMidtermScore() != null
                || grade.getFinalScore() != null;
    }

    private List<GradeResponse> mergeEnrollmentAndExamGrades(
            List<GradeResponse> enrollmentGrades,
            List<GradeResponse> examGrades) {
        List<GradeResponse> merged = new ArrayList<>(enrollmentGrades);
        merged.addAll(examGrades);
        return merged;
    }

    private List<GradeResponse> getStudentExamRegistrationGrades(Long studentId, Long semesterId) {
        List<ExamRegistration> registrations = semesterId != null
                ? examRegistrationRepository.findByStudentIdAndSemesterIdAndStatus(
                        studentId, semesterId, EnrollmentStatus.REGISTERED)
                : examRegistrationRepository.findByStudentIdAndStatus(studentId, EnrollmentStatus.REGISTERED);

        return registrations.stream()
                .map(this::mapExamRegistrationToGradeResponse)
                .filter(this::isDisplayableGrade)
                .toList();
    }

    private GradeResponse mapExamRegistrationToGradeResponse(ExamRegistration registration) {
        Grade grade = registration.getOriginalGrade();
        Enrollment enrollment = grade.getEnrollment();
        GradeResponse response = mapToResponse(enrollment, grade, registration);

        Semester registrationSemester = registration.getSemester();
        if (registrationSemester == null && registration.getClassSection() != null) {
            registrationSemester = registration.getClassSection().getSemester();
        }
        if (registrationSemester != null) {
            response.setSemesterId(registrationSemester.getId());
            response.setSemesterName(registrationSemester.getName());
        }

        Course course = registration.getCourse() != null
                ? registration.getCourse()
                : enrollment.getClassSection().getCourse();
        response.setCourseId(course.getId());
        response.setCourseCode(course.getCode());
        response.setCourseName(course.getName());
        response.setCredits(course.getCredits());

        response.setStudySemesterName(enrollment.getClassSection().getSemester().getName());
        if (registration.getRegistrationType() != null) {
            response.setEnrollmentType(registration.getRegistrationType().name());
        } else {
            response.setEnrollmentType(EnrollmentType.RETAKE.name());
        }
        return response;
    }

    private boolean isVisibleInStudentGrades(Enrollment enrollment) {
        return enrollment.getStatus() != EnrollmentStatus.PENDING
                && enrollment.getStatus() != EnrollmentStatus.CANCELED;
    }

    private Comparator<Enrollment> studentEnrollmentComparator() {
        return Comparator
                .comparing((Enrollment enrollment) -> enrollment.getClassSection().getSemester().getId(),
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(enrollment -> enrollment.getClassSection().getCourse().getCode(),
                        Comparator.nullsLast(String::compareTo))
                .thenComparing(Enrollment::getId, Comparator.nullsLast(Comparator.reverseOrder()));
    }

    private java.time.LocalDate findSemesterOrderDate(Long semesterId) {
        return semesterRepository.findById(semesterId)
                .map(s -> s.getStartDate() != null ? s.getStartDate() : java.time.LocalDate.MIN.plusDays(s.getId()))
                .orElse(java.time.LocalDate.MIN);
    }

    private List<GradeResponse> dedupeGradeRows(List<GradeResponse> grades) {
        Map<String, GradeResponse> uniqueRows = new LinkedHashMap<>();
        for (GradeResponse grade : grades) {
            if (!isDisplayableGrade(grade)) {
                continue;
            }
            String key = grade.getExamRegistrationId() != null
                    ? "exam:" + grade.getExamRegistrationId()
                    : grade.getSemesterId() + ":" + grade.getCourseId();
            uniqueRows.putIfAbsent(key, grade);
        }
        return uniqueRows.values().stream()
                .sorted(Comparator
                        .comparing(GradeResponse::getSemesterId, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(GradeResponse::getCourseCode, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList());
    }

    private List<GradeResponse> dedupeBestPerCoursePerSemester(List<GradeResponse> grades) {
        Map<String, GradeResponse> bestBySemesterCourse = new LinkedHashMap<>();
        for (GradeResponse grade : grades) {
            String key = grade.getSemesterId() + ":" + grade.getCourseId();
            bestBySemesterCourse.merge(key, grade, this::betterGrade);
        }
        return bestBySemesterCourse.values().stream()
                .sorted(Comparator
                        .comparing(GradeResponse::getSemesterId, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(GradeResponse::getCourseCode, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList());
    }

    private List<GradeResponse> getBestGradesByCourse(List<GradeResponse> grades) {
        Map<Long, GradeResponse> bestGrades = new LinkedHashMap<>();
        grades.forEach(grade -> bestGrades.merge(grade.getCourseId(), grade, this::betterGrade));
        return List.copyOf(bestGrades.values());
    }

    private GradeResponse betterGrade(GradeResponse current, GradeResponse candidate) {
        float currentPoint = current.getGradePoint() != null ? current.getGradePoint() : 0f;
        float candidatePoint = candidate.getGradePoint() != null ? candidate.getGradePoint() : 0f;
        if (Float.compare(candidatePoint, currentPoint) != 0) {
            return candidatePoint > currentPoint ? candidate : current;
        }

        float currentScore = current.getTotalScore() != null ? current.getTotalScore() : 0f;
        float candidateScore = candidate.getTotalScore() != null ? candidate.getTotalScore() : 0f;
        return candidateScore > currentScore ? candidate : current;
    }

    private float computeGpa(List<GradeResponse> grades) {
        float totalWeightedPoints = 0f;
        int totalCredits = 0;
        for (GradeResponse grade : grades) {
            if (!hasCompletedGrade(grade) || grade.getCredits() <= 0) {
                continue;
            }
            totalWeightedPoints += grade.getGradePoint() * grade.getCredits();
            totalCredits += grade.getCredits();
        }
        return totalCredits == 0 ? 0f : totalWeightedPoints / totalCredits;
    }

    private int sumAttemptedCredits(List<GradeResponse> grades) {
        return grades.stream()
                .filter(this::hasCompletedGrade)
                .mapToInt(GradeResponse::getCredits)
                .sum();
    }

    private int sumPassedCredits(List<GradeResponse> grades) {
        return grades.stream()
                .filter(this::hasCompletedGrade)
                .filter(grade -> grade.getGradePoint() > 0f)
                .mapToInt(GradeResponse::getCredits)
                .sum();
    }

    private Float round2(float value) {
        return Math.round(value * 100f) / 100f;
    }

    /**
     * Chuyển đổi Grade Entity sang DTO
     */
    private GradeResponse mapToResponse(Grade grade) {
        Enrollment enrollment = grade.getEnrollment();
        return mapToResponse(enrollment, grade, null);
    }

    private GradeResponse mapToResponse(Enrollment enrollment, Grade grade) {
        return mapToResponse(enrollment, grade, null);
    }

    private GradeResponse mapToResponse(Enrollment enrollment, Grade grade, ExamRegistration examRegistration) {
        Student student = enrollment.getStudent();
        ClassSection classSection = enrollment.getClassSection();
        Course course = classSection.getCourse();
        Semester semester = classSection.getSemester();

        long absences = attendanceRecordRepository.countByEnrollmentIdAndStatus(
                enrollment.getId(), com.example.ThangLongUniversityWeb.enums.AttendanceStatus.ABSENT);

        return GradeResponse.builder()
                .id(grade != null ? grade.getId() : null)
                .enrollmentId(enrollment.getId())
                .studentId(student.getId())
                .studentCode(student.getStudentCode())
                .studentName(student.getFullName())
                .courseId(course.getId())
                .courseCode(course.getCode())
                .classCode(classSection.getClassCode())
                .courseName(course.getName())
                .credits(course.getCredits())
                .semesterId(semester.getId())
                .semesterName(semester.getName())
                .participationScore(grade != null ? grade.getParticipationScore() : null)
                .midtermScore(grade != null ? grade.getMidtermScore() : null)
                .finalScore(grade != null ? grade.getFinalScore() : null)
                .retestScore(grade != null ? grade.getRetestScore() : null)
                .attemptNumber(grade != null ? grade.getAttemptNumber() : 1)
                .enrollmentType(grade != null && grade.getEnrollmentType() != null ? grade.getEnrollmentType().name() : "ORDINARY")
                .totalScore(grade != null ? grade.getTotalScore() : null)
                .letterGrade(grade != null ? grade.getLetterGrade() : null)
                .gpa4(grade != null ? grade.getGpa4() : null)
                .gradePoint(grade != null ? grade.getGpa4() : null)
                .courseStatus(enrollment.getCourseStatus() != null ? enrollment.getCourseStatus().name() : null)
                .absenceCount(absences)
                .createdAt(grade != null ? grade.getCreatedAt() : null)
                .updatedAt(grade != null ? grade.getUpdatedAt() : null)
                .examRegistrationId(examRegistration != null ? examRegistration.getId() : null)
                .examAttemptNumber(examRegistration != null ? examRegistration.getAttemptNumber() : null)
                .build();
    }
}
