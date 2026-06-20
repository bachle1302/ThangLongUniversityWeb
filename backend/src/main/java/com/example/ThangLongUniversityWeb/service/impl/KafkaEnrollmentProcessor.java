package com.example.ThangLongUniversityWeb.service.impl;

import com.example.ThangLongUniversityWeb.dto.request.EnrollmentMessage;
import com.example.ThangLongUniversityWeb.dto.response.EnrollmentRequestResponse;
import com.example.ThangLongUniversityWeb.entity.ClassSection;
import com.example.ThangLongUniversityWeb.entity.Student;
import com.example.ThangLongUniversityWeb.service.EnrollmentProcessor;
import com.example.ThangLongUniversityWeb.service.EnrollmentRequestStatusService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Process course enrollment through Kafka.
 * Active when spring.kafka.enabled=true.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true")
public class KafkaEnrollmentProcessor implements EnrollmentProcessor {

    private static final String TOPIC = "class-registration";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final EnrollmentRequestStatusService statusService;

    @Override
    public EnrollmentRequestResponse process(Student student, ClassSection targetClass) {
        String requestId = UUID.randomUUID().toString();
        String username = student.getUser().getUsername();
        String classCode = targetClass.getClassCode();

        try {
            statusService.markPending(requestId, "Da tiep nhan don dang ky, dang cho xu ly.");

            EnrollmentMessage message =
                    new EnrollmentMessage(requestId, student.getId(), targetClass.getId(), username);
            String messageJson = objectMapper.writeValueAsString(message);

            kafkaTemplate.send(TOPIC, messageJson).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("[Kafka] Send message failed for requestId={}: {}", requestId, ex.getMessage());
                } else {
                    log.info(
                            "[Kafka] Published enrollment request to topic {}: studentCode={}, classCode={} (partition={}, offset={})",
                            TOPIC,
                            student.getStudentCode(),
                            classCode,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset()
                    );
                }
            });

            return new EnrollmentRequestResponse(
                    requestId,
                    "He thong da tiep nhan don dang ky lop " + classCode + ". Vui long cho xu ly!"
            );

        } catch (Exception e) {
            statusService.markFailed(requestId, "Loi Kafka: " + e.getMessage());
            log.error("[Kafka] Publish message error: {}", e.getMessage());
            throw new RuntimeException("He thong xu ly hang doi dang ban. Vui long thu lai sau!");
        }
    }
}
