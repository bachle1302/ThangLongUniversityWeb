package com.example.ThangLongUniversityWeb.service.impl;

import com.example.ThangLongUniversityWeb.dto.response.NotificationResponse;
import com.example.ThangLongUniversityWeb.entity.ChatRoom;
import com.example.ThangLongUniversityWeb.entity.ChatRoomMember;
import com.example.ThangLongUniversityWeb.entity.Notification;
import com.example.ThangLongUniversityWeb.entity.User;
import com.example.ThangLongUniversityWeb.enums.NotificationType;
import com.example.ThangLongUniversityWeb.repository.ChatRoomMemberRepository;
import com.example.ThangLongUniversityWeb.repository.NotificationRepository;
import com.example.ThangLongUniversityWeb.repository.UserRepository;
import com.example.ThangLongUniversityWeb.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private static final String CHAT_PREFIX = "chat-";
    private static final String SCHOOL_PREFIX = "school-";

    private final NotificationRepository notificationRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications() {
        User currentUser = getCurrentUser();

        List<NotificationResponse> schoolNotifications = notificationRepository
                .findByRecipientOrderByCreatedAtDesc(currentUser)
                .stream()
                .map(this::mapSchoolNotification)
                .toList();

        List<NotificationResponse> chatNotifications = chatRoomMemberRepository
                .findByUserAndIsActive(currentUser, true)
                .stream()
                .filter(member -> member.getUnreadCount() != null && member.getUnreadCount() > 0)
                .map(this::mapChatNotification)
                .toList();

        return java.util.stream.Stream.concat(schoolNotifications.stream(), chatNotifications.stream())
                .sorted(Comparator.comparing(NotificationResponse::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Override
    @Transactional
    public void markAsRead(String notificationId) {
        User currentUser = getCurrentUser();
        if (notificationId.startsWith(CHAT_PREFIX)) {
            Long roomId = parseId(notificationId, CHAT_PREFIX);
            chatRoomMemberRepository.findByUserAndIsActive(currentUser, true).stream()
                    .filter(member -> member.getChatRoom().getId().equals(roomId))
                    .findFirst()
                    .ifPresent(this::markChatMemberRead);
            return;
        }

        Long id = parseId(notificationId, SCHOOL_PREFIX);
        Notification notification = notificationRepository.findByIdAndRecipient(id, currentUser)
                .orElseThrow(() -> new RuntimeException("Thong bao khong ton tai"));
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        User currentUser = getCurrentUser();
        LocalDateTime now = LocalDateTime.now();

        List<Notification> notifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(currentUser);
        notifications.forEach(notification -> {
            notification.setRead(true);
            notification.setReadAt(now);
        });
        notificationRepository.saveAll(notifications);

        List<ChatRoomMember> members = chatRoomMemberRepository.findByUserAndIsActive(currentUser, true);
        members.forEach(this::markChatMemberRead);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Khong tim thay user: " + username));
    }

    private NotificationResponse mapSchoolNotification(Notification notification) {
        return NotificationResponse.builder()
                .id(SCHOOL_PREFIX + notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .body(notification.getBody())
                .link(notification.getLink())
                .read(Boolean.TRUE.equals(notification.getRead()))
                .createdAt(notification.getCreatedAt())
                .build();
    }

    private NotificationResponse mapChatNotification(ChatRoomMember member) {
        ChatRoom room = member.getChatRoom();
        String roomName = room.getName() == null || room.getName().isBlank() ? "Phong chat" : room.getName();
        String preview = room.getLastMessage() == null ? "" : room.getLastMessage().getContent();
        String sender = room.getLastMessage() == null ? "Tin nhan moi" : displayName(room.getLastMessage().getSender());
        int unreadCount = member.getUnreadCount() == null ? 0 : member.getUnreadCount();

        return NotificationResponse.builder()
                .id(CHAT_PREFIX + room.getId())
                .type(NotificationType.CHAT)
                .title(unreadCount + " tin nhan moi trong " + roomName)
                .body(sender + (preview == null || preview.isBlank() ? "" : ": " + preview))
                .link("/student/chat")
                .read(false)
                .createdAt(room.getLastMessage() == null ? room.getUpdatedAt() : room.getLastMessage().getCreatedAt())
                .build();
    }

    private void markChatMemberRead(ChatRoomMember member) {
        member.setUnreadCount(0);
        member.setLastReadAt(LocalDateTime.now());
        chatRoomMemberRepository.save(member);
    }

    private Long parseId(String notificationId, String prefix) {
        if (!notificationId.startsWith(prefix)) {
            throw new RuntimeException("Ma thong bao khong hop le");
        }
        try {
            return Long.parseLong(notificationId.substring(prefix.length()));
        } catch (NumberFormatException ex) {
            throw new RuntimeException("Ma thong bao khong hop le");
        }
    }

    private String displayName(User user) {
        if (user.getStudent() != null && user.getStudent().getFullName() != null) return user.getStudent().getFullName();
        if (user.getTeacher() != null && user.getTeacher().getFullName() != null) return user.getTeacher().getFullName();
        return user.getUsername();
    }
}
