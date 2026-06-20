package com.example.ThangLongUniversityWeb.service.impl;

import com.example.ThangLongUniversityWeb.dto.response.EnrollmentRequestStatusResponse;
import com.example.ThangLongUniversityWeb.enums.EnrollmentRequestStatus;
import com.example.ThangLongUniversityWeb.service.EnrollmentRequestStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis-backed implementation cho EnrollmentRequestStatusService.
 * TTL: 1 ngày — đủ cho luồng đăng ký học phần.
 */
@Service
@RequiredArgsConstructor
public class RedisEnrollmentRequestStatusServiceImpl implements EnrollmentRequestStatusService {

    private static final String KEY_PREFIX = "enrollment:req:";
    private static final Duration TTL = Duration.ofDays(1);

    private final StringRedisTemplate redisTemplate;

    @Override
    public void markPending(String requestId, String message) {
        set(requestId, EnrollmentRequestStatus.PENDING, message);
    }

    @Override
    public void markProcessing(String requestId, String message) {
        set(requestId, EnrollmentRequestStatus.PROCESSING, message);
    }

    @Override
    public void markSuccess(String requestId, String message) {
        set(requestId, EnrollmentRequestStatus.SUCCESS, message);
    }

    @Override
    public void markFailed(String requestId, String message) {
        set(requestId, EnrollmentRequestStatus.FAILED, message);
    }

    @Override
    public EnrollmentRequestStatusResponse getStatus(String requestId) {
        String value = redisTemplate.opsForValue().get(KEY_PREFIX + requestId);
        if (value == null) {
            return new EnrollmentRequestStatusResponse(requestId,
                    EnrollmentRequestStatus.UNKNOWN.name(),
                    "Không tìm thấy requestId hoặc đã hết hạn lưu trạng thái.");
        }
        String[] parts = value.split("\\|", 2);
        String status  = parts.length > 0 ? parts[0] : EnrollmentRequestStatus.UNKNOWN.name();
        String msg     = parts.length > 1 ? parts[1] : "";
        return new EnrollmentRequestStatusResponse(requestId, status, msg);
    }

    @Override
    public EnrollmentRequestStatus getStatusEnum(String requestId) {
        String value = redisTemplate.opsForValue().get(KEY_PREFIX + requestId);
        if (value == null) return EnrollmentRequestStatus.UNKNOWN;
        try {
            return EnrollmentRequestStatus.valueOf(value.split("\\|", 2)[0]);
        } catch (IllegalArgumentException e) {
            return EnrollmentRequestStatus.UNKNOWN;
        }
    }

    @Override
    public boolean markIfFirstTimeProcessing(String requestId) {
        Boolean ok = redisTemplate.opsForValue()
                .setIfAbsent(KEY_PREFIX + requestId + ":processed", "1", TTL);
        return Boolean.TRUE.equals(ok);
    }

    // ─── internal ────────────────────────────────────────────
    private void set(String requestId, EnrollmentRequestStatus status, String message) {
        String value = status.name() + "|" + (message == null ? "" : message);
        redisTemplate.opsForValue().set(KEY_PREFIX + requestId, value, TTL);
    }
}
