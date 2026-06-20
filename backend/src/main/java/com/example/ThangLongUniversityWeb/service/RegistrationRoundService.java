package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.dto.request.RegistrationRoundRequest;
import com.example.ThangLongUniversityWeb.dto.request.RegistrationTimeSlotRequest;
import com.example.ThangLongUniversityWeb.dto.response.RegistrationRoundResponse;
import com.example.ThangLongUniversityWeb.dto.response.RegistrationTimeSlotResponse;
import com.example.ThangLongUniversityWeb.entity.Grade;
import com.example.ThangLongUniversityWeb.entity.RegistrationRound;
import com.example.ThangLongUniversityWeb.entity.RegistrationTimeSlot;
import com.example.ThangLongUniversityWeb.entity.Semester;
import com.example.ThangLongUniversityWeb.enums.ClassSectionStatus;
import com.example.ThangLongUniversityWeb.enums.EnrollmentStatus;
import com.example.ThangLongUniversityWeb.enums.EnrollmentType;
import com.example.ThangLongUniversityWeb.repository.ClassSectionRepository;
import com.example.ThangLongUniversityWeb.repository.EnrollmentRepository;
import com.example.ThangLongUniversityWeb.repository.GradeRepository;
import com.example.ThangLongUniversityWeb.repository.RegistrationRoundRepository;
import com.example.ThangLongUniversityWeb.repository.SemesterRepository;
import com.example.ThangLongUniversityWeb.repository.ExamRegistrationRepository;
import com.example.ThangLongUniversityWeb.repository.TuitionBillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistrationRoundService {
    private final RegistrationRoundRepository registrationRoundRepository;
    private final SemesterRepository semesterRepository;
    private final ClassSectionRepository classSectionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GradeRepository gradeRepository;
    private final ExamRegistrationRepository examRegistrationRepository;
    private final TuitionBillRepository tuitionBillRepository;
    private final SemesterRealtimeService semesterRealtimeService;

    @Transactional
    public RegistrationRound ensureDefaultRound(Long semesterId, String roundType) {
        String type = (roundType == null || roundType.isBlank()) ? "COURSE" : roundType;
        Semester semester = getSemesterOrThrow(semesterId);
        RegistrationRound round = registrationRoundRepository.findFirstBySemesterIdAndRoundTypeOrderByRoundNumberDesc(semesterId, type)
                .orElseGet(() -> {
                    RegistrationRound created = new RegistrationRound();
                    created.setSemester(semester);
                    created.setRoundNumber(1);
                    created.setName("Đợt 1");
                    created.setRoundType(type);
                    created.setRegistrationOpen(false);
                    created.setLocked(false);
                    return registrationRoundRepository.save(created);
                });
        return round;
    }

    @Transactional
    public RegistrationRound ensureDefaultRound(Long semesterId) {
        return ensureDefaultRound(semesterId, "COURSE");
    }

    @Transactional(readOnly = true)
    public RegistrationRound getOpenRound(Long semesterId, String roundType) {
        String type = (roundType == null || roundType.isBlank()) ? "COURSE" : roundType;
        return registrationRoundRepository
                .findFirstBySemesterIdAndRoundTypeAndRegistrationOpenTrueOrderByRoundNumberDesc(semesterId, type)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public RegistrationRound getOpenRound(Long semesterId) {
        return getOpenRound(semesterId, "COURSE");
    }

    @Transactional
    public List<RegistrationRoundResponse> listRounds(Long semesterId, String roundType) {
        String type = (roundType == null || roundType.isBlank()) ? "COURSE" : roundType;
        ensureDefaultRound(semesterId, type);
        return registrationRoundRepository.findBySemesterIdAndRoundTypeOrderByRoundNumberAsc(semesterId, type)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public List<RegistrationRoundResponse> listRounds(Long semesterId) {
        return listRounds(semesterId, "COURSE");
    }

    @Transactional(readOnly = true)
    public List<RegistrationRound> listRoundsRaw(Long semesterId, String roundType) {
        String type = (roundType == null || roundType.isBlank()) ? "COURSE" : roundType;
        return registrationRoundRepository.findBySemesterIdAndRoundTypeOrderByRoundNumberAsc(semesterId, type);
    }

    @Transactional
    @CacheEvict(cacheNames = {"semesters", "adminDashboard"}, allEntries = true)
    public RegistrationRoundResponse createRound(Long semesterId, RegistrationRoundRequest request) {
        Semester semester = getSemesterOrThrow(semesterId);
        String roundType = request != null && request.getRoundType() != null ? request.getRoundType() : "COURSE";
        ensureDefaultRound(semesterId, roundType);
        
        if (semester.isLocked()) {
            throw new RuntimeException("Học kỳ đã khóa đăng ký học phần, không thể tạo đợt mới.");
        }
        if (registrationRoundRepository.existsBySemesterIdAndRoundTypeAndRegistrationOpenTrue(semesterId, roundType)) {
            throw new RuntimeException("Đang có đợt đăng ký cùng loại đang mở. Hãy đóng hoặc chốt đợt hiện tại trước.");
        }

        int nextNumber = registrationRoundRepository.findMaxRoundNumberBySemesterIdAndRoundType(semesterId, roundType) + 1;
        RegistrationRound round = new RegistrationRound();
        round.setSemester(semester);
        round.setRoundNumber(nextNumber);
        round.setRoundType(roundType);
        round.setName(request != null && request.getName() != null && !request.getName().isBlank()
                ? request.getName().trim()
                : "Đợt " + nextNumber);
        round.setRegistrationOpen(false);
        round.setLocked(false);
        
        if (request != null && request.getTimeSlots() != null) {
            updateTimeSlots(round, request.getTimeSlots());
        }

        RegistrationRound saved = registrationRoundRepository.save(round);

        if (request != null && Boolean.TRUE.equals(request.getOpen())) {
            saved = openRoundInternal(semester, saved);
        }
        semesterRealtimeService.publishAfterCommit(semesterId, "REGISTRATION_ROUNDS");
        return toResponse(saved);
    }

    @Transactional
    @CacheEvict(cacheNames = {"semesters", "adminDashboard"}, allEntries = true)
    public RegistrationRoundResponse openRound(Long semesterId, Long roundId) {
        return openRound(semesterId, roundId, null);
    }

    @Transactional
    @CacheEvict(cacheNames = {"semesters", "adminDashboard"}, allEntries = true)
    public RegistrationRoundResponse openRound(Long semesterId, Long roundId, RegistrationRoundRequest request) {
        Semester semester = getSemesterOrThrow(semesterId);
        RegistrationRound round = getRoundOrThrow(roundId);
        assertRoundInSemester(round, semesterId);
        if (request != null && request.getTimeSlots() != null) {
            updateTimeSlots(round, request.getTimeSlots());
        }
        RegistrationRound saved = openRoundInternal(semester, round);
        semesterRealtimeService.publishAfterCommit(semesterId, "REGISTRATION_ROUNDS");
        return toResponse(saved);
    }

    @Transactional
    @CacheEvict(cacheNames = {"semesters", "adminDashboard"}, allEntries = true)
    public RegistrationRoundResponse closeRound(Long semesterId, Long roundId) {
        Semester semester = getSemesterOrThrow(semesterId);
        RegistrationRound round = getRoundOrThrow(roundId);
        assertRoundInSemester(round, semesterId);
        round.setRegistrationOpen(false);
        registrationRoundRepository.save(round);
        
        if ("COURSE".equals(round.getRoundType())) {
            classSectionRepository.updateStatusForRoundSections(
                round.getId(),
                ClassSectionStatus.CLOSED,
                List.of(ClassSectionStatus.OPEN)
            );
        }
        
        // Disable general semester open only if no other round is open
        boolean anyOpen = registrationRoundRepository.existsBySemesterIdAndRoundTypeAndRegistrationOpenTrue(semesterId, round.getRoundType());
        if (!anyOpen) {
            if ("COURSE".equals(round.getRoundType())) {
                semester.setRegistrationOpen(false);
            } else {
                semester.setRetakeOpen(false);
            }
            semesterRepository.save(semester);
        }
        semesterRealtimeService.publishAfterCommit(semesterId, "REGISTRATION_ROUNDS");
        return toResponse(round);
    }

    @Transactional
    @CacheEvict(cacheNames = {"semesters", "adminDashboard"}, allEntries = true)
    public int lockOpenRound(Long semesterId) {
        RegistrationRound round = registrationRoundRepository
                .findFirstBySemesterIdAndRoundTypeAndRegistrationOpenTrueOrderByRoundNumberDesc(semesterId, "COURSE")
                .orElseThrow(() -> new RuntimeException("Không có đợt đăng ký đang mở để chốt."));
        return lockRound(semesterId, round.getId());
    }

    @Transactional
    @CacheEvict(cacheNames = {"semesters", "adminDashboard"}, allEntries = true)
    public int lockRound(Long semesterId, Long roundId) {
        Semester semester = getSemesterOrThrow(semesterId);
        RegistrationRound round = getRoundOrThrow(roundId);
        assertRoundInSemester(round, semesterId);

        int count = 0;
        if ("COURSE".equals(round.getRoundType())) {
            var pending = enrollmentRepository.findByClassSectionRegistrationRoundIdAndStatus(roundId, EnrollmentStatus.PENDING);
            for (var enrollment : pending) {
                var cs = classSectionRepository.findByIdForUpdate(enrollment.getClassSection().getId())
                        .orElseThrow(() -> new RuntimeException("Khong tim thay lop hoc phan."));
                int currentSlots = cs.getCurrentSlots() == null ? 0 : cs.getCurrentSlots();
                if (cs.getMaxSlots() != null && currentSlots >= cs.getMaxSlots()) {
                    throw new RuntimeException("Lop " + cs.getClassCode() + " da day si so, khong the chot them dang ky.");
                }

                enrollment.setStatus(EnrollmentStatus.REGISTERED);
                var saved = enrollmentRepository.save(enrollment);
                saved.setClassSection(cs);
                cs.setCurrentSlots(currentSlots + 1);
                classSectionRepository.save(cs);

                if (saved.getGrade() == null) {
                    Grade grade = new Grade();
                    grade.setEnrollment(saved);
                    
                    long courseId = cs.getCourse().getId();
                    List<Grade> prevGrades = gradeRepository.findByStudentId(saved.getStudent().getId()).stream()
                        .filter(g -> g.getEnrollment().getClassSection().getCourse().getId().equals(courseId))
                        .toList();
                    int attempt = prevGrades.size() + 1;
                    grade.setAttemptNumber(attempt);
                    grade.setEnrollmentType(EnrollmentType.ORDINARY);
                    gradeRepository.save(grade);
                }
            }
            count = pending.size();
        } else {
            // For RETAKE: lock exam registrations in this round
            var pending = examRegistrationRepository.findByRegistrationRoundIdAndStatus(roundId, EnrollmentStatus.PENDING);
            for (var reg : pending) {
                // Tạm tách luồng thanh toán với thi ra riêng: Cho phép chốt đăng ký trước, thanh toán sau.
                // Do đó không cần kiểm tra bill.isCompleted() khi khóa đợt thi lại nữa.
                /*
                if (reg.getFeeCharged() != null && reg.getFeeCharged() > 0) {
                    var bill = tuitionBillRepository.findByStudentIdAndSemesterId(
                            reg.getStudent().getId(), semesterId).orElse(null);
                    if (bill == null || !bill.isCompleted()) {
                        throw new RuntimeException("Sinh vien " + reg.getStudent().getStudentCode()
                                + " chua thanh toan phi thi lai/nang diem.");
                    }
                }
                */
                reg.setStatus(EnrollmentStatus.REGISTERED);
                examRegistrationRepository.save(reg);
            }
            count = pending.size();
        }

        round.setRegistrationOpen(false);
        round.setLocked(true);
        round.setLockedAt(LocalDateTime.now());
        registrationRoundRepository.save(round);

        if ("COURSE".equals(round.getRoundType())) {
            classSectionRepository.updateStatusForRoundSections(
                roundId,
                ClassSectionStatus.CLOSED,
                List.of(ClassSectionStatus.OPEN, ClassSectionStatus.DRAFT)
            );
        }

        // Do not globally lock the entire semester here (Khóa tổng is done separately).
        boolean anyOpen = registrationRoundRepository.existsBySemesterIdAndRoundTypeAndRegistrationOpenTrue(semesterId, round.getRoundType());
        if (!anyOpen) {
            if ("COURSE".equals(round.getRoundType())) {
                semester.setRegistrationOpen(false);
            } else {
                semester.setRetakeOpen(false);
            }
            semesterRepository.save(semester);
        }

        semesterRealtimeService.publishAfterCommit(semesterId, "REGISTRATION_ROUNDS");
        return count;
    }

    public RegistrationRoundResponse toResponse(RegistrationRound round) {
        Long roundId = round.getId();
        int pending = 0;
        int registered = 0;
        if ("COURSE".equals(round.getRoundType())) {
            pending = roundId == null ? 0 :
                    enrollmentRepository.findByClassSectionRegistrationRoundIdAndStatus(roundId, EnrollmentStatus.PENDING).size();
            registered = roundId == null ? 0 :
                    enrollmentRepository.findByClassSectionRegistrationRoundIdAndStatus(roundId, EnrollmentStatus.REGISTERED).size();
        } else {
            pending = roundId == null ? 0 :
                    examRegistrationRepository.findByRegistrationRoundIdAndStatus(roundId, EnrollmentStatus.PENDING).size();
            registered = roundId == null ? 0 :
                    examRegistrationRepository.findByRegistrationRoundIdAndStatus(roundId, EnrollmentStatus.REGISTERED).size();
        }
        
        List<RegistrationTimeSlotResponse> timeSlotResponses = new ArrayList<>();
        if (round.getTimeSlots() != null) {
            timeSlotResponses = round.getTimeSlots().stream().map(ts -> RegistrationTimeSlotResponse.builder()
                    .id(ts.getId())
                    .startTime(ts.getStartTime())
                    .endTime(ts.getEndTime())
                    .allowedMajorIds(deserializeIds(ts.getAllowedMajorIds()))
                    .allowedCohorts(deserializeStrings(ts.getAllowedCohorts()))
                    .build()).toList();
        }
        
        return RegistrationRoundResponse.builder()
                .id(round.getId())
                .semesterId(round.getSemester().getId())
                .semesterName(round.getSemester().getName())
                .name(round.getName())
                .roundNumber(round.getRoundNumber())
                .registrationOpen(round.isRegistrationOpen())
                .locked(round.isLocked())
                .classSectionCount(roundId == null ? 0 : (int) classSectionRepository.countByRegistrationRoundId(roundId))
                .pendingEnrollments(pending)
                .registeredEnrollments(registered)
                .createdAt(round.getCreatedAt())
                .lockedAt(round.getLockedAt())
                .roundType(round.getRoundType())
                .timeSlots(timeSlotResponses)
                .build();
    }

    private RegistrationRound openRoundInternal(Semester semester, RegistrationRound round) {
        if (round.isLocked()) {
            throw new RuntimeException("Đợt đăng ký đã chốt, không thể mở lại. Hãy tạo đợt mới.");
        }
        if (registrationRoundRepository.existsBySemesterIdAndRoundTypeAndRegistrationOpenTrue(semester.getId(), round.getRoundType())
                && !round.isRegistrationOpen()) {
            throw new RuntimeException("Đang có đợt đăng ký khác cùng loại đang mở.");
        }
        round.setRegistrationOpen(true);
        RegistrationRound saved = registrationRoundRepository.save(round);
        if ("COURSE".equals(round.getRoundType())) {
            semester.setRegistrationOpen(true);
            semester.setLocked(false);
            
            // 1. Assign all DRAFT sections in the semester without a round to this round
            classSectionRepository.assignUnassignedDraftSectionsToRound(semester.getId(), saved);
            
            // 2. Transition all DRAFT and CLOSED sections of this round to OPEN
            classSectionRepository.updateStatusForRoundSections(
                saved.getId(), 
                ClassSectionStatus.OPEN, 
                List.of(ClassSectionStatus.DRAFT, ClassSectionStatus.CLOSED)
            );
        } else {
            semester.setRetakeOpen(true);
            semester.setRetakeLocked(false);
        }
        semesterRepository.save(semester);
        return saved;
    }

    private Semester getSemesterOrThrow(Long semesterId) {
        return semesterRepository.findById(semesterId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học kỳ."));
    }

    private RegistrationRound getRoundOrThrow(Long roundId) {
        return registrationRoundRepository.findById(roundId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đợt đăng ký."));
    }

    private void assertRoundInSemester(RegistrationRound round, Long semesterId) {
        if (!round.getSemester().getId().equals(semesterId)) {
            throw new RuntimeException("Đợt đăng ký không thuộc học kỳ này.");
        }
    }
    
    private void updateTimeSlots(RegistrationRound round, List<RegistrationTimeSlotRequest> requests) {
        if (round.getTimeSlots() == null) {
            round.setTimeSlots(new ArrayList<>());
        }
        round.getTimeSlots().clear();
        for (RegistrationTimeSlotRequest req : requests) {
            RegistrationTimeSlot ts = new RegistrationTimeSlot();
            ts.setRegistrationRound(round);
            ts.setStartTime(req.getStartTime() != null ? req.getStartTime() : LocalDateTime.now());
            ts.setEndTime(req.getEndTime() != null ? req.getEndTime() : LocalDateTime.now().plusDays(1));
            ts.setAllowedMajorIds(serializeIds(req.getAllowedMajorIds()));
            ts.setAllowedCohorts(serializeStrings(req.getAllowedCohorts()));
            round.getTimeSlots().add(ts);
        }
    }

    // Helper to serialize List<Long> -> Comma-separated String
    private String serializeIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return null;
        return ids.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    // Helper to serialize List<String> -> Comma-separated String
    private String serializeStrings(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        return list.stream().map(String::trim).collect(Collectors.joining(","));
    }

    // Helper to deserialize Comma-separated String -> List<Long>
    private List<Long> deserializeIds(String s) {
        if (s == null || s.isBlank()) return List.of();
        return java.util.Arrays.stream(s.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    // Helper to deserialize Comma-separated String -> List<String>
    private List<String> deserializeStrings(String s) {
        if (s == null || s.isBlank()) return List.of();
        return java.util.Arrays.stream(s.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
