package com.example.ThangLongUniversityWeb.repository;

import com.example.ThangLongUniversityWeb.entity.ChatRoom;
import com.example.ThangLongUniversityWeb.entity.ChatRoomMember;
import com.example.ThangLongUniversityWeb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository để quản lý thành viên của ChatRoom
 */
@Repository
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
    
    /**
     * Tìm thành viên trong phòng chat
     */
    Optional<ChatRoomMember> findByChatRoomAndUser(ChatRoom chatRoom, User user);

    /**
     * Lấy danh sách tất cả thành viên của một phòng (chỉ những thành viên active)
     */
    List<ChatRoomMember> findByChatRoomAndIsActive(ChatRoom chatRoom, Boolean isActive);

    List<ChatRoomMember> findByUserAndIsActive(User user, Boolean isActive);

    /**
     * Lấy số lượng thành viên active trong phòng
     */
    @Query("SELECT COUNT(m) FROM ChatRoomMember m WHERE m.chatRoom = :chatRoom AND m.isActive = true")
    Integer countActiveMembers(@Param("chatRoom") ChatRoom chatRoom);

    /**
     * Kiểm tra xem người dùng có phải là thành viên của phòng không
     */
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM ChatRoomMember m " +
           "WHERE m.chatRoom = :chatRoom AND m.user = :user AND m.isActive = true")
    Boolean isMemberOfChatRoom(@Param("chatRoom") ChatRoom chatRoom, @Param("user") User user);

    /**
     * Tính tổng tin nhắn chưa đọc của một người dùng
     */
    @Query("SELECT SUM(m.unreadCount) FROM ChatRoomMember m WHERE m.user = :user AND m.isActive = true")
    Integer getTotalUnreadCount(@Param("user") User user);
}
