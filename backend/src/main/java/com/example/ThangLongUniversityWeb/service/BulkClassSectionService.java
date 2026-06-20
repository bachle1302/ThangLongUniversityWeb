package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.dto.request.BulkClassSectionCourseRequest;
import com.example.ThangLongUniversityWeb.dto.request.BulkClassSectionProposalRequest;
import com.example.ThangLongUniversityWeb.dto.request.BulkClassSectionRequest;
import com.example.ThangLongUniversityWeb.dto.request.ClassSectionRequest;
import com.example.ThangLongUniversityWeb.dto.request.ClassSectionScheduleRequest;
import com.example.ThangLongUniversityWeb.dto.response.BulkClassSectionCourseSummaryResponse;
import com.example.ThangLongUniversityWeb.dto.response.BulkClassSectionProposalResponse;
import com.example.ThangLongUniversityWeb.dto.response.BulkClassSectionValidationItemResponse;
import com.example.ThangLongUniversityWeb.dto.response.BulkClassSectionValidationResponse;
import com.example.ThangLongUniversityWeb.dto.response.ClassSectionResponse;
import com.example.ThangLongUniversityWeb.dto.response.ClassSectionValidationIssueResponse;
import com.example.ThangLongUniversityWeb.dto.response.ClassSectionValidationResponse;
import com.example.ThangLongUniversityWeb.entity.ClassSection;
import com.example.ThangLongUniversityWeb.entity.ClassSectionSchedule;
import com.example.ThangLongUniversityWeb.entity.Course;
import com.example.ThangLongUniversityWeb.entity.Period;
import com.example.ThangLongUniversityWeb.entity.Room;
import com.example.ThangLongUniversityWeb.entity.Teacher;
import com.example.ThangLongUniversityWeb.enums.ClassSectionStatus;
import com.example.ThangLongUniversityWeb.enums.TeacherStatus;
import com.example.ThangLongUniversityWeb.exception.ValidationException;
import com.example.ThangLongUniversityWeb.repository.ClassSectionRepository;
import com.example.ThangLongUniversityWeb.repository.CourseRepository;
import com.example.ThangLongUniversityWeb.repository.PeriodRepository;
import com.example.ThangLongUniversityWeb.repository.RoomRepository;
import com.example.ThangLongUniversityWeb.repository.SemesterRepository;
import com.example.ThangLongUniversityWeb.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BulkClassSectionService {

    private static final List<Integer> PROPOSAL_DAYS = List.of(2, 3, 4, 5, 6, 7);
    private static final long MAX_PERIOD_BREAK_MINUTES = 30;

    private final ClassSectionService classSectionService;
    private final ClassSectionRepository classSectionRepository;
    private final CourseRepository courseRepository;
    private final SemesterRepository semesterRepository;
    private final TeacherRepository teacherRepository;
    private final RoomRepository roomRepository;
    private final PeriodRepository periodRepository;

    @Transactional(readOnly = true)
    public BulkClassSectionProposalResponse propose(BulkClassSectionProposalRequest request) {
        var semester = semesterRepository.findById(request.getSemesterId())
                .orElseThrow(() -> new RuntimeException("Khong tim thay hoc ky."));
        List<Period> periods = periodRepository.findAll().stream()
                .sorted(Comparator.comparing(Period::getPeriodNumber))
                .toList();
        List<Room> rooms = roomRepository.findAll().stream()
                .filter(room -> "AVAILABLE".equalsIgnoreCase(room.getStatus()))
                .sorted(Comparator.comparing(Room::getCapacity).thenComparing(Room::getName))
                .toList();
        List<Teacher> teachers = teacherRepository.findAll().stream()
                .filter(teacher -> teacher.getStatus() == TeacherStatus.DANG_GIANG_DAY)
                .toList();
        List<ClassSection> existingSections = classSectionRepository.findBySemesterId(semester.getId());
        List<ScheduleSlot> occupied = existingSections.stream()
                .filter(section -> section.getStatus() != ClassSectionStatus.CANCELLED)
                .flatMap(section -> section.getSchedules().stream()
                        .map(schedule -> ScheduleSlot.from(section.getTeacher(), schedule)))
                .collect(Collectors.toCollection(ArrayList::new));
        Set<String> usedCodes = existingSections.stream()
                .map(ClassSection::getClassCode)
                .collect(Collectors.toCollection(HashSet::new));

        List<ClassSectionRequest> proposals = new ArrayList<>();
        List<BulkClassSectionCourseSummaryResponse> summaries = new ArrayList<>();
        for (BulkClassSectionCourseRequest config : request.getCourses()) {
            Course course = courseRepository.findById(config.getCourseId())
                    .orElseThrow(() -> new RuntimeException("Khong tim thay mon hoc id=" + config.getCourseId()));
            List<Teacher> compatibleTeachers = teachers.stream()
                    .filter(teacher -> isTeacherCompatible(course, teacher))
                    .sorted(Comparator.comparingLong(teacher -> countTeacherSlots(occupied, teacher.getId())))
                    .toList();
            int proposedBefore = proposals.size();

            for (int classIndex = 0; classIndex < config.getClassCount(); classIndex++) {
                ProposedAssignment assignment = findAssignment(
                        compatibleTeachers, rooms, periods, occupied, config);
                if (assignment == null) {
                    break;
                }
                String classCode = nextClassCode(course.getCode(), usedCodes);
                usedCodes.add(classCode);
                ClassSectionRequest proposal = new ClassSectionRequest();
                proposal.setClassCode(classCode);
                proposal.setCourseId(course.getId());
                proposal.setSemesterId(semester.getId());
                proposal.setTeacherId(assignment.teacher().getId());
                proposal.setMaxSlots(config.getMaxSlots());
                proposal.setSchedules(assignment.schedules().stream()
                        .map(ProposedSchedule::toRequest)
                        .toList());
                proposals.add(proposal);
                assignment.schedules().forEach(schedule -> occupied.add(new ScheduleSlot(
                        assignment.teacher().getId(),
                        schedule.room().getId(),
                        schedule.dayOfWeek(),
                        schedule.startPeriod().getPeriodNumber(),
                        schedule.endPeriod().getPeriodNumber())));
            }

            int proposedCount = proposals.size() - proposedBefore;
            int missingCount = config.getClassCount() - proposedCount;
            summaries.add(BulkClassSectionCourseSummaryResponse.builder()
                    .courseId(course.getId())
                    .courseCode(course.getCode())
                    .courseName(course.getName())
                    .requestedCount(config.getClassCount())
                    .proposedCount(proposedCount)
                    .missingCount(missingCount)
                    .message(missingCount == 0
                            ? "Da tim du lich hoc phu hop."
                            : "Khong du giang vien, phong hoac khung gio phu hop.")
                    .build());
        }
        return BulkClassSectionProposalResponse.builder()
                .items(proposals)
                .summaries(summaries)
                .build();
    }

    @Transactional(readOnly = true)
    public BulkClassSectionValidationResponse validate(BulkClassSectionRequest request) {
        List<BulkClassSectionValidationItemResponse> results = new ArrayList<>();
        List<ClassSectionRequest> accepted = new ArrayList<>();
        for (int index = 0; index < request.getItems().size(); index++) {
            ClassSectionRequest item = request.getItems().get(index);
            ClassSectionValidationResponse validation =
                    classSectionService.validateClassSection(item, item.getSemesterId(), null);
            List<ClassSectionValidationIssueResponse> errors =
                    new ArrayList<>(validation.getErrors());
            validateAgainstBatch(item, accepted, errors);
            ClassSectionValidationResponse combined = ClassSectionValidationResponse.builder()
                    .valid(errors.isEmpty())
                    .errors(errors)
                    .warnings(validation.getWarnings())
                    .infos(validation.getInfos())
                    .build();
            if (combined.isValid()) {
                accepted.add(item);
            }
            results.add(BulkClassSectionValidationItemResponse.builder()
                    .index(index)
                    .classCode(item.getClassCode())
                    .validation(combined)
                    .build());
        }
        return BulkClassSectionValidationResponse.builder()
                .valid(results.stream().allMatch(item -> item.getValidation().isValid()))
                .items(results)
                .build();
    }

    @Transactional
    @CacheEvict(cacheNames = {"adminDashboard", "classSectionOptions"}, allEntries = true)
    public List<ClassSectionResponse> create(BulkClassSectionRequest request) {
        Long semesterId = request.getItems().get(0).getSemesterId();
        if (request.getItems().stream().anyMatch(item -> !semesterId.equals(item.getSemesterId()))) {
            throw new ValidationException("Tat ca lop trong mot lan tao phai thuoc cung hoc ky.");
        }
        semesterRepository.findByIdForUpdate(semesterId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay hoc ky."));
        BulkClassSectionValidationResponse validation = validate(request);
        if (!validation.isValid()) {
            String message = validation.getItems().stream()
                    .flatMap(item -> item.getValidation().getErrors().stream())
                    .map(ClassSectionValidationIssueResponse::getMessage)
                    .findFirst()
                    .orElse("De xuat tao lop khong con hop le.");
            throw new ValidationException(message);
        }
        List<ClassSectionResponse> created = new ArrayList<>();
        for (ClassSectionRequest item : request.getItems()) {
            created.add(classSectionService.createClassSection(item));
        }
        classSectionRepository.flush();
        return created;
    }

    private ProposedAssignment findAssignment(
            List<Teacher> teachers,
            List<Room> rooms,
            List<Period> periods,
            List<ScheduleSlot> occupied,
            BulkClassSectionCourseRequest config
    ) {
        List<PeriodWindow> windows = buildPeriodWindows(periods, config.getPeriodsPerSession());
        for (Teacher teacher : teachers) {
            List<ProposedSchedule> schedules = new ArrayList<>();
            for (Integer day : PROPOSAL_DAYS) {
                ProposedSchedule schedule = findScheduleForDay(
                        teacher, day, rooms, windows, occupied, config.getMaxSlots());
                if (schedule != null) {
                    schedules.add(schedule);
                    if (schedules.size() == config.getSessionsPerWeek()) {
                        return new ProposedAssignment(teacher, schedules);
                    }
                }
            }
        }
        return null;
    }

    private ProposedSchedule findScheduleForDay(
            Teacher teacher,
            Integer day,
            List<Room> rooms,
            List<PeriodWindow> windows,
            List<ScheduleSlot> occupied,
            Integer maxSlots
    ) {
        for (PeriodWindow window : windows) {
            boolean teacherBusy = occupied.stream().anyMatch(slot ->
                    teacher.getId().equals(slot.teacherId())
                            && day.equals(slot.dayOfWeek())
                            && overlaps(window.start().getPeriodNumber(), window.end().getPeriodNumber(),
                            slot.startPeriod(), slot.endPeriod()));
            if (teacherBusy) {
                continue;
            }
            for (Room room : rooms) {
                if (room.getCapacity() < maxSlots) {
                    continue;
                }
                boolean roomBusy = occupied.stream().anyMatch(slot ->
                        room.getId().equals(slot.roomId())
                                && day.equals(slot.dayOfWeek())
                                && overlaps(window.start().getPeriodNumber(), window.end().getPeriodNumber(),
                                slot.startPeriod(), slot.endPeriod()));
                if (!roomBusy) {
                    return new ProposedSchedule(day, room, window.start(), window.end());
                }
            }
        }
        return null;
    }

    private List<PeriodWindow> buildPeriodWindows(List<Period> periods, int length) {
        List<PeriodWindow> windows = new ArrayList<>();
        for (int start = 0; start + length <= periods.size(); start++) {
            List<Period> candidate = periods.subList(start, start + length);
            boolean continuous = true;
            for (int index = 1; index < candidate.size(); index++) {
                Period previous = candidate.get(index - 1);
                Period current = candidate.get(index);
                long breakMinutes = Duration.between(previous.getEndTime(), current.getStartTime()).toMinutes();
                if (current.getPeriodNumber() != previous.getPeriodNumber() + 1
                        || breakMinutes < 0
                        || breakMinutes > MAX_PERIOD_BREAK_MINUTES) {
                    continuous = false;
                    break;
                }
            }
            if (continuous) {
                windows.add(new PeriodWindow(candidate.get(0), candidate.get(candidate.size() - 1)));
            }
        }
        return windows;
    }

    private void validateAgainstBatch(
            ClassSectionRequest current,
            List<ClassSectionRequest> accepted,
            List<ClassSectionValidationIssueResponse> errors
    ) {
        for (ClassSectionRequest other : accepted) {
            if (current.getClassCode().trim().equalsIgnoreCase(other.getClassCode().trim())) {
                errors.add(issue("DUPLICATE_CLASS_CODE_IN_BATCH",
                        "Ma lop hoc phan bi trung trong danh sach de xuat."));
            }
            for (ClassSectionScheduleRequest currentSchedule : current.getSchedules()) {
                for (ClassSectionScheduleRequest otherSchedule : other.getSchedules()) {
                    if (!currentSchedule.getDayOfWeek().equals(otherSchedule.getDayOfWeek())) {
                        continue;
                    }
                    Period currentStart = getPeriod(currentSchedule.getStartPeriodId());
                    Period currentEnd = getPeriod(currentSchedule.getEndPeriodId());
                    Period otherStart = getPeriod(otherSchedule.getStartPeriodId());
                    Period otherEnd = getPeriod(otherSchedule.getEndPeriodId());
                    if (!overlaps(currentStart.getPeriodNumber(), currentEnd.getPeriodNumber(),
                            otherStart.getPeriodNumber(), otherEnd.getPeriodNumber())) {
                        continue;
                    }
                    if (currentSchedule.getRoomId().equals(otherSchedule.getRoomId())) {
                        errors.add(issue("ROOM_CONFLICT_IN_BATCH",
                                "Phong hoc bi trung voi lop " + other.getClassCode() + " trong cung de xuat."));
                    }
                    if (current.getTeacherId() != null
                            && current.getTeacherId().equals(other.getTeacherId())) {
                        errors.add(issue("TEACHER_CONFLICT_IN_BATCH",
                                "Giang vien bi trung lich voi lop " + other.getClassCode() + " trong cung de xuat."));
                    }
                }
            }
        }
    }

    private Period getPeriod(Long periodId) {
        return periodRepository.findById(periodId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay tiet hoc id=" + periodId));
    }

    private ClassSectionValidationIssueResponse issue(String code, String message) {
        return ClassSectionValidationIssueResponse.builder().code(code).message(message).build();
    }

    private boolean isTeacherCompatible(Course course, Teacher teacher) {
        if (course.getMajor() == null || course.getMajor().getDepartment() == null) {
            return true;
        }
        return teacher.getDepartment() != null
                && course.getMajor().getDepartment().getId().equals(teacher.getDepartment().getId());
    }

    private long countTeacherSlots(List<ScheduleSlot> occupied, Long teacherId) {
        return occupied.stream().filter(slot -> teacherId.equals(slot.teacherId())).count();
    }

    private String nextClassCode(String courseCode, Set<String> usedCodes) {
        int next = 1;
        while (usedCodes.contains(courseCode + "-" + String.format("%02d", next))) {
            next++;
        }
        return courseCode + "-" + String.format("%02d", next);
    }

    private boolean overlaps(int start1, int end1, int start2, int end2) {
        return start1 <= end2 && start2 <= end1;
    }

    private record PeriodWindow(Period start, Period end) {}

    private record ProposedSchedule(
            Integer dayOfWeek,
            Room room,
            Period startPeriod,
            Period endPeriod
    ) {
        private ClassSectionScheduleRequest toRequest() {
            ClassSectionScheduleRequest request = new ClassSectionScheduleRequest();
            request.setDayOfWeek(dayOfWeek);
            request.setRoomId(room.getId());
            request.setStartPeriodId(startPeriod.getId());
            request.setEndPeriodId(endPeriod.getId());
            return request;
        }
    }

    private record ProposedAssignment(Teacher teacher, List<ProposedSchedule> schedules) {}

    private record ScheduleSlot(
            Long teacherId,
            Long roomId,
            Integer dayOfWeek,
            Integer startPeriod,
            Integer endPeriod
    ) {
        private static ScheduleSlot from(Teacher teacher, ClassSectionSchedule schedule) {
            return new ScheduleSlot(
                    teacher != null ? teacher.getId() : null,
                    schedule.getRoom().getId(),
                    schedule.getDayOfWeek(),
                    schedule.getStartPeriod().getPeriodNumber(),
                    schedule.getEndPeriod().getPeriodNumber());
        }
    }
}
