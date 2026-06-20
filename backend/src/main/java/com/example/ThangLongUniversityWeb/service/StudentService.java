package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.dto.request.StudentRequest;
import com.example.ThangLongUniversityWeb.dto.request.StudentUpdateRequest;
import com.example.ThangLongUniversityWeb.dto.response.StudentResponse;
import com.example.ThangLongUniversityWeb.dto.response.UserProfileResponse;
import com.example.ThangLongUniversityWeb.entity.AcademicResult;
import com.example.ThangLongUniversityWeb.entity.Homeroom;
import com.example.ThangLongUniversityWeb.entity.Major;
import com.example.ThangLongUniversityWeb.entity.Student;
import com.example.ThangLongUniversityWeb.entity.Teacher;
import com.example.ThangLongUniversityWeb.entity.User;
import com.example.ThangLongUniversityWeb.enums.Role;
import com.example.ThangLongUniversityWeb.exception.ConflictException;
import com.example.ThangLongUniversityWeb.repository.AcademicResultRepository;
import com.example.ThangLongUniversityWeb.repository.EnrollmentRepository;
import com.example.ThangLongUniversityWeb.repository.HomeroomRepository;
import com.example.ThangLongUniversityWeb.repository.MajorRepository;
import com.example.ThangLongUniversityWeb.repository.StudentRepository;
import com.example.ThangLongUniversityWeb.repository.TuitionBillRepository;
import com.example.ThangLongUniversityWeb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Period;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final MajorRepository majorRepository;
    private final HomeroomRepository homeroomRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final TuitionBillRepository tuitionBillRepository;
    private final PasswordEncoder passwordEncoder;
    private final AcademicResultRepository academicResultRepository;
    private final AcademicResultService academicResultService;

    // 1. THÊM MỚI (Tạo User -> Tạo Student)
    @Transactional
    @CacheEvict(cacheNames = {"adminDashboard"}, allEntries = true)
    public StudentResponse createStudent(StudentRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }

        // Tạo User trước
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(Role.STUDENT);
        user.setActive(true);
        User savedUser = userRepository.save(user);

        // Tạo hồ sơ Student gắn với User vừa tạo
        Student student = new Student();
        student.setUser(savedUser);
        student.setStudentCode(request.getStudentCode());
        student.setFullName(request.getFullName());
        student.setDob(request.getDob());
        applyPersonalFields(student, request);
        student.setMajor(getMajorOrThrow(request.getMajorId()));
        student.setAcademicYear(request.getAcademicYear());
        student.setAddress(request.getAddress());

        // Gán lớp hành chính (không bắt buộc)
        if (request.getHomeroomId() != null) {
            Homeroom homeroom = homeroomRepository.findById(request.getHomeroomId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp hành chính!"));
            student.setHomeroom(homeroom);
        }

        return mapToResponse(studentRepository.save(student));
    }

    // 2. CẬP NHẬT (dùng StudentUpdateRequest, không đổi password)
    @Transactional
    @CacheEvict(cacheNames = {"adminDashboard"}, allEntries = true)
    public StudentResponse updateStudent(Long id, StudentUpdateRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên!"));

        if (request.getFullName() != null) student.setFullName(request.getFullName());
        if (request.getDob() != null) student.setDob(request.getDob());
        if (request.getGender() != null) student.setGender(request.getGender());
        if (request.getPhone() != null) student.setPhone(request.getPhone());
        if (request.getNationalId() != null) student.setNationalId(request.getNationalId());
        if (request.getPlaceOfBirth() != null) student.setPlaceOfBirth(request.getPlaceOfBirth());
        if (request.getHometown() != null) student.setHometown(request.getHometown());
        if (request.getPermanentAddress() != null) student.setPermanentAddress(request.getPermanentAddress());
        if (request.getCurrentAddress() != null) student.setCurrentAddress(request.getCurrentAddress());
        if (request.getEmergencyContact() != null) student.setEmergencyContact(request.getEmergencyContact());
        if (request.getAcademicYear() != null) student.setAcademicYear(request.getAcademicYear());
        if (request.getCohort() != null) student.setCohort(request.getCohort());
        if (request.getStatus() != null) student.setStatus(request.getStatus());
        if (request.getTrainingType() != null) student.setTrainingType(request.getTrainingType());
        if (request.getAddress() != null) student.setAddress(request.getAddress());
        if (request.getMajorId() != null) student.setMajor(getMajorOrThrow(request.getMajorId()));

        // Gán / chuyển lớp hành chính
        if (request.getHomeroomId() != null) {
            Homeroom homeroom = homeroomRepository.findById(request.getHomeroomId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp hành chính!"));
            student.setHomeroom(homeroom);
        }

        if (request.getEmail() != null) {
            User user = student.getUser();
            user.setEmail(request.getEmail());
            userRepository.save(user);
        }

        return mapToResponse(studentRepository.save(student));
    }

    private Major getMajorOrThrow(Long majorId) {
        if (majorId == null) {
            throw new RuntimeException("majorId không được để trống");
        }
        return majorRepository.findById(majorId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ngành học!"));
    }

    public StudentResponse mapToResponse(Student student) {
        Homeroom homeroom = student.getHomeroom();
        Teacher advisor = homeroom != null ? homeroom.getAdvisor() : null;

        return StudentResponse.builder()
                .id(student.getId())
                .username(student.getUser() != null ? student.getUser().getUsername() : null)
                .email(student.getUser() != null ? student.getUser().getEmail() : null)
                .studentCode(student.getStudentCode())
                .fullName(student.getFullName())
                .dob(student.getDob())
                .gender(student.getGender())
                .phone(student.getPhone())
                .nationalId(student.getNationalId())
                .placeOfBirth(student.getPlaceOfBirth())
                .hometown(student.getHometown())
                .permanentAddress(student.getPermanentAddress())
                .currentAddress(student.getCurrentAddress())
                .emergencyContact(student.getEmergencyContact())
                .address(student.getAddress())
                .academicYear(student.getAcademicYear())
                .cohort(student.getCohort())
                // Homeroom info
                .homeroomId(homeroom != null ? homeroom.getId() : null)
                .className(homeroom != null ? homeroom.getClassName() : null)
                .advisorId(advisor != null ? advisor.getId() : null)
                .advisorName(advisor != null ? advisor.getFullName() : null)
                .advisorCode(advisor != null ? advisor.getTeacherCode() : null)
                .status(student.getStatus())
                .trainingType(student.getTrainingType())
                .majorId(student.getMajor() != null ? student.getMajor().getId() : null)
                .majorName(student.getMajor() != null ? student.getMajor().getName() : null)
                .majorCode(student.getMajor() != null ? student.getMajor().getMajorCode() : null)
                .gpa(resolveLatestGpa(student.getId()))
                .cpa(resolveCpa(student.getId()))
                .totalCredits(resolveTotalCredits(student.getId()))
                .build();
    }

    private Float resolveLatestGpa(Long studentId) {
        // Try pre-computed value first
        java.util.List<AcademicResult> results = academicResultRepository.findByStudentIdOrderBySemesterDesc(studentId);
        java.util.Optional<Float> precomputed = results.stream()
                .filter(r -> r.getSemester() != null && r.getSemesterGpa() != null)
                .findFirst()
                .map(AcademicResult::getSemesterGpa);
        if (precomputed.isPresent()) return precomputed.get();
        // Fall back to live computation from grades
        return academicResultService.computeLatestSemesterGpaLive(studentId);
    }

    private Float resolveCpa(Long studentId) {
        return academicResultRepository.findByStudentIdAndSemesterIdIsNull(studentId)
                .map(AcademicResult::getCumulativeGpa)
                .orElseGet(() -> academicResultService.computeCumulativeGpaLive(studentId));
    }

    private Integer resolveTotalCredits(Long studentId) {
        return academicResultRepository.findByStudentIdAndSemesterIdIsNull(studentId)
                .map(AcademicResult::getCumulativeCredits)
                .orElseGet(() -> academicResultService.computeCumulativeCreditsLive(studentId));
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfileByUsername(String username) {
        Student student = studentRepository.findByUser_Username(username)
                .orElseThrow(() -> new RuntimeException("Khong tim thay thong tin sinh vien cua tai khoan nay!"));
        User user = student.getUser();
        Integer rawYear = student.getAcademicYear();
        String academicYearStr = rawYear != null ? rawYear + " - " + (rawYear + 4) : null;
        Integer age = student.getDob() != null ? Period.between(student.getDob(), LocalDate.now()).getYears() : null;
        String fullName = student.getFullName() != null && !student.getFullName().isBlank()
                ? student.getFullName()
                : user.getUsername();
        String avatarName = URLEncoder.encode(fullName, StandardCharsets.UTF_8);

        Homeroom homeroom = student.getHomeroom();
        Teacher advisor = homeroom != null ? homeroom.getAdvisor() : null;

        return UserProfileResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .fullName(student.getFullName())
                .code(student.getStudentCode())
                .majorOrDegree(student.getMajor() != null ? student.getMajor().getName() : null)
                .avatarUrl("https://ui-avatars.com/api/?name=" + avatarName + "&background=b11226&color=fff")
                .gender(student.getGender())
                .dateOfBirth(student.getDob() != null ? student.getDob().toString() : null)
                .age(age)
                .nationalId(student.getNationalId())
                .placeOfBirth(student.getPlaceOfBirth())
                .hometown(student.getHometown())
                .permanentAddress(firstNonBlank(student.getPermanentAddress(), student.getAddress()))
                .currentAddress(firstNonBlank(student.getCurrentAddress(), student.getAddress()))
                .phone(student.getPhone())
                .emergencyContact(student.getEmergencyContact())
                .cohort(student.getCohort())
                .className(homeroom != null ? homeroom.getClassName() : null)
                .academicYear(academicYearStr)
                .advisor(advisor != null ? advisor.getFullName() : null)
                .status(student.getStatus())
                .trainingType(student.getTrainingType())
                .build();
    }

    private void applyPersonalFields(Student student, StudentRequest request) {
        student.setGender(request.getGender());
        student.setPhone(request.getPhone());
        student.setNationalId(request.getNationalId());
        student.setPlaceOfBirth(request.getPlaceOfBirth());
        student.setHometown(request.getHometown());
        student.setPermanentAddress(request.getPermanentAddress());
        student.setCurrentAddress(request.getCurrentAddress());
        student.setEmergencyContact(request.getEmergencyContact());
        student.setCohort(request.getCohort());
        student.setStatus(request.getStatus());
        student.setTrainingType(request.getTrainingType());
    }

    private String firstNonBlank(String primary, String fallback) {
        return primary != null && !primary.isBlank() ? primary : fallback;
    }

    // 3. XÓA (Xóa Enrollment -> Student -> User)
    @Transactional
    @CacheEvict(cacheNames = {"adminDashboard"}, allEntries = true)
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên!"));

        if (tuitionBillRepository.existsByStudentId(student.getId())) {
            throw new ConflictException("Không thể xóa sinh viên đã có hóa đơn học phí. Hãy xử lý các bản ghi học phí trước khi xóa.");
        }

        User user = student.getUser();
        enrollmentRepository.deleteByStudentId(student.getId());
        studentRepository.delete(student);
        userRepository.delete(user);
    }
}
