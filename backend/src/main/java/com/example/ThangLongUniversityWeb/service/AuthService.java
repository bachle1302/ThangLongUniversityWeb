package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.dto.request.LoginRequest;
import com.example.ThangLongUniversityWeb.dto.request.ChangePasswordRequest;
import com.example.ThangLongUniversityWeb.dto.response.AuthResponse;
import com.example.ThangLongUniversityWeb.entity.User;
import com.example.ThangLongUniversityWeb.exception.UnauthorizedException;
import com.example.ThangLongUniversityWeb.repository.UserRepository;
import com.example.ThangLongUniversityWeb.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final RedisTokenService redisTokenService;
    private final PasswordEncoder passwordEncoder;

    // ... (Giữ nguyên các hàm bên dưới của bạn) ...
    public AuthResponse login(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (AuthenticationException ex) {
            throw new UnauthorizedException("Invalid username or password");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + request.getUsername()));

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = jwtUtils.generateAccessToken(user.getUsername(), user.getRole().name());
        String refreshJti = UUID.randomUUID().toString();
        String refreshToken = jwtUtils.generateRefreshToken(user.getUsername(), refreshJti);

        redisTokenService.saveCurrentRefreshToken(user.getUsername(), refreshJti);

        return new AuthResponse(
                accessToken,
                refreshToken,
                user.getRole().name()
        );
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RuntimeException("Refresh token is required");
        }

        String username = jwtUtils.extractUsername(refreshToken);
        if (username == null || username.isBlank()) {
            throw new RuntimeException("Refresh token is invalid");
        }

        String jti = jwtUtils.extractJti(refreshToken);
        if (jti == null || jti.isBlank()) {
            throw new RuntimeException("Refresh token is invalid");
        }

        if (!jwtUtils.isTokenValid(refreshToken, username)) {
            throw new RuntimeException("Refresh token is expired or invalid");
        }

        String currentJti = redisTokenService.getCurrentRefreshJti(username);
        // No rotation mode:
        // - Refresh token phải là token current của user và còn active trong Redis
        if (currentJti == null || !currentJti.equals(jti) || !redisTokenService.isRefreshJtiActive(jti)) {
            redisTokenService.revokeAllForUser(username);
            throw new RuntimeException("Refresh token is not valid");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        String accessToken = jwtUtils.generateAccessToken(user.getUsername(), user.getRole().name());

        return new AuthResponse(
                accessToken,
                // Keep the same refresh token (no rotation)
                refreshToken,
                user.getRole().name()
        );
    }
    public void logout(String refreshToken) {
        // Idempotent logout:
        // - Client có thể gửi refresh token, access token, token hết hạn, hoặc token rác
        // - Backend sẽ cố gắng revoke refresh token nếu parse được (username + jti)
        // - Nếu không parse/không hợp lệ => coi như đã logout, không ném lỗi
        if (refreshToken == null || refreshToken.isBlank()) return;
        try {
            String username = jwtUtils.extractUsername(refreshToken);
            String jti = jwtUtils.extractJti(refreshToken);
            if (username == null || username.isBlank() || jti == null || jti.isBlank()) {
                return;
            }
            redisTokenService.revokeRefreshToken(username, jti);
        } catch (Exception ignored) {
            // intentionally idempotent
        }
    }

    @org.springframework.transaction.annotation.Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Mật khẩu hiện tại không chính xác");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Mật khẩu mới không được trùng với mật khẩu cũ");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Xác nhận mật khẩu mới không khớp");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Revoke all refresh tokens to force user to login again
        redisTokenService.revokeAllForUser(username);
    }
}
