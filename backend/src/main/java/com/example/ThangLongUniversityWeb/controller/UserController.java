package com.example.ThangLongUniversityWeb.controller;

import com.example.ThangLongUniversityWeb.dto.response.UserProfileResponse;
import com.example.ThangLongUniversityWeb.enums.Role;
import com.example.ThangLongUniversityWeb.entity.Student;
import com.example.ThangLongUniversityWeb.entity.Teacher;
import com.example.ThangLongUniversityWeb.entity.User;
import com.example.ThangLongUniversityWeb.repository.StudentRepository;
import com.example.ThangLongUniversityWeb.repository.TeacherRepository;
import com.example.ThangLongUniversityWeb.repository.UserRepository;
import com.example.ThangLongUniversityWeb.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Mạng xã hội nội bộ", description = "Các API xem hồ sơ cá nhân và người khác")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final StudentService studentService;

    @Operation(summary = "Xem hồ sơ của chính mình")
    @GetMapping("/me")
    @Transactional(readOnly = true)
    public ResponseEntity<UserProfileResponse> getMyProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        return ResponseEntity.ok(buildUserProfile(currentUsername));
    }

    @Operation(summary = "Xem hồ sơ của người khác (Tìm theo username, mã SV hoặc mã GV)")
    @GetMapping("/{identifier}")
    @Transactional(readOnly = true)
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable String identifier) {
        // identifier lúc này có thể là "sv001" (username) hoặc "SV001" (studentCode)
        return ResponseEntity.ok(buildUserProfile(identifier));
    }

    // --- HÀM XỬ LÝ TÌM KIẾM ĐA NĂNG ---
    private UserProfileResponse buildUserProfile(String identifier) {
        User user = null;
        Student studentProfile = null;
        Teacher teacherProfile = null;

        // Ưu tiên 1: Thử tìm xem identifier có phải là Username không
        var userOpt = userRepository.findByUsername(identifier);
        if (userOpt.isPresent()) {
            user = userOpt.get();
            // Lấy profile tương ứng nếu có
            if (user.getRole() == Role.STUDENT) {
                studentProfile = studentRepository.findByUser_Username(user.getUsername()).orElse(null);
            } else if (user.getRole() == Role.TEACHER) {
                teacherProfile = teacherRepository.findByUser_Username(user.getUsername()).orElse(null);
            }
        }
        // Ưu tiên 2: Nếu không phải Username, thử tìm xem có phải Mã Sinh Viên không
        else {
            var studentOpt = studentRepository.findByStudentCode(identifier);
            if (studentOpt.isPresent()) {
                studentProfile = studentOpt.get();
                user = studentProfile.getUser(); // Kéo ngược User ra từ Student
            }
            // Ưu tiên 3: Thử tìm xem có phải Mã Giảng Viên không
            else {
                var teacherOpt = teacherRepository.findByTeacherCode(identifier);
                if (teacherOpt.isPresent()) {
                    teacherProfile = teacherOpt.get();
                    user = teacherProfile.getUser(); // Kéo ngược User ra từ Teacher
                }
            }
        }

        // Chốt hạ: Nếu quét cả 3 bảng vẫn không thấy ai
        if (user == null) {
            throw new UsernameNotFoundException("Không tìm thấy người dùng với thông tin: " + identifier);
        }

        if (user.getRole() == Role.STUDENT && studentProfile != null) {
            return studentService.getProfileByUsername(user.getUsername());
        }

        // --- Đóng gói dữ liệu trả về ---
        UserProfileResponse.UserProfileResponseBuilder responseBuilder = UserProfileResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .avatarUrl("https://ui-avatars.com/api/?name=" + user.getUsername() + "&background=random");

        if (user.getRole() == Role.STUDENT && studentProfile != null) {
            Integer rawYear = studentProfile.getAcademicYear();
            String academicYearStr = rawYear != null ? rawYear + " - " + (rawYear + 4) : null;
            Integer age = studentProfile.getDob() != null
                    ? java.time.Period.between(studentProfile.getDob(), java.time.LocalDate.now()).getYears()
                    : null;

            responseBuilder.fullName(studentProfile.getFullName())
                    .code(studentProfile.getStudentCode())
                    .majorOrDegree(studentProfile.getMajor() != null ? studentProfile.getMajor().getName() : null)
                    .gender(studentProfile.getGender())
                    .dateOfBirth(studentProfile.getDob() != null ? studentProfile.getDob().toString() : null)
                    .age(age)
                    .nationalId(studentProfile.getNationalId())
                    .placeOfBirth(studentProfile.getPlaceOfBirth())
                    .hometown(studentProfile.getHometown())
                    .permanentAddress(studentProfile.getPermanentAddress())
                    .currentAddress(studentProfile.getCurrentAddress())
                    .phone(studentProfile.getPhone())
                    .emergencyContact(studentProfile.getEmergencyContact())
                    .cohort(studentProfile.getCohort())
                    .className(studentProfile.getHomeroom() != null ? studentProfile.getHomeroom().getClassName() : null)
                    .academicYear(academicYearStr)
                    .advisor(studentProfile.getHomeroom() != null && studentProfile.getHomeroom().getAdvisor() != null ? studentProfile.getHomeroom().getAdvisor().getFullName() : null)
                    .status(studentProfile.getStatus())
                    .trainingType(studentProfile.getTrainingType());
        } else if (user.getRole() == Role.TEACHER && teacherProfile != null) {
            Integer age = teacherProfile.getDob() != null
                    ? java.time.Period.between(teacherProfile.getDob(), java.time.LocalDate.now()).getYears()
                    : null;

            responseBuilder.fullName(teacherProfile.getFullName())
                    .code(teacherProfile.getTeacherCode())
                    .majorOrDegree(teacherProfile.getDegree())
                    .gender(teacherProfile.getGender())
                    .dateOfBirth(teacherProfile.getDob() != null ? teacherProfile.getDob().toString() : null)
                    .age(age)
                    .nationalId(teacherProfile.getNationalId())
                    .placeOfBirth(teacherProfile.getPlaceOfBirth())
                    .hometown(teacherProfile.getHometown())
                    .permanentAddress(teacherProfile.getPermanentAddress())
                    .currentAddress(teacherProfile.getCurrentAddress())
                    .phone(teacherProfile.getPhone())
                    .emergencyContact(teacherProfile.getEmergencyContact())
                    .department(teacherProfile.getDepartment() != null ? teacherProfile.getDepartment().getName() : null);
        } else if (user.getRole() == Role.ADMIN) {
            responseBuilder.fullName("Quản trị viên hệ thống").code("ADMIN");
        }

        return responseBuilder.build();
    }
}
