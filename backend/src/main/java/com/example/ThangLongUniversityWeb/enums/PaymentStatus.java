package com.example.ThangLongUniversityWeb.enums;

public enum PaymentStatus {
    PENDING,    // Đã tạo URL, chờ thanh toán
    SUCCESS,    // Thanh toán thành công
    FAILED,     // Giao dịch thất bại / bị từ chối
    CANCELLED,  // Người dùng hủy thanh toán
    INVALID     // Chữ ký không hợp lệ (nghi ngờ giả mạo)
}
