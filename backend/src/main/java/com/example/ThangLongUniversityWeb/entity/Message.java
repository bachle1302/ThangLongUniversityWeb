package com.example.ThangLongUniversityWeb.entity;

import com.example.ThangLongUniversityWeb.enums.MessageType;
import com.example.ThangLongUniversityWeb.enums.MessageStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages", indexes = {
    @Index(name = "idx_conversation_id", columnList = "conversation_id"),
    @Index(name = "idx_sender_id", columnList = "sender_id"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MessageType type = MessageType.TEXT;

    /**
     * Trạng thái tin nhắn: SENT (đã gửi), DELIVERED (đã giao), READ (đã xem)
     */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MessageStatus status = MessageStatus.SENT;

    /**
     * File URL hoặc Media URL nếu loại tin nhắn là IMAGE hoặc FILE
     */
    private String mediaUrl;

    private String fileName;

    private Long fileSize;

    /**
     * Thời gian lần cuối cập nhật (khi status thay đổi)
     */
    private LocalDateTime updatedAt;

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
