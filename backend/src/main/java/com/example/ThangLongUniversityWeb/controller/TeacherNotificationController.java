package com.example.ThangLongUniversityWeb.controller;

import com.example.ThangLongUniversityWeb.dto.response.NotificationResponse;
import com.example.ThangLongUniversityWeb.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/notifications")
@RequiredArgsConstructor
@Tag(name = "Teacher - notifications")
@SecurityRequirement(name = "bearerAuth")
public class TeacherNotificationController {
    private final NotificationService notificationService;

    @Operation(summary = "Lay danh sach thong bao cua giang vien")
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications() {
        return ResponseEntity.ok(notificationService.getMyNotifications());
    }

    @Operation(summary = "Danh dau mot thong bao da doc")
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable String notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Danh dau tat ca thong bao da doc")
    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }
}
