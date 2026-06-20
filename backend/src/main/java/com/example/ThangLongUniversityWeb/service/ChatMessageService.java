package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.dto.request.ChatMessageRequest;
import com.example.ThangLongUniversityWeb.dto.request.MarkMessageAsReadRequest;
import com.example.ThangLongUniversityWeb.dto.response.ChatMessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface cho Chat Message use cases
 */
public interface ChatMessageService {
    
    /**
     * Gửi tin nhắn mới
     */
    ChatMessageResponse sendMessage(ChatMessageRequest request);

    /**
     * Lấy lịch sử tin nhắn của một phòng chat
     */
    Page<ChatMessageResponse> getChatHistory(Long chatRoomId, Pageable pageable);

    Page<ChatMessageResponse> getFileHistory(Long chatRoomId, Pageable pageable);

    Page<ChatMessageResponse> getLinkHistory(Long chatRoomId, Pageable pageable);

    /**
     * Lấy tin nhắn cụ thể
     */
    ChatMessageResponse getMessageById(Long messageId);

    /**
     * Đánh dấu tin nhắn/phòng chat là đã đọc
     */
    void markAsRead(MarkMessageAsReadRequest request);

    /**
     * Xóa tin nhắn
     */
    void deleteMessage(Long messageId);

    /**
     * Chỉnh sửa nội dung tin nhắn
     */
    ChatMessageResponse editMessage(Long messageId, String newContent);

    /**
     * Lấy tin nhắn gần nhất trong phòng chat (để hiển thị preview)
     */
    ChatMessageResponse getLastMessage(Long chatRoomId);

    /**
     * Cập nhật status tin nhắn thành DELIVERED
     */
    void updateMessageStatusToDelivered(Long messageId);
}
