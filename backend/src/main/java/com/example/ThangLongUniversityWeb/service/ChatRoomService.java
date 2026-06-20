package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.dto.request.ChatRoomRequest;
import com.example.ThangLongUniversityWeb.dto.response.ChatRoomResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface cho Chat Room use cases
 */
public interface ChatRoomService {
    
    /**
     * Tạo phòng chat mới (1-1, GROUP, hoặc CLASS_GROUP)
     */
    ChatRoomResponse createChatRoom(ChatRoomRequest request);

    /**
     * Lấy danh sách phòng chat của người dùng hiện tại
     */
    Page<ChatRoomResponse> getMyChatRooms(Pageable pageable);

    /**
     * Lấy chi tiết một phòng chat
     */
    ChatRoomResponse getChatRoom(Long chatRoomId);

    /**
     * Cập nhật thông tin phòng chat
     */
    ChatRoomResponse updateChatRoom(Long chatRoomId, ChatRoomRequest request);

    /**
     * Xóa phòng chat (xóa mềm)
     */
    void deleteChatRoom(Long chatRoomId);

    /**
     * Thêm thành viên vào phòng chat
     */
    ChatRoomResponse addMemberToChatRoom(Long chatRoomId, Long userId);

    /**
     * Xóa thành viên khỏi phòng chat
     */
    ChatRoomResponse removeMemberFromChatRoom(Long chatRoomId, Long userId);

    /**
     * Rời khỏi phòng chat
     */
    void leaveChatRoom(Long chatRoomId);

    /**
     * Lấy hoặc tạo phòng chat 1-1 với người khác
     */
    ChatRoomResponse getOrCreatePrivateChatRoom(Long otherUserId);
}
