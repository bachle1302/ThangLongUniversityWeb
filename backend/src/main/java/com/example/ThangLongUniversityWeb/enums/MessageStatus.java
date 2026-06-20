package com.example.ThangLongUniversityWeb.enums;

/**
 * Trạng thái tin nhắn trong hệ thống Chat
 */
public enum MessageStatus {
    /**
     * Tin nhắn vừa được gửi lên server, chưa lưu vào database
     */
    SENT,

    /**
     * Tin nhắn đã được lưu vào database, gửi đi được
     */
    DELIVERED,

    /**
     * Tin nhắn đã được người nhận xem
     */
    READ
}
