package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.dto.response.EnrollmentRequestStatusResponse;
import com.example.ThangLongUniversityWeb.enums.EnrollmentRequestStatus;

/**
 * Contract để lưu trạng thái xử lý yêu cầu đăng ký học phần.
 * Implementation hiện tại: Redis (TTL 1 ngày).
 * Nếu Redis down, toàn bộ flow sẽ fail — cần có fallback in-memory nếu cần.
 */
public interface EnrollmentRequestStatusService {

    void markPending(String requestId, String message);

    void markProcessing(String requestId, String message);

    void markSuccess(String requestId, String message);

    void markFailed(String requestId, String message);

    /**
     * Lấy trạng thái hiện tại của yêu cầu đăng ký.
     */
    EnrollmentRequestStatusResponse getStatus(String requestId);

    /**
     * Idempotency check cho Kafka consumer (SETNX).
     * @return true nếu đây là lần đầu tiên consumer xử lý requestId này.
     */
    boolean markIfFirstTimeProcessing(String requestId);

    /**
     * Lấy trạng thái dưới dạng enum (tiện dùng nội bộ).
     */
    EnrollmentRequestStatus getStatusEnum(String requestId);
}
