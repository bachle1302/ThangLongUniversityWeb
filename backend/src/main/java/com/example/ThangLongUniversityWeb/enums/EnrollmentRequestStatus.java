package com.example.ThangLongUniversityWeb.enums;

/**
 * Trạng thái xử lý yêu cầu đăng ký học phần.
 * Dùng cho cả luồng Kafka và Direct, được push realtime qua WebSocket.
 */
public enum EnrollmentRequestStatus {
    PENDING,    // Đã tiếp nhận, đang chờ xử lý
    PROCESSING, // Đang kiểm tra và ghi nhận
    SUCCESS,    // Đăng ký thành công
    FAILED,     // Đăng ký thất bại (slot đầy, lỗi nghiệp vụ...)
    UNKNOWN     // Không tìm thấy requestId (hết TTL Redis)
}
