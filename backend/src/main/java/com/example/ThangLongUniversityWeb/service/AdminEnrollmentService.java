package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.audit.Audit;
import com.example.ThangLongUniversityWeb.dto.request.AdminOverrideEnrollmentRequest;
import com.example.ThangLongUniversityWeb.dto.response.AdminEnrollmentResponse;
import com.example.ThangLongUniversityWeb.entity.ClassSection;
import com.example.ThangLongUniversityWeb.entity.Enrollment;
import com.example.ThangLongUniversityWeb.entity.ExamRegistration;
import com.example.ThangLongUniversityWeb.entity.Grade;
import com.example.ThangLongUniversityWeb.entity.Semester;
import com.example.ThangLongUniversityWeb.entity.Student;
import com.example.ThangLongUniversityWeb.enums.EnrollmentStatus;
import com.example.ThangLongUniversityWeb.enums.EnrollmentType;
import com.example.ThangLongUniversityWeb.repository.ClassSectionRepository;
import com.example.ThangLongUniversityWeb.repository.EnrollmentRepository;
import com.example.ThangLongUniversityWeb.repository.ExamRegistrationRepository;
import com.example.ThangLongUniversityWeb.repository.GradeRepository;
import com.example.ThangLongUniversityWeb.repository.SemesterRepository;
import com.example.ThangLongUniversityWeb.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminEnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final ClassSectionRepository classSectionRepository;
    private final SemesterRepository semesterRepository;
    private final GradeRepository gradeRepository;
    private final ExamRegistrationRepository examRegistrationRepository;
    private final RegistrationRoundService registrationRoundService;

    @Transactional(readOnly = true)
    public Page<AdminEnrollmentResponse> search(Long semesterId, Long classSectionId, EnrollmentStatus status, Pageable pageable) {
        return enrollmentRepository.searchAdmin(semesterId, classSectionId, status, pageable)
                .map(this::toAdminResponse);
    }

    @Transactional
    @Audit(action = "ENROLLMENT_OVERRIDE", targetType = "Enrollment")
    public AdminEnrollmentResponse overrideEnrollment(AdminOverrideEnrollmentRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên."));
        ClassSection targetClass = classSectionRepository.findByIdForUpdate(request.getClassSectionId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học phần."));

        try {
            Enrollment enrollment = new Enrollment();
            enrollment.setStudent(student);
            enrollment.setClassSection(targetClass);
            enrollment.setStatus(EnrollmentStatus.REGISTERED);

            Enrollment saved = enrollmentRepository.save(enrollment);

            // Override: vẫn tăng currentSlots kể cả vượt maxSlots
            targetClass.setCurrentSlots(targetClass.getCurrentSlots() + 1);
            classSectionRepository.save(targetClass);

            return toAdminResponse(saved);
        } catch (DataIntegrityViolationException dup) {
            throw new RuntimeException("Sinh viên đã đăng ký lớp học phần này (duplicate).");
        }
    }

    @Transactional
    @Audit(action = "ENROLLMENT_LOCK_SEMESTER", targetType = "Enrollment")
    public int lockPendingEnrollments(Long semesterId) {
        return registrationRoundService.lockOpenRound(semesterId);
    }

    @Transactional
    @Audit(action = "RETAKE_LOCK_SEMESTER", targetType = "ExamRegistration")
    public int lockPendingRetakes(Long semesterId) {
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay hoc ky."));
        if (!semester.isLocked()) {
            throw new RuntimeException("Phai khoa tong dang ky hoc phan truoc khi khoa tong thi lai.");
        }
        var pendingRetakes = examRegistrationRepository.findBySemesterIdAndStatus(semesterId, EnrollmentStatus.PENDING);

        for (ExamRegistration registration : pendingRetakes) {
            registration.setStatus(EnrollmentStatus.REGISTERED);
            if (registration.getCourse() != null) {
                var activeSections = classSectionRepository.findBySemesterIdAndCourseId(semesterId, registration.getCourse().getId())
                        .stream()
                        .filter(cs -> cs.getStatus() != com.example.ThangLongUniversityWeb.enums.ClassSectionStatus.CANCELLED)
                        .toList();
                if (!activeSections.isEmpty()) {
                    var selected = activeSections.stream()
                            .min(java.util.Comparator.comparing(cs -> cs.getCurrentSlots() != null ? cs.getCurrentSlots() : 0))
                            .orElse(null);
                    registration.setClassSection(selected);
                }
            }
            examRegistrationRepository.save(registration);
        }

        semester.setRegistrationOpen(false);
        semester.setLocked(true);
        semesterRepository.save(semester);

        return pendingRetakes.size();
    }

    private AdminEnrollmentResponse toAdminResponse(Enrollment e) {
        var round = e.getClassSection().getRegistrationRound();
        return AdminEnrollmentResponse.builder()
                .enrollmentId(e.getId())
                .studentId(e.getStudent().getId())
                .studentCode(e.getStudent().getStudentCode())
                .studentName(e.getStudent().getFullName())
                .classSectionId(e.getClassSection().getId())
                .classCode(e.getClassSection().getClassCode())
                .semesterId(e.getClassSection().getSemester().getId())
                .semesterName(e.getClassSection().getSemester().getName())
                .courseName(e.getClassSection().getCourse().getName())
                .courseCode(e.getClassSection().getCourse().getCode())
                .credits(e.getClassSection().getCourse().getCredits())
                .enrolledAt(e.getEnrolledAt() != null ? e.getEnrolledAt().toString() : null)
                .status(e.getStatus() == null ? null : e.getStatus().name())
                .registrationRoundName(round != null ? round.getName() : null)
                .registrationRoundNumber(round != null ? round.getRoundNumber() : null)
                .build();
    }
}
