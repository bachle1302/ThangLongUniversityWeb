package com.example.ThangLongUniversityWeb.repository;

import com.example.ThangLongUniversityWeb.entity.ChatRoom;
import com.example.ThangLongUniversityWeb.entity.Message;
import com.example.ThangLongUniversityWeb.enums.MessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository để quản lý tin nhắn (Message)
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    /**
     * Lấy lịch sử tin nhắn của một phòng chat (phân trang)
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :chatRoomId ORDER BY m.createdAt DESC")
    Page<Message> findByChatRoomId(@Param("chatRoomId") Long chatRoomId, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.conversation.id = :chatRoomId AND m.type = 'FILE' ORDER BY m.createdAt DESC")
    Page<Message> findFilesByChatRoomId(@Param("chatRoomId") Long chatRoomId, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.conversation.id = :chatRoomId AND m.type = 'TEXT' AND LOWER(m.content) LIKE '%http%' ORDER BY m.createdAt DESC")
    Page<Message> findLinksByChatRoomId(@Param("chatRoomId") Long chatRoomId, Pageable pageable);

    /**
     * Lấy tin nhắn mới hơn một thời gian nào đó
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :chatRoomId AND m.createdAt > :since ORDER BY m.createdAt ASC")
    List<Message> findMessagesAfter(@Param("chatRoomId") Long chatRoomId, @Param("since") LocalDateTime since);

    /**
     * Lấy tin nhắn chưa được giao (SENT)
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :chatRoomId AND m.status = :status ORDER BY m.createdAt ASC")
    List<Message> findMessagesByStatus(@Param("chatRoomId") Long chatRoomId, @Param("status") MessageStatus status);

    /**
     * Cập nhật tất cả tin nhắn trong phòng thành READ
     */
    @Query("UPDATE Message m SET m.status = 'READ' WHERE m.conversation.id = :chatRoomId AND m.status != 'READ'")
    void markAllMessagesAsRead(@Param("chatRoomId") Long chatRoomId);

    /**
     * Lấy tin nhắn gần nhất trong phòng chat
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :chatRoomId ORDER BY m.createdAt DESC LIMIT 1")
    Message findLastMessage(@Param("chatRoomId") Long chatRoomId);

    /**
     * Đếm tin nhắn chưa đọc trong phòng chat
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id = :chatRoomId AND m.status = 'SENT'")
    Long countUnreadMessages(@Param("chatRoomId") Long chatRoomId);
}
