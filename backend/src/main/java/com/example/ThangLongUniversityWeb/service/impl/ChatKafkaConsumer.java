package com.example.ThangLongUniversityWeb.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka Consumer cho các topic chat.
 * Kích hoạt khi spring.kafka.enabled=true.
 *
 * TODO: Implement xử lý message thực tế khi có Kafka broker.
 * Hiện tại chỉ log để xác nhận message đã được nhận.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true")
public class ChatKafkaConsumer {

    /**
     * Xử lý tin nhắn chat từ Kafka (persistence / analytics layer).
     * Topic: chat-messages
     */
    @KafkaListener(topics = "chat-messages", groupId = "chat-consumer-group")
    public void consumeChatMessage(String messageJson) {
        log.info("[Kafka] Nhận chat-message: {}", messageJson);
        // TODO: parse JSON -> lưu DB hoặc push notification nếu user offline
    }

    /**
     * Xử lý thông báo chat (push notification).
     * Topic: chat-notifications
     */
    @KafkaListener(topics = "chat-notifications", groupId = "chat-notification-group")
    public void consumeChatNotification(String notificationJson) {
        log.info("[Kafka] Nhận chat-notification: {}", notificationJson);
        // TODO: push FCM / email notification cho user offline
    }

    /**
     * Xử lý analytics chat (tracking, analytics pipeline).
     * Topic: chat-analytics
     */
    @KafkaListener(topics = "chat-analytics", groupId = "chat-analytics-group")
    public void consumeChatAnalytics(String analyticsJson) {
        log.debug("[Kafka] Nhận chat-analytics: {}", analyticsJson);
        // TODO: ghi vào time-series DB hoặc data lake
    }
}
