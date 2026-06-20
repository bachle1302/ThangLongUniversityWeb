package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.dto.response.AdminClassSectionOptionsResponse;
import com.example.ThangLongUniversityWeb.dto.response.AdminDashboardResponse;
import com.example.ThangLongUniversityWeb.dto.response.ClassSectionResponse;
import com.example.ThangLongUniversityWeb.dto.response.StudentSemesterResponse;
import com.example.ThangLongUniversityWeb.dto.response.TeacherDashboardResponse;
import com.example.ThangLongUniversityWeb.dto.response.UserProfileResponse;
import com.example.ThangLongUniversityWeb.entity.ClassSection;
import com.example.ThangLongUniversityWeb.entity.Course;
import com.example.ThangLongUniversityWeb.entity.Teacher;
import com.example.ThangLongUniversityWeb.entity.User;
import com.example.ThangLongUniversityWeb.enums.ClassSectionStatus;
import com.example.ThangLongUniversityWeb.enums.EnrollmentStatus;
import com.example.ThangLongUniversityWeb.repository.ClassSectionRepository;
import com.example.ThangLongUniversityWeb.repository.CourseRepository;
import com.example.ThangLongUniversityWeb.repository.DepartmentRepository;
import com.example.ThangLongUniversityWeb.repository.EnrollmentRepository;
import com.example.ThangLongUniversityWeb.repository.MajorRepository;
import com.example.ThangLongUniversityWeb.repository.RoomRepository;
import com.example.ThangLongUniversityWeb.repository.RegistrationRoundRepository;
import com.example.ThangLongUniversityWeb.repository.StudentRepository;
import com.example.ThangLongUniversityWeb.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final RoomRepository roomRepository;
    private final MajorRepository majorRepository;
    private final ClassSectionRepository classSectionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final RegistrationRoundRepository registrationRoundRepository;
    private final ClassSectionService classSectionService;
    private final CourseService courseService;
    private final SemesterService semesterService;
    private final TeacherService teacherService;
    private final RoomService roomService;
    private final PeriodService periodService;

    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = "adminDashboard",
            key = "#semesterId == null ? 'current' : #semesterId.toString()")
    public AdminDashboardResponse getAdminDashboard(Long semesterId) {
        StudentSemesterResponse currentSemester =
                resolveDashboardSemester(semesterService.getAllSemestersReadOnly(), semesterId);
        List<ClassSection> sections = currentSemester == null
                ? List.of()
                : classSectionRepository.findBySemesterId(currentSemester.getId());
        List<Course> courses = courseRepository.findAll();

        if (currentSemester != null) {
            boolean registrationOpen =
                    registrationRoundRepository.existsBySemesterIdAndRoundTypeAndRegistrationOpenTrue(
                            currentSemester.getId(), "COURSE");
            currentSemester.setRegistrationOpen(registrationOpen);
        }

        long classSectionCount = sections.size();
        long openClassCount = sections.stream()
                .filter(section -> section.getStatus() == ClassSectionStatus.OPEN && !section.isClosed())
                .count();
        long assignedClassCount = sections.stream()
                .filter(section -> section.getTeacher() != null)
                .count();
        long scheduledClassCount = sections.stream()
                .filter(section -> section.getSchedules() != null && !section.getSchedules().isEmpty())
                .count();
        long totalCapacity = sections.stream()
                .map(ClassSection::getMaxSlots)
                .filter(Objects::nonNull)
                .mapToLong(Integer::longValue)
                .sum();
        long pendingEnrollments = currentSemester == null ? 0
                : enrollmentRepository.countByClassSectionSemesterIdAndStatus(
                        currentSemester.getId(), EnrollmentStatus.PENDING);
        long registeredEnrollments = currentSemester == null ? 0
                : enrollmentRepository.countByClassSectionSemesterIdAndStatus(
                        currentSemester.getId(), EnrollmentStatus.REGISTERED);
        long registeredSlots = pendingEnrollments + registeredEnrollments;
        long roomCapacity = roomRepository.findAll().stream()
                .map(room -> room.getCapacity() == null ? 0L : room.getCapacity().longValue())
                .mapToLong(Long::longValue)
                .sum();

        List<AdminDashboardResponse.MajorStudentCount> studentsByMajor = majorRepository.findAll().stream()
                .map(major -> AdminDashboardResponse.MajorStudentCount.builder()
                        .majorId(major.getId())
                        .majorCode(major.getMajorCode())
                        .majorName(major.getName())
                        .studentCount(studentRepository.countByMajorId(major.getId()))
                        .build())
                .toList();

        List<ClassSectionResponse> attentionClasses = sections.stream()
                .filter(section -> section.getTeacher() == null
                        || section.getSchedules() == null
                        || section.getSchedules().isEmpty()
                        || occupancyRate(section) >= 90)
                .sorted(Comparator
                        .comparing((ClassSection section) -> section.getTeacher() != null)
                        .thenComparing(section -> section.getSchedules() != null && !section.getSchedules().isEmpty())
                        .thenComparing(section -> section.getMaxSlots() == null ? 0 : section.getMaxSlots()))
                .limit(6)
                .map(classSectionService::mapToResponse)
                .toList();

        List<ClassSectionResponse> recentClasses = sections.stream()
                .sorted(Comparator.comparing(
                        ClassSection::getId,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(6)
                .map(classSectionService::mapToResponse)
                .toList();

        return AdminDashboardResponse.builder()
                .currentSemester(currentSemester)
                .studentCount(studentRepository.count())
                .teacherCount(teacherRepository.count())
                .courseCount(courseRepository.count())
                .departmentCount(departmentRepository.count())
                .roomCount(roomRepository.count())
                .roomCapacity(roomCapacity)
                .classSectionCount(classSectionCount)
                .openClassCount(openClassCount)
                .assignedClassCount(assignedClassCount)
                .scheduledClassCount(scheduledClassCount)
                .totalRegisteredSlots(registeredSlots)
                .pendingEnrollmentCount(pendingEnrollments)
                .registeredEnrollmentCount(registeredEnrollments)
                .totalCapacity(totalCapacity)
                .totalCourseCredits(courses.stream()
                        .map(Course::getCredits)
                        .filter(Objects::nonNull)
                        .mapToLong(Integer::longValue)
                        .sum())
                .averageOccupancy(totalCapacity == 0 ? 0d : (registeredSlots * 100d) / totalCapacity)
                .assignedTeacherRate(classSectionCount == 0 ? 0d : (assignedClassCount * 100d) / classSectionCount)
                .scheduledClassRate(classSectionCount == 0 ? 0d : (scheduledClassCount * 100d) / classSectionCount)
                .studentsByMajor(studentsByMajor)
                .attentionClasses(attentionClasses)
                .recentClasses(recentClasses)
                .build();
    }

    private int occupancyRate(ClassSection section) {
        int current = section.getCurrentSlots() == null ? 0 : section.getCurrentSlots();
        int max = section.getMaxSlots() == null ? 0 : section.getMaxSlots();
        return max == 0 ? 0 : Math.min(100, Math.round(current * 100f / max));
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "teacherDashboard", key = "#username")
    public TeacherDashboardResponse getTeacherDashboard(String username) {
        Teacher teacher = teacherRepository.findByUser_Username(username)
                .orElseThrow(() -> new RuntimeException("Khong tim thay thong tin giang vien!"));
        List<StudentSemesterResponse> semesters = semesterService.getAllSemestersReadOnly();
        StudentSemesterResponse currentSemester = resolveCurrentSemester(semesters);
        List<ClassSectionResponse> classes = currentSemester == null
                ? List.of()
                : classSectionRepository.findByTeacherIdAndSemesterId(teacher.getId(), currentSemester.getId()).stream()
                .map(classSectionService::mapToResponse)
                .toList();

        int today = LocalDate.now().getDayOfWeek().getValue() + 1;
        List<ClassSectionResponse> todaySchedule = classes.stream()
                .filter(item -> item.getSchedules() != null && item.getSchedules().stream()
                        .anyMatch(slot -> Objects.equals(slot.getDayOfWeek(), today)))
                .toList();

        int totalStudents = classes.stream()
                .map(ClassSectionResponse::getCurrentSlots)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
        int ungradedClassCount = (int) classes.stream()
                .filter(item -> !Boolean.TRUE.equals(item.isGradeLocked()))
                .count();

        return TeacherDashboardResponse.builder()
                .profile(mapTeacherProfile(teacher))
                .currentSemester(currentSemester)
                .classes(classes)
                .todaySchedule(todaySchedule)
                .classCount(classes.size())
                .totalStudents(totalStudents)
                .ungradedClassCount(ungradedClassCount)
                .todayScheduleCount(todaySchedule.size())
                .build();
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "classSectionOptions")
    public AdminClassSectionOptionsResponse getClassSectionOptions() {
        return AdminClassSectionOptionsResponse.builder()
                .courses(courseService.getAllCourses())
                .semesters(semesterService.getAllSemestersReadOnly())
                .teachers(teacherService.getAllTeachers())
                .rooms(roomService.getAllRooms())
                .periods(periodService.getAllPeriods())
                .build();
    }

    private StudentSemesterResponse resolveCurrentSemester(List<StudentSemesterResponse> semesters) {
        return resolveDashboardSemester(semesters, null);
    }

    private StudentSemesterResponse resolveDashboardSemester(
            List<StudentSemesterResponse> semesters,
            Long semesterId) {
        if (semesters == null || semesters.isEmpty()) {
            return null;
        }
        if (semesterId != null) {
            return semesters.stream()
                    .filter(semester -> Objects.equals(semester.getId(), semesterId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy học kỳ."));
        }

        List<StudentSemesterResponse> sorted = semesters.stream()
                .sorted(Comparator.comparing(semester -> semester.getStartDate() == null
                        ? LocalDate.MIN
                        : semester.getStartDate()))
                .toList();
        LocalDate today = LocalDate.now();
        return sorted.stream()
                .filter(semester -> semester.getStartDate() != null
                        && semester.getEndDate() != null
                        && !today.isBefore(semester.getStartDate())
                        && !today.isAfter(semester.getEndDate()))
                .max(Comparator.comparing(StudentSemesterResponse::getStartDate))
                .or(() -> sorted.stream().filter(StudentSemesterResponse::isRegistrationOpen).findFirst())
                .or(() -> sorted.stream()
                        .filter(semester -> semester.getStartDate() != null && !semester.getStartDate().isAfter(today))
                        .max(Comparator.comparing(StudentSemesterResponse::getStartDate)))
                .orElse(sorted.get(sorted.size() - 1));
    }

    private UserProfileResponse mapTeacherProfile(Teacher teacher) {
        User user = teacher.getUser();
        String fullName = teacher.getFullName() != null && !teacher.getFullName().isBlank()
                ? teacher.getFullName()
                : user.getUsername();
        String avatarName = URLEncoder.encode(fullName, StandardCharsets.UTF_8);
        return UserProfileResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .fullName(fullName)
                .code(teacher.getTeacherCode())
                .majorOrDegree(teacher.getDegree())
                .department(teacher.getDepartment() != null ? teacher.getDepartment().getName() : null)
                .avatarUrl("https://ui-avatars.com/api/?name=" + avatarName + "&background=b11226&color=fff")
                .gender(teacher.getGender())
                .dateOfBirth(teacher.getDob() != null ? teacher.getDob().toString() : null)
                .nationalId(teacher.getNationalId())
                .placeOfBirth(teacher.getPlaceOfBirth())
                .hometown(teacher.getHometown())
                .permanentAddress(teacher.getPermanentAddress())
                .currentAddress(teacher.getCurrentAddress())
                .phone(teacher.getPhone())
                .emergencyContact(teacher.getEmergencyContact())
                .status(teacher.getStatus() != null ? teacher.getStatus().name() : null)
                .build();
    }
}
