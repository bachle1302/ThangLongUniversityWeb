package com.example.ThangLongUniversityWeb.service.impl;

import com.example.ThangLongUniversityWeb.dto.request.ChatMessageRequest;
import com.example.ThangLongUniversityWeb.dto.request.MarkMessageAsReadRequest;
import com.example.ThangLongUniversityWeb.dto.response.ChatMessageResponse;
import com.example.ThangLongUniversityWeb.entity.ChatRoom;
import com.example.ThangLongUniversityWeb.entity.Message;
import com.example.ThangLongUniversityWeb.entity.User;
import com.example.ThangLongUniversityWeb.entity.Conversation;
import com.example.ThangLongUniversityWeb.entity.ChatRoomMember;
import com.example.ThangLongUniversityWeb.enums.MessageStatus;
import com.example.ThangLongUniversityWeb.repository.ChatRoomRepository;
import com.example.ThangLongUniversityWeb.repository.ChatRoomMemberRepository;
import com.example.ThangLongUniversityWeb.repository.MessageRepository;
import com.example.ThangLongUniversityWeb.repository.UserRepository;
import com.example.ThangLongUniversityWeb.repository.ConversationRepository;
import com.example.ThangLongUniversityWeb.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementation của ChatMessageService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageServiceImpl implements ChatMessageService {

    /**
     * Serialize conversation bootstrap per chat room id inside this JVM (fixes parallel WS sends).
     */
    private static final ConcurrentHashMap<Long, Object> ROOM_LOCKS = new ConcurrentHashMap<>();

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;

    /**
     * Lấy user hiện tại từ JWT token
     */
    private User getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new RuntimeException("Chưa có Authentication trong SecurityContext (WebSocket message cần set auth)");
        }
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user: " + username));
    }

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(ChatMessageRequest request) {
        User sender = getCurrentUser();

        // Lấy ChatRoom
        ChatRoom chatRoom = chatRoomRepository.findById(request.getChatRoomId())
                .orElseThrow(() -> new RuntimeException("Phòng chat không tồn tại"));

        // Kiểm tra xem sender có phải là thành viên không
        Boolean isMember = chatRoomMemberRepository.isMemberOfChatRoom(chatRoom, sender);
        if (!isMember) {
            throw new RuntimeException("Bạn không phải là thành viên của phòng chat này");
        }

        // Conversation row must exist with PK == chat_room.id (DB FK). Concurrent first messages could race; fix below.
        Conversation conversation = getOrCreateConversation(chatRoom);

        // Tạo tin nhắn mới
        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(request.getContent())
                .type(request.getType())
                .mediaUrl(request.getMediaUrl())
                .fileName(request.getFileName())
                .fileSize(request.getFileSize())
                .status(MessageStatus.SENT)
                .build();

        Message savedMessage = messageRepository.save(message);

        // Cập nhật lastMessage của ChatRoom
        chatRoom.setLastMessage(savedMessage);
        chatRoomRepository.save(chatRoom);

        // Cập nhật unreadCount cho các thành viên khác
        List<ChatRoomMember> members = chatRoomMemberRepository.findByChatRoomAndIsActive(chatRoom, true);
        for (ChatRoomMember member : members) {
            if (!member.getUser().getId().equals(sender.getId())) {
                member.setUnreadCount(member.getUnreadCount() + 1);
                chatRoomMemberRepository.save(member);
            }
        }

        log.info("📨 Tin nhắn gửi thành công: ID={}, Phòng={}, Người gửi={}", 
                savedMessage.getId(), chatRoom.getId(), sender.getUsername());

        return mapToResponse(savedMessage);
    }

    /**
     * Ensures a {@link Conversation} row exists for this chat room id without optimistic-lock races
     * from parallel {@code save()} on the same manual PK.
     */
    private Conversation getOrCreateConversation(ChatRoom chatRoom) {
        Long roomId = chatRoom.getId();
        Object lock = ROOM_LOCKS.computeIfAbsent(roomId, k -> new Object());
        synchronized (lock) {
            return conversationRepository.findById(roomId).orElseGet(() -> {
                conversationRepository.upsertConversationRow(
                        roomId,
                        chatRoom.getName(),
                        chatRoom.getType().name()
                );
                return conversationRepository.findById(roomId)
                        .orElseThrow(() -> new RuntimeException(
                                "Không tạo được conversation cho phòng " + roomId));
            });
        }
    }

    @Override
    @Transactional
    public Page<ChatMessageResponse> getChatHistory(Long chatRoomId, Pageable pageable) {
        // Kiểm tra xem user có phải là thành viên không
        User currentUser = getCurrentUser();
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Phòng chat không tồn tại"));

        Boolean isMember = chatRoomMemberRepository.isMemberOfChatRoom(chatRoom, currentUser);
        if (!isMember) {
            throw new RuntimeException("Bạn không phải là thành viên của phòng chat này");
        }

        // Lấy lịch sử tin nhắn
        markRoomRead(chatRoom, currentUser);
        Page<Message> messages = messageRepository.findByChatRoomId(chatRoomId, pageable);

        List<ChatMessageResponse> responses = messages.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, messages.getTotalElements());
    }

    @Override
    public Page<ChatMessageResponse> getFileHistory(Long chatRoomId, Pageable pageable) {
        ensureCurrentUserIsRoomMember(chatRoomId);
        Page<Message> messages = messageRepository.findFilesByChatRoomId(chatRoomId, pageable);
        List<ChatMessageResponse> responses = messages.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, messages.getTotalElements());
    }

    @Override
    public Page<ChatMessageResponse> getLinkHistory(Long chatRoomId, Pageable pageable) {
        ensureCurrentUserIsRoomMember(chatRoomId);
        Page<Message> messages = messageRepository.findLinksByChatRoomId(chatRoomId, pageable);
        List<ChatMessageResponse> responses = messages.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, messages.getTotalElements());
    }

    private void ensureCurrentUserIsRoomMember(Long chatRoomId) {
        User currentUser = getCurrentUser();
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Phong chat khong ton tai"));
        Boolean isMember = chatRoomMemberRepository.isMemberOfChatRoom(chatRoom, currentUser);
        if (!isMember) {
            throw new RuntimeException("Ban khong phai la thanh vien cua phong chat nay");
        }
    }

    @Override
    public ChatMessageResponse getMessageById(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Tin nhắn không tồn tại"));

        return mapToResponse(message);
    }

    @Override
    @Transactional
    public void markAsRead(MarkMessageAsReadRequest request) {
        User currentUser = getCurrentUser();
        ChatRoom chatRoom = chatRoomRepository.findById(request.getChatRoomId())
                .orElseThrow(() -> new RuntimeException("Phòng chat không tồn tại"));

        ChatRoomMember member = chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, currentUser)
                .orElseThrow(() -> new RuntimeException("Bạn không phải là thành viên của phòng chat này"));

        // Cập nhật lastReadAt
        member.setLastReadAt(java.time.LocalDateTime.now());
        member.setUnreadCount(0);
        chatRoomMemberRepository.save(member);

        log.info("✅ Đã đánh dấu phòng chat {} là đã đọc", request.getChatRoomId());
    }

    private void markRoomRead(ChatRoom chatRoom, User user) {
        ChatRoomMember member = chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, user)
                .orElseThrow(() -> new RuntimeException("Ban khong phai la thanh vien cua phong chat nay"));
        member.setLastReadAt(java.time.LocalDateTime.now());
        member.setUnreadCount(0);
        chatRoomMemberRepository.save(member);
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Tin nhắn không tồn tại"));

        User currentUser = getCurrentUser();

        // Chỉ người gửi mới được xóa
        if (!message.getSender().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Chỉ người gửi mới có thể xóa tin nhắn");
        }

        messageRepository.delete(message);
        log.info("❌ Đã xóa tin nhắn: {}", messageId);
    }

    @Override
    @Transactional
    public ChatMessageResponse editMessage(Long messageId, String newContent) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Tin nhắn không tồn tại"));

        User currentUser = getCurrentUser();

        // Chỉ người gửi mới được chỉnh sửa
        if (!message.getSender().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Chỉ người gửi mới có thể chỉnh sửa tin nhắn");
        }

        message.setContent(newContent);
        Message updatedMessage = messageRepository.save(message);

        log.info("✏️ Tin nhắn {} đã được chỉnh sửa", messageId);
        return mapToResponse(updatedMessage);
    }

    @Override
    public ChatMessageResponse getLastMessage(Long chatRoomId) {
        Message lastMessage = messageRepository.findLastMessage(chatRoomId);
        if (lastMessage == null) {
            return null;
        }
        return mapToResponse(lastMessage);
    }

    @Override
    @Transactional
    public void updateMessageStatusToDelivered(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Tin nhắn không tồn tại"));

        message.setStatus(MessageStatus.DELIVERED);
        messageRepository.save(message);

        log.debug("📬 Cập nhật status tin nhắn {} thành DELIVERED", messageId);
    }

    /**
     * Chuyển Message entity thành Response DTO
     */
    private ChatMessageResponse mapToResponse(Message message) {
        ZoneId zone = ZoneId.systemDefault();
        return ChatMessageResponse.builder()
                .id(message.getId())
                .chatRoomId(message.getConversation().getId())
                .senderId(message.getSender().getId())
                .senderUsername(message.getSender().getUsername())
                .senderCode(getUserCode(message.getSender()))
                .senderFullName(message.getSender().getUsername())  // Có thể mở rộng
                .senderFullName(getUserFullName(message.getSender()))
                .senderAvatarUrl("https://ui-avatars.com/api/?name=" + getUserFullName(message.getSender()) + "&background=random")
                .content(message.getContent())
                .type(message.getType())
                .status(message.getStatus())
                .mediaUrl(message.getMediaUrl())
                .fileName(message.getFileName())
                .fileSize(message.getFileSize())
                .createdAt(message.getCreatedAt())
                .createdAtEpochMs(message.getCreatedAt() == null ? null : message.getCreatedAt().atZone(zone).toInstant().toEpochMilli())
                .updatedAt(message.getUpdatedAt())
                .updatedAtEpochMs(message.getUpdatedAt() == null ? null : message.getUpdatedAt().atZone(zone).toInstant().toEpochMilli())
                .build();
    }

    private String getUserCode(User user) {
        if (user.getStudent() != null) return user.getStudent().getStudentCode();
        if (user.getTeacher() != null) return user.getTeacher().getTeacherCode();
        return user.getUsername();
    }

    private String getUserFullName(User user) {
        if (user.getStudent() != null && user.getStudent().getFullName() != null) return user.getStudent().getFullName();
        if (user.getTeacher() != null && user.getTeacher().getFullName() != null) return user.getTeacher().getFullName();
        return user.getUsername();
    }
}
