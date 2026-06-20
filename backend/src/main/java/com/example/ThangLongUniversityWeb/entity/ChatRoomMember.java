package com.example.ThangLongUniversityWeb.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Thành viên của một ChatRoom
 */
@Entity
@Table(name = "chat_room_members", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"chat_room_id", "user_id"})
}, indexes = {
    @Index(name = "idx_chat_room_id", columnList = "chat_room_id"),
    @Index(name = "idx_user_id", columnList = "user_id")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ChatRoomMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Thời điểm gia nhập phòng chat
     */
    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime joinedAt = LocalDateTime.now();

    /**
     * Thời gian lần cuối cùng người này xem chat room
     */
    private LocalDateTime lastReadAt;

    /**
     * Số lượng tin nhắn chưa đọc
     */
    @Builder.Default
    private Integer unreadCount = 0;

    /**
     * Cờ xác định liệu người này còn là thành viên hay đã rời
     */
    @Builder.Default
    private Boolean isActive = true;
}
