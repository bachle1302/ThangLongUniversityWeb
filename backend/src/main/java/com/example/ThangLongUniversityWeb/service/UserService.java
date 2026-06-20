package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.dto.request.AdminUserUpdateRequest;
import com.example.ThangLongUniversityWeb.dto.response.AdminUserManagementResponse;
import com.example.ThangLongUniversityWeb.enums.Role;
import com.example.ThangLongUniversityWeb.entity.User;
import com.example.ThangLongUniversityWeb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTokenService redisTokenService;

    @Transactional(readOnly = true)
    public List<AdminUserManagementResponse> listUsersForAdmin() {
        return userRepository.findAllWithProfiles().stream()
                .map(this::mapToAdminUserResponse)
                .toList();
    }

    // 1. CHỈ DÙNG ĐỂ TẠO ADMIN MỚI (Student/Teacher đã có Service riêng tạo rồi)
    @Transactional
    public User createAdmin(String username, String password, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole(Role.ADMIN); // Ép cứng luôn là Role ADMIN
        user.setActive(true);

        return userRepository.save(user);
    }

    // 2. KHÓA / MỞ KHÓA TÀI KHOẢN
    @Transactional
    public User toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User!"));

        // Đảo ngược trạng thái
        boolean newStatus = !user.isActive();
        user.setActive(newStatus);

        // SỬA ĐOẠN NÀY: Nếu tài khoản bị KHÓA (newStatus == false), lập tức xóa Refresh Token trong Redis
        if (!newStatus) {
            redisTokenService.revokeAllForUser(user.getUsername());
        }

        return userRepository.save(user);
    }

    @Transactional
    public AdminUserManagementResponse updateUser(Long userId, AdminUserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay User!"));

        if (userRepository.existsByUsernameAndIdNot(request.getUsername(), userId)) {
            throw new RuntimeException("Ten dang nhap da ton tai!");
        }
        if (userRepository.existsByEmailAndIdNot(request.getEmail(), userId)) {
            throw new RuntimeException("Email da ton tai!");
        }

        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim());
        applyFullName(user, request.getFullName().trim());

        return mapToAdminUserResponse(userRepository.save(user));
    }

    // 3. XÓA TÀI KHOẢN (Chỉ xóa Admin, nếu xóa SV/GV thì phải xóa từ StudentService)
    @Transactional
    public void deleteAdminUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User!"));

        if (user.getRole() != Role.ADMIN) {
            throw new RuntimeException("API này chỉ dùng để xóa Admin. Muốn xóa SV/GV hãy dùng API tương ứng!");
        }

        userRepository.delete(user);
    }

    private AdminUserManagementResponse mapToAdminUserResponse(User user) {
        return AdminUserManagementResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .fullName(resolveFullName(user))
                .profileId(resolveProfileId(user))
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }

    private String resolveFullName(User user) {
        if (user.getRole() == Role.STUDENT && user.getStudent() != null && user.getStudent().getFullName() != null && !user.getStudent().getFullName().isBlank()) {
            return user.getStudent().getFullName();
        }
        if (user.getRole() == Role.TEACHER && user.getTeacher() != null && user.getTeacher().getFullName() != null && !user.getTeacher().getFullName().isBlank()) {
            return user.getTeacher().getFullName();
        }
        if (user.getRole() == Role.ADMIN) {
            return "Quan tri he thong";
        }
        return user.getUsername();
    }

    private Long resolveProfileId(User user) {
        if (user.getRole() == Role.STUDENT && user.getStudent() != null) {
            return user.getStudent().getId();
        }
        if (user.getRole() == Role.TEACHER && user.getTeacher() != null) {
            return user.getTeacher().getId();
        }
        return null;
    }

    private void applyFullName(User user, String fullName) {
        if (user.getRole() == Role.STUDENT && user.getStudent() != null) {
            user.getStudent().setFullName(fullName);
            return;
        }
        if (user.getRole() == Role.TEACHER && user.getTeacher() != null) {
            user.getTeacher().setFullName(fullName);
        }
    }

    @Transactional
    public void resetUserPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User!"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Revoke all refresh tokens
        redisTokenService.revokeAllForUser(user.getUsername());
    }
}