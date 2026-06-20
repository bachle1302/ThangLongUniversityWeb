package com.example.ThangLongUniversityWeb.repository;

import com.example.ThangLongUniversityWeb.entity.ChatRoom;
import com.example.ThangLongUniversityWeb.entity.User;
import com.example.ThangLongUniversityWeb.enums.ConversationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository để quản lý ChatRoom
 */
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    
    /**
     * Tìm phòng chat 1-1 giữa hai người dùng
     */
    @Query("SELECT cr FROM ChatRoom cr " +
           "WHERE cr.type = 'PRIVATE' " +
           "AND EXISTS (SELECT 1 FROM ChatRoomMember m1 WHERE m1.chatRoom = cr AND m1.user = :user1) " +
           "AND EXISTS (SELECT 1 FROM ChatRoomMember m2 WHERE m2.chatRoom = cr AND m2.user = :user2)")
    Optional<ChatRoom> findPrivateChatRoom(@Param("user1") User user1, @Param("user2") User user2);

    /**
     * Lấy danh sách tất cả phòng chat của một người dùng
     */
    @Query("SELECT cr FROM ChatRoom cr " +
           "JOIN ChatRoomMember m ON m.chatRoom = cr " +
           "WHERE m.user = :user AND m.isActive = true AND cr.isActive = true " +
           "ORDER BY cr.updatedAt DESC")
    Page<ChatRoom> findAllChatRoomsByUser(@Param("user") User user, Pageable pageable);

    /**
     * Tìm phòng chat theo tên (cho group chat)
     */
    List<ChatRoom> findByNameContainingIgnoreCase(String name);

    /**
     * Lấy phòng chat theo loại
     */
    List<ChatRoom> findByType(ConversationType type);

    /**
     * Tìm phòng chat theo người tạo
     */
    List<ChatRoom> findByCreator(User creator);
}
