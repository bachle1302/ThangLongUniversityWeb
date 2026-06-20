package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.dto.request.GradeRequest;
import com.example.ThangLongUniversityWeb.dto.request.TeacherRequest;
import com.example.ThangLongUniversityWeb.dto.request.TeacherUpdateRequest;
import com.example.ThangLongUniversityWeb.dto.response.ClassSectionResponse;
import com.example.ThangLongUniversityWeb.dto.response.StudentGradeResponse;
import com.example.ThangLongUniversityWeb.dto.response.StudentSemesterResponse;
import com.example.ThangLongUniversityWeb.dto.response.TeacherResponse;
import com.example.ThangLongUniversityWeb.audit.Audit;
import com.example.ThangLongUniversityWeb.entity.ClassSection;
import com.example.ThangLongUniversityWeb.entity.Department;
import com.example.ThangLongUniversityWeb.entity.Enrollment;
import com.example.ThangLongUniversityWeb.entity.Grade;
import com.example.ThangLongUniversityWeb.entity.Homeroom;
import com.example.ThangLongUniversityWeb.entity.Student;
import com.example.ThangLongUniversityWeb.entity.Teacher;
import com.example.ThangLongUniversityWeb.exception.ConflictException;
import com.example.ThangLongUniversityWeb.enums.EnrollmentStatus;
import com.example.ThangLongUniversityWeb.enums.EnrollmentType;
import com.example.ThangLongUniversityWeb.enums.Role;
import com.example.ThangLongUniversityWeb.entity.User;
import com.example.ThangLongUniversityWeb.repository.ClassSectionRepository;
import com.example.ThangLongUniversityWeb.repository.DepartmentRepository;
import com.example.ThangLongUniversityWeb.repository.EnrollmentRepository;
import com.example.ThangLongUniversityWeb.repository.GradeRepository;
import com.example.ThangLongUniversityWeb.repository.SemesterRepository;
import com.example.ThangLongUniversityWeb.repository.TeacherRepository;
import com.example.ThangLongUniversityWeb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final ClassSectionRepository classSectionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GradeRepository gradeRepository;
    private final ClassSectionService classSectionService;
    private final SemesterRepository semesterRepository;
    private final PasswordEncoder passwordEncoder;

    // ── Admin: danh sách + mapping ────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TeacherResponse> getAllTeachers() {
        return teacherRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TeacherResponse mapToResponse(Teacher teacher) {
        User user = teacher.getUser();
        Department dept = teacher.getDepartment();
        return TeacherResponse.builder()
                .id(teacher.getId())
                .username(user != null ? user.getUsername() : null)
                .email(user != null ? user.getEmail() : null)
                .teacherCode(teacher.getTeacherCode())
                .fullName(teacher.getFullName())
                .dob(teacher.getDob())
                .gender(teacher.getGender())
                .phone(teacher.getPhone())
                .nationalId(teacher.getNationalId())
                .placeOfBirth(teacher.getPlaceOfBirth())
                .hometown(teacher.getHometown())
                .permanentAddress(teacher.getPermanentAddress())
                .currentAddress(teacher.getCurrentAddress())
                .emergencyContact(teacher.getEmergencyContact())
                .departmentId(dept != null ? dept.getId() : null)
                .departmentCode(dept != null ? dept.getDepartmentCode() : null)
                .departmentName(dept != null ? dept.getName() : null)
                .degree(teacher.getDegree())
                .address(teacher.getAddress())
                .status(teacher.getStatus())
                .build();
    }

    @Transactional
    @CacheEvict(cacheNames = {"adminDashboard", "classSectionOptions", "teacherDashboard"}, allEntries = true)
    public TeacherResponse updateTeacherPartial(Long id, TeacherUpdateRequest request) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giảng viên!"));

        if (request.getFullName() != null) teacher.setFullName(request.getFullName());
        if (request.getDob() != null) teacher.setDob(request.getDob());
        if (request.getGender() != null) teacher.setGender(request.getGender());
        if (request.getPhone() != null) teacher.setPhone(request.getPhone());
        if (request.getNationalId() != null) teacher.setNationalId(request.getNationalId());
        if (request.getPlaceOfBirth() != null) teacher.setPlaceOfBirth(request.getPlaceOfBirth());
        if (request.getHometown() != null) teacher.setHometown(request.getHometown());
        if (request.getPermanentAddress() != null) teacher.setPermanentAddress(request.getPermanentAddress());
        if (request.getCurrentAddress() != null) teacher.setCurrentAddress(request.getCurrentAddress());
        if (request.getEmergencyContact() != null) teacher.setEmergencyContact(request.getEmergencyContact());
        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khoa/bộ môn!"));
            teacher.setDepartment(dept);
        }
        if (request.getDegree() != null) teacher.setDegree(request.getDegree());
        if (request.getAddress() != null) teacher.setAddress(request.getAddress());
        if (request.getStatus() != null) teacher.setStatus(request.getStatus());

        User user = teacher.getUser();
        if (user != null && request.getEmail() != null) {
            user.setEmail(request.getEmail());
            userRepository.save(user);
        }

        return mapToResponse(teacherRepository.save(teacher));
    }

    @Transactional
    @CacheEvict(cacheNames = {"adminDashboard", "classSectionOptions", "teacherDashboard"}, allEntries = true)
    public Teacher createTeacher(TeacherRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(Role.TEACHER);
        user.setActive(true);
        User savedUser = userRepository.save(user);

        Teacher teacher = new Teacher();
        teacher.setUser(savedUser);
        teacher.setTeacherCode(request.getTeacherCode());
        teacher.setFullName(request.getFullName());
        teacher.setDob(request.getDob());
        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khoa/bộ môn!"));
            teacher.setDepartment(dept);
        }
        teacher.setDegree(request.getDegree());
        teacher.setAddress(request.getAddress());
        teacher.setPhone(request.getPhone());
        return teacherRepository.save(teacher);
    }

    @Transactional
    @CacheEvict(cacheNames = {"adminDashboard", "classSectionOptions", "teacherDashboard"}, allEntries = true)
    public Teacher updateTeacher(Long id, TeacherRequest request) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giảng viên!"));

        teacher.setFullName(request.getFullName());
        teacher.setDob(request.getDob());
        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khoa/bộ môn!"));
            teacher.setDepartment(dept);
        }
        teacher.setDegree(request.getDegree());
        teacher.setAddress(request.getAddress());

        User user = teacher.getUser();
        user.setEmail(request.getEmail());
        userRepository.save(user);

        return teacherRepository.save(teacher);
    }

    @Transactional
    @CacheEvict(cacheNames = {"adminDashboard", "classSectionOptions", "teacherDashboard"}, allEntries = true)
    public void deleteTeacher(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giảng viên!"));

        List<ClassSection> assignedSections = classSectionRepository.findByTeacherId(teacher.getId());
        if (!assignedSections.isEmpty()) {
            throw new ConflictException("Không thể xóa giảng viên vì đã được phân công lớp học phần. Hãy gỡ phân công hoặc chuyển giảng viên trước khi xóa.");
        }

        User user = teacher.getUser();
        teacherRepository.delete(teacher);
        userRepository.delete(user);
    }

    // Lấy thông tin giảng viên đang đăng nhập
    private Teacher getCurrentTeacher() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return teacherRepository.findByUser_Username(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin giảng viên!"));
    }

    // 1. Xem danh sách lớp được phân công dạy trong kỳ
    @Transactional(readOnly = true)
    public List<ClassSectionResponse> getMyClasses(Long semesterId) {
        Teacher teacher = getCurrentTeacher();
        return classSectionRepository.findByTeacherIdAndSemesterId(teacher.getId(), semesterId)
                .stream()
                .map(classSectionService::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StudentSemesterResponse> getSemesters() {
        return semesterRepository.findAll().stream()
                .sorted(Comparator.comparing(semester -> semester.getStartDate() == null ? LocalDate.MIN : semester.getStartDate()))
                .map(semester -> StudentSemesterResponse.builder()
                        .id(semester.getId())
                        .name(semester.getName())
                        .startDate(semester.getStartDate())
                        .endDate(semester.getEndDate())
                        .registrationOpen(semester.isRegistrationOpen())
                        .locked(semester.isLocked())
                        .examPublished(semester.isExamPublished())
                        .retakeOpen(semester.isRetakeOpen())
                        .retakeLocked(semester.isRetakeLocked())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StudentGradeResponse> getStudentsInClass(Long classSectionId) {
        Teacher teacher = getCurrentTeacher();
        ClassSection classSection = classSectionRepository.findById(classSectionId)
                .orElseThrow(() -> new RuntimeException("Lớp học phần không tồn tại!"));

        // BẢO MẬT: Chặn nếu lớp này không phải do giảng viên này dạy
        if (!classSection.getTeacher().getId().equals(teacher.getId())) {
            throw new RuntimeException("Bạn không có quyền xem danh sách lớp của giảng viên khác!");
        }

        return enrollmentRepository.findByClassSectionId(classSectionId)
                .stream()
                .filter(enrollment -> enrollment.getStatus() != EnrollmentStatus.PENDING)
                .map(enrollment -> buildStudentGradeResponse(enrollment, enrollment.getGrade()))
                .collect(Collectors.toList());
    }

    // 3. Nhập điểm / Chấm điểm cho sinh viên
    @Transactional
    @Audit(action = "GRADE_UPDATE", targetType = "Enrollment")
    public StudentGradeResponse gradeStudent(Long enrollmentId, GradeRequest request) {
        Teacher teacher = getCurrentTeacher();
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi đăng ký của sinh viên!"));

        if (enrollment.getClassSection().getSemester().isLocked()) {
            throw new RuntimeException("Học kỳ đã bị khóa, không thể sửa điểm.");
        }

        // BẢO MẬT: Kiểm tra xem sinh viên này có học lớp do mình dạy không
        if (!enrollment.getClassSection().getTeacher().getId().equals(teacher.getId())) {
            throw new RuntimeException("Bạn không có quyền chấm điểm cho sinh viên lớp khác!");
        }

        // Lấy hoặc tạo Grade entity
        Grade grade = gradeRepository.findByEnrollmentId(enrollmentId).orElseGet(() -> {
            Grade g = new Grade();
            g.setEnrollment(enrollment);
            g.setAttemptNumber(1);
            g.setEnrollmentType(EnrollmentType.ORDINARY);
            return g;
        });

        validateScore(request.getMidTermScore(), "midTermScore");
        validateScore(request.getFinalScore(), "finalScore");
        grade.setMidtermScore(request.getMidTermScore());
        grade.setFinalScore(request.getFinalScore());
        if (request.getRetestScore() != null) {
            grade.setRetestScore(request.getRetestScore());
        }

        Grade savedGrade = gradeRepository.save(grade);

        return buildStudentGradeResponse(enrollment, savedGrade);
    }

    private StudentGradeResponse buildStudentGradeResponse(Enrollment enrollment, Grade grade) {
        Student student = enrollment.getStudent();
        Homeroom homeroom = student.getHomeroom();
        Teacher advisor = homeroom != null ? homeroom.getAdvisor() : null;

        return StudentGradeResponse.builder()
                .enrollmentId(enrollment.getId())
                .studentCode(student.getStudentCode())
                .fullName(student.getFullName())
                .phone(student.getPhone())
                .email(student.getUser() != null ? student.getUser().getEmail() : null)
                .className(homeroom != null ? homeroom.getClassName() : null)
                .advisorName(advisor != null ? advisor.getFullName() : null)
                .majorName(student.getMajor() != null ? student.getMajor().getName() : null)
                .facultyName(student.getCohort())
                .midTermScore(grade != null ? grade.getMidtermScore() : null)
                .finalScore(grade != null ? grade.getFinalScore() : null)
                .totalScore(grade != null ? grade.getTotalScore() : null)
                .status(enrollment.getStatus().name())
                .courseStatus(enrollment.getCourseStatus() != null ? enrollment.getCourseStatus().name() : null)
                .build();
    }

    private void validateScore(Float score, String field) {
        if (score == null) return;
        if (score < 0.0f || score > 10.0f) {
            throw new RuntimeException("Điểm " + field + " phải nằm trong khoảng [0, 10].");
        }
    }
}
