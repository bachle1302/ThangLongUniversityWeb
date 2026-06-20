package com.example.ThangLongUniversityWeb.kafka;

import com.example.ThangLongUniversityWeb.dto.response.ChatMessageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka Producer để xử lý tin nhắn chat bất đồng bộ
 * 
 * Các topic sử dụng:
 * - chat-messages: Lưu trữ tin nhắn vào database
 * - chat-notifications: Gửi thông báo push
 * - chat-analytics: Thu thập thống kê
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true")
public class ChatKafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Gửi tin nhắn chat để lưu vào database
     */
    public void sendMessageForPersistence(ChatMessageResponse message) {
        try {
            String messageJson = objectMapper.writeValueAsString(message);
            kafkaTemplate.send("chat-messages", messageJson);
            log.debug("📨 Đã gửi tin nhắn {} vào Kafka để lưu trữ", message.getId());
        } catch (Exception e) {
            log.error("❌ Lỗi gửi tin nhắn vào Kafka: {}", e.getMessage());
        }
    }

    /**
     * Gửi thông báo cho người nhận
     */
    public void sendNotification(ChatMessageResponse message) {
        try {
            String notificationJson = objectMapper.writeValueAsString(message);
            kafkaTemplate.send("chat-notifications", notificationJson);
            log.debug("🔔 Đã gửi thông báo cho tin nhắn {}", message.getId());
        } catch (Exception e) {
            log.error("❌ Lỗi gửi thông báo: {}", e.getMessage());
        }
    }

    /**
     * Gửi dữ liệu để thu thập analytics
     */
    public void sendAnalyticsData(ChatMessageResponse message) {
        try {
            String analyticsJson = objectMapper.writeValueAsString(message);
            kafkaTemplate.send("chat-analytics", analyticsJson);
            log.debug("📊 Đã gửi dữ liệu analytics cho tin nhắn {}", message.getId());
        } catch (Exception e) {
            log.error("❌ Lỗi gửi analytics: {}", e.getMessage());
        }
    }

    /**
     * Gửi tin nhắn chat với tất cả các xử lý bất đồng bộ
     */
    public void sendChatMessage(ChatMessageResponse message) {
        // 1. Lưu tin nhắn vào database
        sendMessageForPersistence(message);

        // 2. Gửi thông báo
        sendNotification(message);

        // 3. Thu thập analytics
        sendAnalyticsData(message);
    }
}
