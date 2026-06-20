package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.dto.response.NotificationResponse;

import java.util.List;

public interface NotificationService {
    List<NotificationResponse> getMyNotifications();

    void markAsRead(String notificationId);

    void markAllAsRead();
}
