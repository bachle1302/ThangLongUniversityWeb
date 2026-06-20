package com.example.ThangLongUniversityWeb.entity;

import com.example.ThangLongUniversityWeb.enums.ConversationType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * ChatRoom entity - đại diện cho một phòng chat
 * Có thể là chat riêng tư (1-1), nhóm (N-N) hoặc lớp học
 */
@Entity
@Table(name = "chat_rooms", indexes = {
    @Index(name = "idx_chat_room_type", columnList = "type"),
    @Index(name = "idx_chat_room_created_at", columnList = "created_at")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tên của chat room (để trống nếu là chat 1-1, sẽ tự generate)
     */
    private String name;

    /**
     * Mô tả chi tiết về phòng chat
     */
    private String description;

    /**
     * Loại cuộc hội thoại: PRIVATE (1-1), GROUP (nhóm), CLASS_GROUP (lớp học)
     */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ConversationType type = ConversationType.PRIVATE;

    /**
     * URL ảnh đại diện của chat room
     */
    private String avatarUrl;

    /**
     * Người tạo phòng chat
     */
    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;

    /**
     * Tin nhắn gần nhất được gửi (để hiển thị preview)
     */
    @OneToOne
    @JoinColumn(name = "last_message_id")
    private Message lastMessage;

    /**
     * Số lượng thành viên hiện tại
     */
    @Builder.Default
    private Integer memberCount = 0;

    /**
     * Cờ xác định phòng chat có bị xóa mềm không
     */
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Thời gian tạo phòng
     */
    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Thời gian cập nhật lần cuối
     */
    private LocalDateTime updatedAt;

    /**
     * Danh sách thành viên tham gia phòng chat
     */
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ChatRoomMember> members = new HashSet<>();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
