package com.example.ThangLongUniversityWeb.controller;

import com.example.ThangLongUniversityWeb.dto.response.EnrollmentRequestStatusResponse;
import com.example.ThangLongUniversityWeb.service.EnrollmentRequestStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * WebSocket controller để sinh viên subscribe nhận trạng thái đăng ký học phần realtime.
 *
 * Flow:
 *  1. Sinh viên gọi REST POST /api/student/enroll/{classSectionId}
 *     → nhận về { requestId, message }
 *
 *  2. Sinh viên subscribe WebSocket topic:
 *     /user/{username}/queue/enrollment-status
 *
 *  3. Server push EnrollmentStatusNotification về destination trên khi xử lý xong
 *     (trong DirectEnrollmentProcessor hoặc KafkaEnrollmentConsumer).
 *
 *  4. Sinh viên cũng có thể poll REST: GET /api/student/enrollments/status/{requestId}
 *     nếu không dùng WebSocket.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Enrollment WebSocket")
@SecurityRequirement(name = "bearerAuth")
public class EnrollmentWebSocketController {

    private final EnrollmentRequestStatusService statusService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Client gửi: /app/enrollment/subscribe/{requestId}
     * Server phản hồi ngay trạng thái hiện tại qua /user/{username}/queue/enrollment-status
     * (Dùng để đồng bộ trạng thái khi client reconnect WebSocket sau khi đã submit đơn)
     */
    @MessageMapping("/enrollment/subscribe/{requestId}")
    public void subscribeEnrollmentStatus(
            @DestinationVariable String requestId,
            SimpMessageHeaderAccessor headerAccessor) {
        try {
            String username = (String) headerAccessor.getSessionAttributes().get("username");
            if (username == null || username.isBlank()) {
                log.warn("⚠️ WebSocket enrollment subscribe: username null cho requestId={}", requestId);
                return;
            }

            EnrollmentRequestStatusResponse current = statusService.getStatus(requestId);
            log.info("📡 User {} subscribe enrollment status requestId={} → {}", username, requestId, current.getStatus());

            messagingTemplate.convertAndSendToUser(
                    username,
                    "/queue/enrollment-status",
                    current
            );
        } catch (Exception e) {
            log.error("❌ Lỗi subscription enrollment status: {}", e.getMessage());
        }
    }
}
