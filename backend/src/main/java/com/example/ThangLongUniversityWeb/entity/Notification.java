package com.example.ThangLongUniversityWeb.entity;

import com.example.ThangLongUniversityWeb.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notifications_recipient_read", columnList = "recipient_id, read_flag"),
        @Index(name = "idx_notifications_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private NotificationType type = NotificationType.SCHOOL;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    private String link;

    @Builder.Default
    @Column(name = "read_flag", nullable = false)
    private Boolean read = false;

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime readAt;
}
