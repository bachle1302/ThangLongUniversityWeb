package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.dto.request.ClassSectionRequest;
import com.example.ThangLongUniversityWeb.dto.request.ClassSectionScheduleRequest;
import com.example.ThangLongUniversityWeb.dto.request.ExamScheduleRequest;
import com.example.ThangLongUniversityWeb.dto.response.AdminClassSectionStudentResponse;
import com.example.ThangLongUniversityWeb.dto.response.ClassSectionResponse;
import com.example.ThangLongUniversityWeb.dto.response.ClassSectionScheduleResponse;
import com.example.ThangLongUniversityWeb.dto.response.ClassSectionValidationIssueResponse;
import com.example.ThangLongUniversityWeb.dto.response.ClassSectionValidationResponse;
import com.example.ThangLongUniversityWeb.dto.response.ExamScheduleResponse;
import com.example.ThangLongUniversityWeb.entity.ClassSection;
import com.example.ThangLongUniversityWeb.entity.ClassSectionSchedule;
import com.example.ThangLongUniversityWeb.entity.Course;
import com.example.ThangLongUniversityWeb.entity.Period;
import com.example.ThangLongUniversityWeb.entity.RegistrationRound;
import com.example.ThangLongUniversityWeb.entity.Room;
import com.example.ThangLongUniversityWeb.entity.Semester;
import com.example.ThangLongUniversityWeb.entity.Teacher;
import com.example.ThangLongUniversityWeb.enums.EnrollmentStatus;
import com.example.ThangLongUniversityWeb.enums.ClassSectionStatus;
import com.example.ThangLongUniversityWeb.enums.TeacherStatus;
import com.example.ThangLongUniversityWeb.repository.ClassSectionRepository;
import com.example.ThangLongUniversityWeb.repository.ClassSectionScheduleRepository;
import com.example.ThangLongUniversityWeb.repository.CourseRepository;
import com.example.ThangLongUniversityWeb.repository.EnrollmentRepository;
import com.example.ThangLongUniversityWeb.repository.PeriodRepository;
import com.example.ThangLongUniversityWeb.repository.RegistrationRoundRepository;
import com.example.ThangLongUniversityWeb.repository.RoomRepository;
import com.example.ThangLongUniversityWeb.repository.SemesterRepository;
import com.example.ThangLongUniversityWeb.repository.TeacherRepository;
import com.example.ThangLongUniversityWeb.repository.GradeRepository;
import com.example.ThangLongUniversityWeb.repository.AttendanceRecordRepository;
import com.example.ThangLongUniversityWeb.enums.CourseStudyStatus;
import com.example.ThangLongUniversityWeb.enums.AttendanceStatus;
import com.example.ThangLongUniversityWeb.entity.Grade;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassSectionService {

    private final ClassSectionRepository classSectionRepository;
    private final ClassSectionScheduleRepository scheduleRepository;
    private final CourseRepository courseRepository;
    private final SemesterRepository semesterRepository;
    private final TeacherRepository teacherRepository;
    private final PeriodRepository periodRepository;
    private final RoomRepository roomRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final RegistrationRoundRepository registrationRoundRepository;
    private final RegistrationRoundService registrationRoundService;
    private final GradeRepository gradeRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final SemesterRealtimeService semesterRealtimeService;

    public void checkScheduleConflict(ClassSectionRequest request, Long semesterId, Long excludeId) {
        ClassSectionValidationResponse validation = validateClassSection(request, semesterId, excludeId);
        if (!validation.isValid()) {
            throw new RuntimeException(validation.getErrors().get(0).getMessage());
        }
    }

    public ClassSectionValidationResponse validateClassSection(ClassSectionRequest request, Long semesterId, Long excludeId) {
        List<ClassSectionValidationIssueResponse> errors = new ArrayList<>();
        List<ClassSectionValidationIssueResponse> warnings = new ArrayList<>();
        List<ClassSectionValidationIssueResponse> infos = new ArrayList<>();
        Long effectiveSemesterId = semesterId != null ? semesterId : request.getSemesterId();

        if (request.getClassCode() != null && !request.getClassCode().isBlank()) {
            String classCode = request.getClassCode().trim();
            classSectionRepository.findBySemesterIdAndClassCode(effectiveSemesterId, classCode)
                    .filter(existing -> excludeId == null || !existing.getId().equals(excludeId))
                    .ifPresent(existing -> errors.add(issue("DUPLICATE_CLASS_CODE", "Ma lop hoc phan da ton tai trong hoc ky nay.")));
        }

        if (effectiveSemesterId != null && request.getCourseId() != null) {
            long courseClassCount = classSectionRepository
                    .findBySemesterIdAndCourseId(effectiveSemesterId, request.getCourseId())
                    .stream()
                    .filter(section -> excludeId == null || !section.getId().equals(excludeId))
                    .count();
            if (courseClassCount > 0) {
                infos.add(issue("COURSE_ALREADY_HAS_CLASSES", "Mon hoc nay da co " + courseClassCount + " lop trong hoc ky."));
            }
        }

        validateTeacherDepartmentForRequest(request, errors);

        if (request.getSchedules() == null || request.getSchedules().isEmpty()) {
            errors.add(issue("MISSING_SCHEDULE", "Can khai bao it nhat mot lich hoc."));
        } else {
            validateInternalScheduleOverlaps(request.getSchedules(), errors);
            for (ClassSectionScheduleRequest scheduleReq : request.getSchedules()) {
                validateOneSchedule(request, effectiveSemesterId, excludeId, scheduleReq, errors);
            }
        }

        return ClassSectionValidationResponse.builder()
                .valid(errors.isEmpty())
                .errors(errors)
                .warnings(warnings)
                .infos(infos)
                .build();
    }

    private void validateOneSchedule(
            ClassSectionRequest request,
            Long semesterId,
            Long excludeId,
            ClassSectionScheduleRequest scheduleReq,
            List<ClassSectionValidationIssueResponse> errors
    ) {
        try {
            Room room = getRoomOrThrow(scheduleReq.getRoomId());
            validateRoomCapacity(room, request.getMaxSlots());

            Period startPeriod = getPeriodOrThrow(scheduleReq.getStartPeriodId(), "startPeriodId");
            Period endPeriod = getPeriodOrThrow(scheduleReq.getEndPeriodId(), "endPeriodId");
            validatePeriodOrder(startPeriod, endPeriod);

            Integer startNumber = startPeriod.getPeriodNumber();
            Integer endNumber = endPeriod.getPeriodNumber();

            if (semesterId != null && scheduleRepository.countRoomConflicts(
                    semesterId, room.getId(), scheduleReq.getDayOfWeek(), startNumber, endNumber, excludeId) > 0) {
                errors.add(issue("ROOM_CONFLICT", "Phong " + room.getName() + " da duoc su dung trong khung gio nay."));
            }

            if (semesterId != null && request.getTeacherId() != null && !scheduleRepository.findTeacherConflicts(
                    semesterId, request.getTeacherId(), scheduleReq.getDayOfWeek(), startNumber, endNumber, excludeId).isEmpty()) {
                errors.add(issue("TEACHER_CONFLICT", "Giang vien da co lop trong khung gio nay."));
            }
        } catch (RuntimeException ex) {
            errors.add(issue("INVALID_SCHEDULE", ex.getMessage()));
        }
    }

    private void validateInternalScheduleOverlaps(
            List<ClassSectionScheduleRequest> scheduleRequests,
            List<ClassSectionValidationIssueResponse> errors
    ) {
        try {
            for (int i = 0; i < scheduleRequests.size(); i++) {
                ClassSectionScheduleRequest current = scheduleRequests.get(i);
                Period currentStart = getPeriodOrThrow(current.getStartPeriodId(), "startPeriodId");
                Period currentEnd = getPeriodOrThrow(current.getEndPeriodId(), "endPeriodId");
                for (int j = i + 1; j < scheduleRequests.size(); j++) {
                    ClassSectionScheduleRequest other = scheduleRequests.get(j);
                    if (!current.getDayOfWeek().equals(other.getDayOfWeek())) {
                        continue;
                    }
                    Period otherStart = getPeriodOrThrow(other.getStartPeriodId(), "startPeriodId");
                    Period otherEnd = getPeriodOrThrow(other.getEndPeriodId(), "endPeriodId");
                    if (isPeriodOverlap(
                            currentStart.getPeriodNumber(),
                            currentEnd.getPeriodNumber(),
                            otherStart.getPeriodNumber(),
                            otherEnd.getPeriodNumber())) {
                        errors.add(issue("CLASS_SCHEDULE_OVERLAP", "Cac buoi hoc trong cung lop bi trung khung gio."));
                        return;
                    }
                }
            }
        } catch (RuntimeException ex) {
            errors.add(issue("INVALID_SCHEDULE", ex.getMessage()));
        }
    }

    private ClassSectionValidationIssueResponse issue(String code, String message) {
        return ClassSectionValidationIssueResponse.builder()
                .code(code)
                .message(message)
                .build();
    }

    public void checkStudentScheduleConflict(Long studentId, Long classSectionId) {
        ClassSection targetClass = classSectionRepository.findById(classSectionId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay lop hoc phan."));

        List<ClassSection> enrolledClasses = enrollmentRepository.findCurrentSelectedOrRegisteredClasses(
                studentId, targetClass.getSemester().getId());

        for (ClassSectionSchedule targetSchedule : targetClass.getSchedules()) {
            for (ClassSection enrolledClass : enrolledClasses) {
                for (ClassSectionSchedule enrolledSchedule : enrolledClass.getSchedules()) {
                    if (targetSchedule.getDayOfWeek().equals(enrolledSchedule.getDayOfWeek())
                            && isPeriodOverlap(
                            targetSchedule.getStartPeriod().getPeriodNumber(),
                            targetSchedule.getEndPeriod().getPeriodNumber(),
                            enrolledSchedule.getStartPeriod().getPeriodNumber(),
                            enrolledSchedule.getEndPeriod().getPeriodNumber())) {
                        throw new RuntimeException("Lich hoc bi trung voi lop da chon/dang ky.");
                    }
                }
            }
        }
    }

    @Transactional
    @CacheEvict(cacheNames = {"adminDashboard", "classSectionOptions"}, allEntries = true)
    public ClassSectionResponse createClassSection(ClassSectionRequest request) {
        Semester semester = semesterRepository.findByIdForUpdate(request.getSemesterId())
                .orElseThrow(() -> new RuntimeException("Khong tim thay hoc ky."));

        if (classSectionRepository.findBySemesterIdAndClassCode(request.getSemesterId(), request.getClassCode().trim()).isPresent()) {
            throw new RuntimeException("Ma lop hoc phan da ton tai trong hoc ky nay.");
        }

        checkScheduleConflict(request, request.getSemesterId(), null);

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Khong tim thay mon hoc."));
        Teacher teacher = getTeacherOrNull(request.getTeacherId());
        validateTeacherDepartment(course, teacher);
        RegistrationRound registrationRound = resolveRegistrationRound(request, semester.getId());

        ClassSection section = new ClassSection();
        section.setClassCode(request.getClassCode().trim());
        section.setCourse(course);
        section.setSemester(semester);
        section.setRegistrationRound(registrationRound);
        section.setTeacher(teacher);
        section.setMaxSlots(request.getMaxSlots());
        section.setCurrentSlots(0);
        section.setStatus(registrationRound != null && registrationRound.isRegistrationOpen()
                ? ClassSectionStatus.OPEN
                : ClassSectionStatus.DRAFT);

        ClassSection saved = classSectionRepository.save(section);
        replaceSchedules(saved, request.getSchedules());
        saved = classSectionRepository.findById(saved.getId()).orElseThrow();
        semesterRealtimeService.publishAfterCommit(semester.getId(), "CLASS_SECTIONS");
        return mapToResponse(saved);
    }

    @Transactional
    @CacheEvict(cacheNames = {"adminDashboard", "classSectionOptions"}, allEntries = true)
    public ClassSectionResponse updateClassSection(Long id, ClassSectionRequest request) {
        ClassSection section = classSectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay lop hoc phan."));

        checkScheduleConflict(request, section.getSemester().getId(), id);

        Teacher teacher = getTeacherOrNull(request.getTeacherId());
        validateTeacherDepartment(section.getCourse(), teacher);
        section.setTeacher(teacher);
        section.setMaxSlots(request.getMaxSlots());

        if (request.getRegistrationRoundId() != null) {
            RegistrationRound round = registrationRoundRepository.findById(request.getRegistrationRoundId())
                    .orElseThrow(() -> new RuntimeException("Khong tim thay dot dang ky."));
            if (!round.getSemester().getId().equals(section.getSemester().getId())) {
                throw new RuntimeException("Dot dang ky khong thuoc hoc ky cua lop hoc phan.");
            }
            if (round.isLocked()) {
                throw new RuntimeException("Dot dang ky da chot, khong the chuyen lop vao dot nay.");
            }
            section.setRegistrationRound(round);
        }

        scheduleRepository.deleteAll(section.getSchedules());
        section.getSchedules().clear();
        replaceSchedules(section, request.getSchedules());

        ClassSection saved = classSectionRepository.save(section);
        semesterRealtimeService.publishAfterCommit(section.getSemester().getId(), "CLASS_SECTIONS");
        return mapToResponse(saved);
    }

    @Transactional
    @CacheEvict(cacheNames = {"adminDashboard", "classSectionOptions"}, allEntries = true)
    public void deleteClassSection(Long id) {
        ClassSection section = classSectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay lop hoc phan."));
        Long semesterId = section.getSemester().getId();
        try {
            classSectionRepository.delete(section);
            classSectionRepository.flush();
            semesterRealtimeService.publishAfterCommit(semesterId, "CLASS_SECTIONS");
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException("Khong the xoa lop hoc phan vi da co du lieu lien quan.");
        }
    }

    @Transactional
    @CacheEvict(cacheNames = {"adminDashboard", "classSectionOptions"}, allEntries = true)
    public ClassSectionResponse cancelClassSection(Long id) {
        ClassSection section = classSectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay lop hoc phan."));
        if (section.getStatus() == ClassSectionStatus.CANCELLED) {
            return mapToResponse(section);
        }

        var activeEnrollments = enrollmentRepository.findByClassSectionId(section.getId()).stream()
                .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.PENDING
                        || enrollment.getStatus() == EnrollmentStatus.REGISTERED)
                .toList();
        for (var enrollment : activeEnrollments) {
            enrollment.setStatus(EnrollmentStatus.CANCELED);
            enrollmentRepository.save(enrollment);
        }

        section.setStatus(ClassSectionStatus.CANCELLED);
        section.setCurrentSlots((int) enrollmentRepository.countByClassSectionIdAndStatusIn(
                section.getId(), List.of(EnrollmentStatus.PENDING, EnrollmentStatus.REGISTERED)));
        ClassSection saved = classSectionRepository.save(section);
        semesterRealtimeService.publishAfterCommit(section.getSemester().getId(), "CLASS_SECTIONS");
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AdminClassSectionStudentResponse> getClassSectionStudents(Long classSectionId) {
        ClassSection section = classSectionRepository.findById(classSectionId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay lop hoc phan."));

        return enrollmentRepository.findByClassSectionId(section.getId()).stream()
                .map(enrollment -> {
                    var student = enrollment.getStudent();
                    var major = student.getMajor();
                    var user = student.getUser();
                    
                    String displayStatus = "Đang học";
                    CourseStudyStatus courseStatus = enrollment.getCourseStatus();
                    
                    if (courseStatus == CourseStudyStatus.BANNED_FROM_EXAM) {
                        displayStatus = "Cấm thi";
                    } else if (courseStatus == CourseStudyStatus.REPEAT_COURSE) {
                        displayStatus = "Học lại";
                    } else if (courseStatus == CourseStudyStatus.RETAKE_EXAM) {
                        displayStatus = "Thi lại";
                    } else if (courseStatus == CourseStudyStatus.PASSED) {
                        displayStatus = "Qua môn";
                    } else {
                        long absences = attendanceRecordRepository.countByEnrollmentIdAndStatus(
                                enrollment.getId(), AttendanceStatus.ABSENT);
                        if (absences > 3) {
                            displayStatus = "Cấm thi";
                        } else {
                            Grade grade = enrollment.getGrade();
                            if (grade != null && grade.getParticipationScore() != null && grade.getMidtermScore() != null) {
                                float preFinalAvg = grade.getParticipationScore() * 0.25f + grade.getMidtermScore() * 0.75f;
                                if (preFinalAvg < 4.0f) {
                                    displayStatus = "Học lại";
                                } else {
                                    displayStatus = "Đủ điều kiện thi";
                                }
                            } else {
                                long courseId = section.getCourse().getId();
                                List<Grade> prevGrades = gradeRepository.findByStudentId(student.getId()).stream()
                                    .filter(g -> g.getEnrollment().getClassSection().getCourse().getId().equals(courseId))
                                    .filter(g -> !g.getEnrollment().getId().equals(enrollment.getId()))
                                    .toList();
                                if (!prevGrades.isEmpty()) {
                                    displayStatus = "Học lại";
                                }
                            }
                        }
                    }

                    return AdminClassSectionStudentResponse.builder()
                            .enrollmentId(enrollment.getId())
                            .studentId(student.getId())
                            .studentCode(student.getStudentCode())
                            .fullName(student.getFullName())
                            .email(user != null ? user.getEmail() : null)
                            .majorId(major != null ? major.getId() : null)
                            .majorCode(major != null ? major.getMajorCode() : null)
                            .majorName(major != null ? major.getName() : null)
                            .cohort(student.getCohort())
                            .academicYear(student.getAcademicYear())
                            .enrolledAt(enrollment.getEnrolledAt() != null ? enrollment.getEnrolledAt().toString() : null)
                            .status(displayStatus)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public ClassSectionResponse mapToResponse(ClassSection section) {
        List<ClassSectionScheduleResponse> schedules = section.getSchedules().stream()
                .map(this::mapScheduleToResponse)
                .collect(Collectors.toList());
        int activeSlots = section.getId() == null
                ? (section.getCurrentSlots() == null ? 0 : section.getCurrentSlots())
                : (int) enrollmentRepository.countByClassSectionIdAndStatusIn(
                section.getId(), List.of(EnrollmentStatus.PENDING, EnrollmentStatus.REGISTERED));

        var distinctRooms = schedules.stream()
                .map(ClassSectionScheduleResponse::getRoomName)
                .distinct()
                .toList();

        String topRoomName = null;
        Long topRoomId = null;
        Integer topRoomCapacity = null;
        if (distinctRooms.size() == 1 && !schedules.isEmpty()) {
            topRoomName = schedules.get(0).getRoomName();
            topRoomId = schedules.get(0).getRoomId();
            topRoomCapacity = section.getRoom() != null ? section.getRoom().getCapacity() : null;
        }

        return ClassSectionResponse.builder()
                .id(section.getId())
                .classCode(section.getClassCode())
                .courseId(section.getCourse().getId())
                .courseCode(section.getCourse().getCode())
                .courseName(section.getCourse().getName())
                .majorName(section.getCourse().getMajor() != null ? section.getCourse().getMajor().getName() : "Dai cuong")
                .courseType(section.getCourse().getCourseType())
                .courseTypeLabel(section.getCourse().getCourseType() != null
                        && section.getCourse().getCourseType().name().equals("ELECTIVE") ? "Tu do" : "Bat buoc")
                .credits(section.getCourse().getCredits())
                .semesterId(section.getSemester().getId())
                .semesterName(section.getSemester().getName())
                .registrationRoundId(section.getRegistrationRound() != null ? section.getRegistrationRound().getId() : null)
                .registrationRoundName(section.getRegistrationRound() != null ? section.getRegistrationRound().getName() : null)
                .registrationRoundNumber(section.getRegistrationRound() != null ? section.getRegistrationRound().getRoundNumber() : null)
                .teacherId(section.getTeacher() != null ? section.getTeacher().getId() : null)
                .teacherName(section.getTeacher() != null ? section.getTeacher().getFullName() : "Chua phan cong")
                .room(topRoomName)
                .roomId(topRoomId)
                .roomCapacity(topRoomCapacity)
                .schedules(schedules)
                .maxSlots(section.getMaxSlots())
                .currentSlots(activeSlots)
                .status(section.getStatus().name())
                .isClosed(section.isClosed())
                .gradeLocked(section.isGradeLocked())
                .examAt(section.getExamAt())
                .examRoom(section.getExamRoom())
                .examType(section.getExamType())
                .sourceExamSessionId(section.getSourceExamSession() != null ? section.getSourceExamSession().getId() : null)
                .virtualRetakeClass(section.getSourceExamSession() != null)
                .semesterEnded(section.getSemester() != null && section.getSemester().isEnded())
                .build();
    }

    private ClassSectionScheduleResponse mapScheduleToResponse(ClassSectionSchedule schedule) {
        return ClassSectionScheduleResponse.builder()
                .id(schedule.getId())
                .dayOfWeek(schedule.getDayOfWeek())
                .startPeriodId(schedule.getStartPeriod().getId())
                .startPeriod(schedule.getStartPeriod().getPeriodNumber())
                .endPeriodId(schedule.getEndPeriod().getId())
                .endPeriod(schedule.getEndPeriod().getPeriodNumber())
                .lessonCount(schedule.getEndPeriod().getPeriodNumber() - schedule.getStartPeriod().getPeriodNumber() + 1)
                .periodRange(schedule.getStartPeriod().getPeriodNumber() + "-" + schedule.getEndPeriod().getPeriodNumber())
                .startTime(schedule.getStartPeriod().getStartTime())
                .endTime(schedule.getEndPeriod().getEndTime())
                .roomId(schedule.getRoom() != null ? schedule.getRoom().getId() : null)
                .roomName(schedule.getRoom() != null ? schedule.getRoom().getName() : null)
                .build();
    }

    @Transactional
    @CacheEvict(cacheNames = {"adminDashboard"}, allEntries = true)
    public ExamScheduleResponse updateExamSchedule(Long classSectionId, ExamScheduleRequest request) {
        ClassSection section = classSectionRepository.findById(classSectionId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay lop hoc phan."));
        section.setExamAt(request.getExamAt());
        section.setExamRoom(request.getExamRoom());
        if (request.getExamType() != null) {
            section.setExamType(request.getExamType());
        }
        classSectionRepository.save(section);
        return toExamScheduleResponse(section);
    }

    @Transactional
    @CacheEvict(cacheNames = {"adminDashboard"}, allEntries = true)
    public List<ExamScheduleResponse> batchUpdateExamSchedules(Long semesterId, List<ExamScheduleRequest> requests) {
        return requests.stream().map(req -> {
            ClassSection section = classSectionRepository.findById(req.getClassSectionId())
                    .orElseThrow(() -> new RuntimeException("Khong tim thay lop hoc phan id=" + req.getClassSectionId()));
            if (!section.getSemester().getId().equals(semesterId)) {
                throw new RuntimeException("Lop hoc phan khong thuoc hoc ky " + semesterId);
            }
            section.setExamAt(req.getExamAt());
            section.setExamRoom(req.getExamRoom());
            if (req.getExamType() != null) {
                section.setExamType(req.getExamType());
            }
            classSectionRepository.save(section);
            return toExamScheduleResponse(section);
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExamScheduleResponse> getExamSchedulesBySemester(Long semesterId) {
        return classSectionRepository.findBySemesterId(semesterId).stream()
                .map(this::toExamScheduleResponse)
                .collect(Collectors.toList());
    }

    private ExamScheduleResponse toExamScheduleResponse(ClassSection section) {
        int studentCount = (int) enrollmentRepository.countByClassSectionIdAndStatusIn(
                section.getId(), List.of(EnrollmentStatus.PENDING, EnrollmentStatus.REGISTERED));
        return ExamScheduleResponse.builder()
                .classSectionId(section.getId())
                .classCode(section.getClassCode())
                .courseName(section.getCourse().getName())
                .courseCode(section.getCourse().getCode())
                .credits(section.getCourse().getCredits())
                .teacherName(section.getTeacher() != null ? section.getTeacher().getFullName() : "Chua phan cong")
                .examAt(section.getExamAt())
                .examRoom(section.getExamRoom())
                .examType(section.getExamType())
                .studentCount(studentCount)
                .semesterId(section.getSemester().getId())
                .semesterName(section.getSemester().getName())
                .build();
    }

    private void replaceSchedules(ClassSection section, List<ClassSectionScheduleRequest> scheduleRequests) {
        for (ClassSectionScheduleRequest scheduleReq : scheduleRequests) {
            ClassSectionSchedule schedule = new ClassSectionSchedule();
            schedule.setClassSection(section);
            schedule.setDayOfWeek(scheduleReq.getDayOfWeek());
            schedule.setStartPeriod(getPeriodOrThrow(scheduleReq.getStartPeriodId(), "startPeriodId"));
            schedule.setEndPeriod(getPeriodOrThrow(scheduleReq.getEndPeriodId(), "endPeriodId"));
            schedule.setRoom(getRoomOrThrow(scheduleReq.getRoomId()));
            scheduleRepository.save(schedule);
            section.getSchedules().add(schedule);
        }
        if (!section.getSchedules().isEmpty()) {
            section.setRoom(section.getSchedules().get(0).getRoom());
        }
    }

    private RegistrationRound resolveRegistrationRound(ClassSectionRequest request, Long semesterId) {
        if (request.getRegistrationRoundId() != null) {
            RegistrationRound round = registrationRoundRepository.findById(request.getRegistrationRoundId())
                    .orElseThrow(() -> new RuntimeException("Khong tim thay dot dang ky."));
            if (!round.getSemester().getId().equals(semesterId)) {
                throw new RuntimeException("Dot dang ky khong thuoc hoc ky da chon.");
            }
            if (round.isLocked()) {
                throw new RuntimeException("Dot dang ky da chot, khong the tao lop moi vao dot nay.");
            }
            return round;
        }
        RegistrationRound openRound = registrationRoundService.getOpenRound(semesterId);
        return openRound;
    }

    private Teacher getTeacherOrNull(Long teacherId) {
        if (teacherId == null) {
            return null;
        }
        return teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay giang vien."));
    }

    private void validateTeacherDepartmentForRequest(ClassSectionRequest request, List<ClassSectionValidationIssueResponse> errors) {
        if (request.getCourseId() == null || request.getTeacherId() == null) {
            return;
        }
        try {
            Course course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new RuntimeException("Khong tim thay mon hoc."));
            Teacher teacher = getTeacherOrNull(request.getTeacherId());
            validateTeacherDepartment(course, teacher);
        } catch (RuntimeException ex) {
            errors.add(issue("TEACHER_DEPARTMENT_MISMATCH", ex.getMessage()));
        }
    }

    private void validateTeacherDepartment(Course course, Teacher teacher) {
        if (teacher == null) {
            return;
        }
        if (teacher.getStatus() != TeacherStatus.DANG_GIANG_DAY) {
            throw new RuntimeException("Giang vien khong o trang thai dang giang day.");
        }
        if (course == null || course.getMajor() == null || course.getMajor().getDepartment() == null) {
            return;
        }
        if (teacher.getDepartment() == null) {
            throw new RuntimeException("Giang vien chua duoc gan khoa, khong the phan cong cho hoc phan theo nganh.");
        }
        Long courseDepartmentId = course.getMajor().getDepartment().getId();
        Long teacherDepartmentId = teacher.getDepartment().getId();
        if (!courseDepartmentId.equals(teacherDepartmentId)) {
            throw new RuntimeException("Giang vien khong thuoc cung khoa voi nganh cua hoc phan.");
        }
    }

    private Period getPeriodOrThrow(Long periodId, String fieldName) {
        if (periodId == null) {
            throw new RuntimeException("Thieu " + fieldName);
        }
        return periodRepository.findById(periodId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay tiet hoc id=" + periodId));
    }

    private Room getRoomOrThrow(Long roomId) {
        if (roomId == null) {
            throw new RuntimeException("Thieu roomId");
        }
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay phong hoc."));
    }

    private void validateRoomCapacity(Room room, Integer maxSlots) {
        if (!"AVAILABLE".equalsIgnoreCase(room.getStatus())) {
            throw new RuntimeException("Phong " + room.getName() + " hien khong kha dung.");
        }
        if (maxSlots != null && room.getCapacity() != null && maxSlots > room.getCapacity()) {
            throw new RuntimeException("Si so lop vuot qua suc chua phong " + room.getName() + ".");
        }
    }

    private void validatePeriodOrder(Period startPeriod, Period endPeriod) {
        if (startPeriod.getPeriodNumber() > endPeriod.getPeriodNumber()) {
            throw new RuntimeException("Tiet bat dau phai nho hon hoac bang tiet ket thuc.");
        }
    }

    private boolean isPeriodOverlap(Integer start1, Integer end1, Integer start2, Integer end2) {
        return start1 <= end1 && start2 <= end2 && start1 <= end2 && start2 <= end1;
    }
}
