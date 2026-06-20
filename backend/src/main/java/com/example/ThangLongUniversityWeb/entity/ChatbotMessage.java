package com.example.ThangLongUniversityWeb.entity;

import com.example.ThangLongUniversityWeb.enums.ChatbotRole;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chatbot_messages", indexes = {
    @Index(name = "idx_chatbot_user_session", columnList = "user_id, session_id")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ChatbotMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatbotRole role;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
