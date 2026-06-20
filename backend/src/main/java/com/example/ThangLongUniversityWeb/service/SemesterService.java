package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.dto.request.SemesterRequest;
import com.example.ThangLongUniversityWeb.dto.response.SemesterSummaryResponse;
import com.example.ThangLongUniversityWeb.dto.response.StudentSemesterResponse;
import com.example.ThangLongUniversityWeb.entity.RegistrationRound;
import com.example.ThangLongUniversityWeb.entity.Semester;
import com.example.ThangLongUniversityWeb.enums.EnrollmentStatus;
import com.example.ThangLongUniversityWeb.repository.ClassSectionRepository;
import com.example.ThangLongUniversityWeb.repository.EnrollmentRepository;
import com.example.ThangLongUniversityWeb.repository.ExamRegistrationRepository;
import com.example.ThangLongUniversityWeb.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SemesterService {

    private final SemesterRepository semesterRepository;
    private final ClassSectionRepository classSectionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ExamRegistrationRepository examRegistrationRepository;
    private final RegistrationRoundService registrationRoundService;
    private final SemesterRealtimeService semesterRealtimeService;

    @Cacheable(cacheNames = "semesters")
    @Transactional(readOnly = true)
    public List<StudentSemesterResponse> getAllSemesters() {
        return semesterRepository.findAll().stream()
                .map(this::toStudentResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StudentSemesterResponse> getAllSemestersReadOnly() {
        return semesterRepository.findAll().stream()
                .map(this::toStudentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(cacheNames = {"semesters", "classSectionOptions", "adminDashboard"}, allEntries = true)
    public StudentSemesterResponse createSemester(SemesterRequest request) {
        if (semesterRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Tên học kỳ đã tồn tại.");
        }

        Semester semester = new Semester();
        semester.setName(request.getName());
        semester.setStartDate(request.getStartDate());
        semester.setEndDate(request.getEndDate());
        semester.setRegistrationOpen(request.isRegistrationOpen());
        Semester saved = semesterRepository.save(semester);
        registrationRoundService.ensureDefaultRound(saved.getId(), "COURSE");
        registrationRoundService.ensureDefaultRound(saved.getId(), "RETAKE");
        return toStudentResponse(saved);
    }

    @Transactional
    @CacheEvict(cacheNames = {"semesters", "classSectionOptions", "adminDashboard"}, allEntries = true)
    public StudentSemesterResponse updateSemester(Long id, SemesterRequest request) {
        Semester semester = getSemesterOrThrow(id);

        semester.setName(request.getName());
        semester.setStartDate(request.getStartDate());
        semester.setEndDate(request.getEndDate());

        return toStudentResponse(semesterRepository.save(semester));
    }

    @Transactional
    @CacheEvict(cacheNames = {"semesters", "classSectionOptions", "adminDashboard"}, allEntries = true)
    public void deleteSemester(Long id) {
        long classSectionCount = classSectionRepository.countBySemesterId(id);
        if (classSectionCount > 0) {
            throw new RuntimeException("Không thể xóa học kỳ đã có lớp học phần.");
        }
        try {
            semesterRepository.deleteById(id);
            semesterRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException("Không thể xóa học kỳ vì đã được tham chiếu bởi dữ liệu liên quan.");
        }
    }

    @Transactional
    @CacheEvict(cacheNames = {"semesters", "classSectionOptions", "adminDashboard"}, allEntries = true)
    public StudentSemesterResponse toggleRegistration(Long id, boolean open) {
        RegistrationRound round = registrationRoundService.ensureDefaultRound(id);
        if (open) {
            registrationRoundService.openRound(id, round.getId());
        } else if (round.isRegistrationOpen()) {
            registrationRoundService.closeRound(id, round.getId());
        }
        semesterRealtimeService.publishAfterCommit(id, "SEMESTER_STATUS");
        return toStudentResponse(getSemesterOrThrow(id));
    }

    @Transactional
    @CacheEvict(cacheNames = {"semesters", "classSectionOptions", "adminDashboard"}, allEntries = true)
    public int lockEnrollments(Long id) {
        Semester semester = getSemesterOrThrow(id);
        
        List<RegistrationRound> rounds = registrationRoundService.listRoundsRaw(id, "COURSE");
        int count = 0;
        for (RegistrationRound r : rounds) {
            if (!r.isLocked()) {
                count += registrationRoundService.lockRound(id, r.getId());
            }
        }
        
        semester.setRegistrationOpen(false);
        semester.setLocked(true);
        semesterRepository.save(semester);
        semesterRealtimeService.publishAfterCommit(id, "SEMESTER_STATUS");
        return count;
    }

    @Transactional
    @CacheEvict(cacheNames = {"semesters", "classSectionOptions", "adminDashboard"}, allEntries = true)
    public StudentSemesterResponse publishExamSchedules(Long id) {
        Semester semester = getSemesterOrThrow(id);
        if (!semester.isLocked()) {
            throw new RuntimeException("Phai khoa tong dang ky hoc phan truoc khi cong bo lich thi.");
        }
        if (!semester.isRetakeLocked()) {
            throw new RuntimeException("Phai khoa tong dang ky thi lai truoc khi cong bo lich thi.");
        }
        semester.setExamPublished(true);
        return toStudentResponse(semesterRepository.save(semester));
    }

    @Transactional
    @CacheEvict(cacheNames = {"semesters", "classSectionOptions", "adminDashboard"}, allEntries = true)
    public StudentSemesterResponse unpublishExamSchedules(Long id) {
        Semester semester = getSemesterOrThrow(id);
        semester.setExamPublished(false);
        return toStudentResponse(semesterRepository.save(semester));
    }

    @Transactional
    @CacheEvict(cacheNames = {"semesters", "classSectionOptions", "adminDashboard"}, allEntries = true)
    public StudentSemesterResponse toggleRetakeRegistration(Long id, boolean open) {
        Semester semester = getSemesterOrThrow(id);
        if (open && !semester.isLocked()) {
            throw new RuntimeException("Phải chốt học phần trước khi mở đăng ký thi lại.");
        }
        if (semester.isRetakeLocked()) {
            throw new RuntimeException("Đăng ký thi lại đã chốt, không thể thay đổi.");
        }
        if (open) {
            RegistrationRound round = registrationRoundService.ensureDefaultRound(id, "RETAKE");
            registrationRoundService.openRound(id, round.getId());
        } else {
            RegistrationRound openRound = registrationRoundService.getOpenRound(id, "RETAKE");
            if (openRound != null) {
                registrationRoundService.closeRound(id, openRound.getId());
            } else {
                semester.setRetakeOpen(false);
                semesterRepository.save(semester);
            }
        }

        semesterRealtimeService.publishAfterCommit(id, "SEMESTER_STATUS");
        return toStudentResponse(getSemesterOrThrow(id));
    }

    @Transactional
    @CacheEvict(cacheNames = {"semesters", "classSectionOptions", "adminDashboard"}, allEntries = true)
    public int lockRetakes(Long id) {
        Semester semester = getSemesterOrThrow(id);
        if (!semester.isLocked()) {
            throw new RuntimeException("Phai khoa tong dang ky hoc phan truoc khi khoa tong thi lai.");
        }
        
        List<RegistrationRound> rounds = registrationRoundService.listRoundsRaw(id, "RETAKE");
        int count = 0;
        for (RegistrationRound r : rounds) {
            if (!r.isLocked()) {
                count += registrationRoundService.lockRound(id, r.getId());
            }
        }
        
        semester.setRetakeOpen(false);
        semester.setRetakeLocked(true);
        semesterRepository.save(semester);
        semesterRealtimeService.publishAfterCommit(id, "SEMESTER_STATUS");
        return count;
    }

    @Transactional
    @CacheEvict(cacheNames = {"semesters", "classSectionOptions", "adminDashboard"}, allEntries = true)
    public StudentSemesterResponse endSemester(Long id) {
        Semester semester = getSemesterOrThrow(id);
        if (!semester.isLocked()) {
            throw new RuntimeException("Phai khoa tong dang ky hoc phan truoc khi ket thuc hoc ky.");
        }
        if (!semester.isRetakeLocked()) {
            throw new RuntimeException("Phai khoa tong dang ky thi lai truoc khi ket thuc hoc ky.");
        }
        if (!semester.isExamPublished()) {
            throw new RuntimeException("Phai cong bo lich thi truoc khi ket thuc hoc ky.");
        }
        RegistrationRound openRound = registrationRoundService.getOpenRound(id);
        if (openRound != null) {
            registrationRoundService.closeRound(id, openRound.getId());
        }
        semester.setRegistrationOpen(false);
        semester.setRetakeOpen(false);
        semester.setEnded(true);
        StudentSemesterResponse response = toStudentResponse(semesterRepository.save(semester));
        semesterRealtimeService.publishAfterCommit(id, "SEMESTER_STATUS");
        return response;
    }

    public SemesterSummaryResponse getSemesterSummary(Long id) {
        registrationRoundService.ensureDefaultRound(id);
        Semester semester = getSemesterOrThrow(id);
        var classSections = classSectionRepository.findBySemesterId(id);

        int examScheduled = (int) classSections.stream().filter(cs -> cs.getExamAt() != null).count();
        int examNotScheduled = classSections.size() - examScheduled;

        var enrollments = enrollmentRepository.findByClassSectionSemesterId(id);
        int pending = (int) enrollments.stream().filter(e -> e.getStatus() == EnrollmentStatus.PENDING).count();
        int registered = (int) enrollments.stream().filter(e -> e.getStatus() == EnrollmentStatus.REGISTERED).count();

        var retakes = examRegistrationRepository.findBySemesterIdAndStatus(id, EnrollmentStatus.PENDING);
        var retakesReg = examRegistrationRepository.findBySemesterIdAndStatus(id, EnrollmentStatus.REGISTERED);
        var rounds = registrationRoundService.listRounds(id);
        RegistrationRound activeRound = registrationRoundService.getOpenRound(id);
        if (activeRound == null) {
            activeRound = registrationRoundService.ensureDefaultRound(id);
        }

        return SemesterSummaryResponse.builder()
                .semesterId(semester.getId())
                .name(semester.getName())
                .startDate(semester.getStartDate() != null ? semester.getStartDate().toString() : null)
                .endDate(semester.getEndDate() != null ? semester.getEndDate().toString() : null)
                .classSectionCount(classSections.size())
                .examScheduledCount(examScheduled)
                .examNotScheduledCount(examNotScheduled)
                .enrollmentCount(enrollments.size())
                .pendingEnrollments(pending)
                .registeredEnrollments(registered)
                .retakeRegistrations(retakes.size() + retakesReg.size())
                .retakePending(retakes.size())
                .retakeRegistered(retakesReg.size())
                .registrationOpen(semester.isRegistrationOpen())
                .locked(semester.isLocked())
                .examPublished(semester.isExamPublished())
                .retakeOpen(semester.isRetakeOpen())
                .retakeLocked(semester.isRetakeLocked())
                .ended(semester.isEnded())
                .maxCreditsPerSemester(semester.getMaxCreditsPerSemester())
                .activeRegistrationRoundId(activeRound.getId())
                .activeRegistrationRoundName(activeRound.getName())
                .activeRegistrationRoundNumber(activeRound.getRoundNumber())
                .registrationRoundCount(rounds.size())
                .build();
    }

    private Semester getSemesterOrThrow(Long id) {
        return semesterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học kỳ."));
    }

    public StudentSemesterResponse toStudentResponse(Semester s) {
        RegistrationRound activeRound = registrationRoundService.getOpenRound(s.getId());
        return StudentSemesterResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .startDate(s.getStartDate())
                .endDate(s.getEndDate())
                .registrationOpen(s.isRegistrationOpen())
                .locked(s.isLocked())
                .examPublished(s.isExamPublished())
                .retakeOpen(s.isRetakeOpen())
                .retakeLocked(s.isRetakeLocked())
                .ended(s.isEnded())
                .activeRegistrationRoundId(activeRound != null ? activeRound.getId() : null)
                .activeRegistrationRoundName(activeRound != null ? activeRound.getName() : null)
                .activeRegistrationRoundNumber(activeRound != null ? activeRound.getRoundNumber() : null)
                .build();
    }
}
