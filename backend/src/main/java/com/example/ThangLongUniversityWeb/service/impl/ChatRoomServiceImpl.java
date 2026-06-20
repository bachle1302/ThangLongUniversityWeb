package com.example.ThangLongUniversityWeb.service.impl;

import com.example.ThangLongUniversityWeb.dto.request.ChatRoomRequest;
import com.example.ThangLongUniversityWeb.dto.response.ChatRoomResponse;
import com.example.ThangLongUniversityWeb.dto.response.ChatRoomMemberResponse;
import com.example.ThangLongUniversityWeb.entity.ChatRoom;
import com.example.ThangLongUniversityWeb.entity.ChatRoomMember;
import com.example.ThangLongUniversityWeb.entity.User;
import com.example.ThangLongUniversityWeb.repository.ChatRoomRepository;
import com.example.ThangLongUniversityWeb.repository.ChatRoomMemberRepository;
import com.example.ThangLongUniversityWeb.repository.UserRepository;
import com.example.ThangLongUniversityWeb.service.ChatRoomService;
import com.example.ThangLongUniversityWeb.enums.ConversationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation của ChatRoomService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomServiceImpl implements ChatRoomService {
    
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;

    /**
     * Lấy user hiện tại từ JWT token
     */
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user: " + username));
    }

    @Override
    @Transactional
    public ChatRoomResponse createChatRoom(ChatRoomRequest request) {
        User currentUser = getCurrentUser();

        if (request.getType() != ConversationType.PRIVATE) {
            Set<Long> memberIds = request.getMemberIds() == null
                    ? new LinkedHashSet<>()
                    : new LinkedHashSet<>(request.getMemberIds());
            memberIds.remove(currentUser.getId());
            if (memberIds.size() < 2) {
                throw new RuntimeException("Nhom chat can it nhat 2 thanh vien khac ngoai nguoi tao");
            }
            request.setMemberIds(List.copyOf(memberIds));
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                request.setName("Nhom chat moi");
            }
        }

        // Nếu là chat 1-1, kiểm tra xem phòng đã tồn tại chưa
        if (request.getType() == ConversationType.PRIVATE) {
            User recipient = userRepository.findById(request.getRecipientId())
                    .orElseThrow(() -> new RuntimeException("Người nhận không tồn tại"));

            // Kiểm tra xem phòng 1-1 này đã có chưa
            var existingRoom = chatRoomRepository.findPrivateChatRoom(currentUser, recipient);
            if (existingRoom.isPresent()) {
                log.info("Phòng chat 1-1 đã tồn tại, trả về phòng cũ");
                return mapToResponse(existingRoom.get(), currentUser);
            }
        }

        // Tạo phòng chat mới
        String privateRoomName = null;
        if (request.getType() == ConversationType.PRIVATE && request.getRecipientId() != null) {
            User recipient = userRepository.findById(request.getRecipientId())
                    .orElseThrow(() -> new RuntimeException("Nguoi nhan khong ton tai"));
            privateRoomName = currentUser.getUsername() + " - " + recipient.getUsername();
        }

        ChatRoom chatRoom = ChatRoom.builder()
                .name(request.getType() == ConversationType.PRIVATE ? privateRoomName : request.getName().trim())
                .description(request.getDescription())
                .type(request.getType())
                .avatarUrl(request.getAvatarUrl())
                .creator(currentUser)
                .isActive(true)
                .memberCount(0)
                .build();

        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        // Thêm creator vào phòng chat
        addMemberToRoom(savedChatRoom, currentUser);

        // Thêm các thành viên khác (nếu có)
        if (request.getType() == ConversationType.PRIVATE && request.getRecipientId() != null) {
            User recipient = userRepository.findById(request.getRecipientId())
                    .orElseThrow(() -> new RuntimeException("Người nhận không tồn tại"));
            addMemberToRoom(savedChatRoom, recipient);
        } else if (request.getType() != ConversationType.PRIVATE && request.getMemberIds() != null) {
            for (Long memberId : request.getMemberIds()) {
                User member = userRepository.findById(memberId)
                        .orElseThrow(() -> new RuntimeException("Thành viên không tồn tại: " + memberId));
                addMemberToRoom(savedChatRoom, member);
            }
        }

        log.info("✅ Tạo phòng chat thành công: {} (Type: {})", savedChatRoom.getId(), request.getType());
        return mapToResponse(savedChatRoom, currentUser);
    }

    @Override
    public Page<ChatRoomResponse> getMyChatRooms(Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<ChatRoom> chatRooms = chatRoomRepository.findAllChatRoomsByUser(currentUser, pageable);

        List<ChatRoomResponse> responses = chatRooms.getContent().stream()
                .map(room -> mapToResponse(room, currentUser))
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, chatRooms.getTotalElements());
    }

    @Override
    public ChatRoomResponse getChatRoom(Long chatRoomId) {
        User currentUser = getCurrentUser();
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Phòng chat không tồn tại"));

        // Kiểm tra xem user có phải là thành viên không
        Boolean isMember = chatRoomMemberRepository.isMemberOfChatRoom(chatRoom, currentUser);
        if (!isMember) {
            throw new RuntimeException("Bạn không phải là thành viên của phòng chat này");
        }

        return mapToResponse(chatRoom, currentUser);
    }

    @Override
    @Transactional
    public ChatRoomResponse updateChatRoom(Long chatRoomId, ChatRoomRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Phòng chat không tồn tại"));

        User currentUser = getCurrentUser();
        
        // Chỉ creator mới được phép chỉnh sửa
        if (!chatRoom.getCreator().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Chỉ creator của phòng mới có thể chỉnh sửa");
        }

        if (request.getName() != null) {
            chatRoom.setName(request.getName());
        }
        if (request.getDescription() != null) {
            chatRoom.setDescription(request.getDescription());
        }
        if (request.getAvatarUrl() != null) {
            chatRoom.setAvatarUrl(request.getAvatarUrl());
        }

        ChatRoom updatedRoom = chatRoomRepository.save(chatRoom);
        return mapToResponse(updatedRoom, currentUser);
    }

    @Override
    @Transactional
    public void deleteChatRoom(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Phòng chat không tồn tại"));

        User currentUser = getCurrentUser();
        
        // Chỉ creator mới được phép xóa
        if (!chatRoom.getCreator().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Chỉ creator của phòng mới có thể xóa");
        }

        chatRoom.setIsActive(false);
        chatRoomRepository.save(chatRoom);
        log.info("❌ Đã xóa (mềm) phòng chat: {}", chatRoomId);
    }

    @Override
    @Transactional
    public ChatRoomResponse addMemberToChatRoom(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Phòng chat không tồn tại"));

        User newMember = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        User currentUser = getCurrentUser();
        
        // Chỉ creator hoặc admin mới được phép thêm thành viên
        if (!chatRoom.getCreator().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền thêm thành viên");
        }

        addMemberToRoom(chatRoom, newMember);
        User finalCurrentUser = currentUser;
        ChatRoom updatedRoom = chatRoomRepository.findById(chatRoomId).get();
        return mapToResponse(updatedRoom, finalCurrentUser);
    }

    @Override
    @Transactional
    public ChatRoomResponse removeMemberFromChatRoom(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Phòng chat không tồn tại"));

        User memberToRemove = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        User currentUser = getCurrentUser();

        // Chỉ creator hoặc chính người đó mới được phép xóa
        if (!chatRoom.getCreator().getId().equals(currentUser.getId()) &&
            !currentUser.getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xóa thành viên");
        }

        ChatRoomMember member = chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, memberToRemove)
                .orElseThrow(() -> new RuntimeException("Thành viên không tồn tại trong phòng"));

        member.setIsActive(false);
        chatRoomMemberRepository.save(member);

        // Cập nhật số lượng thành viên
        Integer activeCount = chatRoomMemberRepository.countActiveMembers(chatRoom);
        chatRoom.setMemberCount(activeCount);
        ChatRoom updatedRoom = chatRoomRepository.save(chatRoom);

        log.info("👤 Đã xóa thành viên {} khỏi phòng chat {}", userId, chatRoomId);
        return mapToResponse(updatedRoom, currentUser);
    }

    @Override
    @Transactional
    public void leaveChatRoom(Long chatRoomId) {
        User currentUser = getCurrentUser();
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Phòng chat không tồn tại"));

        removeMemberFromChatRoom(chatRoomId, currentUser.getId());
        log.info("👋 User {} đã rời khỏi phòng chat {}", currentUser.getUsername(), chatRoomId);
    }

    @Override
    @Transactional
    public ChatRoomResponse getOrCreatePrivateChatRoom(Long otherUserId) {
        User currentUser = getCurrentUser();
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        if (currentUser.getId().equals(otherUserId)) {
            throw new RuntimeException("Không thể tạo phòng chat với chính mình");
        }

        // Kiểm tra xem phòng chat 1-1 đã tồn tại chưa
        var existingRoom = chatRoomRepository.findPrivateChatRoom(currentUser, otherUser);
        if (existingRoom.isPresent()) {
            return mapToResponse(existingRoom.get(), currentUser);
        }

        // Nếu chưa có, tạo phòng mới
        ChatRoomRequest request = ChatRoomRequest.builder()
                .type(ConversationType.PRIVATE)
                .recipientId(otherUserId)
                .build();

        return createChatRoom(request);
    }

    /**
     * Helper method để thêm thành viên vào phòng chat
     */
    private void addMemberToRoom(ChatRoom chatRoom, User user) {
        var existing = chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, user);
        if (existing.isPresent()) {
            ChatRoomMember member = existing.get();
            if (!Boolean.TRUE.equals(member.getIsActive())) {
                member.setIsActive(true);
                chatRoomMemberRepository.save(member);
            }
            return;
        }
        ChatRoomMember member = ChatRoomMember.builder()
                .chatRoom(chatRoom)
                .user(user)
                .isActive(true)
                .build();
        chatRoomMemberRepository.save(member);

        // Cập nhật số lượng thành viên
        Integer memberCount = chatRoomMemberRepository.countActiveMembers(chatRoom);
        chatRoom.setMemberCount(memberCount);
        chatRoomRepository.save(chatRoom);
    }

    /**
     * Chuyển ChatRoom entity thành Response DTO
     */
    private ChatRoomResponse mapToResponse(ChatRoom chatRoom, User currentUser) {
        // Lấy danh sách thành viên
        List<ChatRoomMember> members = chatRoomMemberRepository.findByChatRoomAndIsActive(chatRoom, true);
        List<ChatRoomMemberResponse> memberResponses = members.stream()
                .map(this::mapMemberToResponse)
                .collect(Collectors.toList());

        // Lấy thông tin tin nhắn cuối cùng nếu có
        String lastMessagePreview = "";
        String lastMessageSender = "";
        if (chatRoom.getLastMessage() != null) {
            lastMessagePreview = chatRoom.getLastMessage().getContent().substring(0, Math.min(50, chatRoom.getLastMessage().getContent().length()));
            lastMessageSender = chatRoom.getLastMessage().getSender().getUsername();
        }

        // Lấy số tin nhắn chưa đọc
        ChatRoomMember currentMember = chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, currentUser).orElse(null);
        Integer unreadCount = currentMember != null ? currentMember.getUnreadCount() : 0;

        return ChatRoomResponse.builder()
                .id(chatRoom.getId())
                .name(chatRoom.getName())
                .description(chatRoom.getDescription())
                .type(chatRoom.getType())
                .avatarUrl(chatRoom.getAvatarUrl())
                .creatorId(chatRoom.getCreator().getId())
                .creatorUsername(chatRoom.getCreator().getUsername())
                .lastMessagePreview(lastMessagePreview)
                .lastMessageSender(lastMessageSender)
                .lastMessageTime(chatRoom.getLastMessage() != null ? chatRoom.getLastMessage().getCreatedAt() : null)
                .memberCount(chatRoom.getMemberCount())
                .unreadCount(unreadCount)
                .members(memberResponses)
                .createdAt(chatRoom.getCreatedAt())
                .isActive(chatRoom.getIsActive())
                .build();
    }

    /**
     * Chuyển ChatRoomMember entity thành Response DTO
     */
    private ChatRoomMemberResponse mapMemberToResponse(ChatRoomMember member) {
        return ChatRoomMemberResponse.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .username(member.getUser().getUsername())
                .code(getUserCode(member.getUser()))
                .fullName(member.getUser().getUsername())  // Có thể mở rộng để lấy fullName từ Student/Teacher
                .fullName(getUserFullName(member.getUser()))
                .role(member.getUser().getRole().name())
                .joinedAt(member.getJoinedAt())
                .lastReadAt(member.getLastReadAt())
                .unreadCount(member.getUnreadCount())
                .isOnline(false)  // Có thể kiểm tra từ Redis cache online users
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
