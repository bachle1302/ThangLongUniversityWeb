package com.example.ThangLongUniversityWeb.kafka;

import com.example.ThangLongUniversityWeb.dto.request.EnrollmentMessage;
import com.example.ThangLongUniversityWeb.dto.response.EnrollmentStatusNotification;
import com.example.ThangLongUniversityWeb.entity.ClassSection;
import com.example.ThangLongUniversityWeb.entity.Enrollment;
import com.example.ThangLongUniversityWeb.entity.Student;
import com.example.ThangLongUniversityWeb.enums.EnrollmentRequestStatus;
import com.example.ThangLongUniversityWeb.enums.EnrollmentStatus;
import com.example.ThangLongUniversityWeb.repository.ClassSectionRepository;
import com.example.ThangLongUniversityWeb.repository.EnrollmentRepository;
import com.example.ThangLongUniversityWeb.repository.StudentRepository;
import com.example.ThangLongUniversityWeb.service.EnrollmentRequestStatusService;
import com.example.ThangLongUniversityWeb.service.SemesterRealtimeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Kafka consumer that stores course enrollment requests as PENDING.
 * Admin approval later promotes PENDING to REGISTERED.
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "spring.kafka.enabled", havingValue = "true")
public class EnrollmentConsumer {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentConsumer.class);

    private final EnrollmentRepository enrollmentRepository;
    private final ClassSectionRepository classSectionRepository;
    private final StudentRepository studentRepository;
    private final ObjectMapper objectMapper;
    private final EnrollmentRequestStatusService enrollmentRequestStatusService;
    private final SimpMessagingTemplate messagingTemplate;
    private final SemesterRealtimeService semesterRealtimeService;

    @KafkaListener(topics = "class-registration", groupId = "university-group")
    @Transactional
    public void consumeRegistrationMessage(String messageStr) {
        EnrollmentMessage message = null;
        try {
            message = objectMapper.readValue(messageStr, EnrollmentMessage.class);
            log.info(
                    "[Kafka Consumer] requestId={}, studentId={}, classSectionId={}",
                    message.getRequestId(),
                    message.getStudentId(),
                    message.getClassSectionId()
            );

            if (message.getRequestId() != null
                    && !enrollmentRequestStatusService.markIfFirstTimeProcessing(message.getRequestId())) {
                log.warn("[Kafka Consumer] Duplicate message ignored (requestId={})", message.getRequestId());
                return;
            }
            if (message.getRequestId() != null) {
                enrollmentRequestStatusService.markProcessing(message.getRequestId(), "Dang xu ly dang ky.");
            }

            final long studentId = message.getStudentId();
            final long classSectionId = message.getClassSectionId();
            Student student = studentRepository.findById(studentId).orElseThrow(
                    () -> new IllegalArgumentException("Khong tim thay sinh vien id=" + studentId)
            );
            ClassSection targetClass = classSectionRepository.findByIdForUpdate(classSectionId).orElseThrow(
                    () -> new IllegalArgumentException("Khong tim thay lop id=" + classSectionId)
            );

            if (enrollmentRepository.existsByStudentIdAndClassSectionId(student.getId(), targetClass.getId())) {
                if (message.getRequestId() != null) {
                    enrollmentRequestStatusService.markSuccess(
                            message.getRequestId(),
                            "Da ghi nhan truoc do (idempotent)."
                    );
                }
                pushStatus(
                        message.getUsername(),
                        message.getRequestId(),
                        EnrollmentRequestStatus.SUCCESS,
                        targetClass.getClassCode(),
                        "Lop " + targetClass.getClassCode() + " da co trong danh sach chon."
                );
                return;
            }

            if (targetClass.isClosed() || isSelectionFull(targetClass)) {
                log.warn(
                        "[Kafka Consumer] Class {} is full or closed (studentCode={})",
                        targetClass.getClassCode(),
                        student.getStudentCode()
                );
                if (message.getRequestId() != null) {
                    enrollmentRequestStatusService.markFailed(
                            message.getRequestId(),
                            "Lop da day si so hoac bi khoa."
                    );
                }
                pushStatus(
                        message.getUsername(),
                        message.getRequestId(),
                        EnrollmentRequestStatus.FAILED,
                        targetClass.getClassCode(),
                        "Dang ky that bai: lop " + targetClass.getClassCode() + " da day si so."
                );
                return;
            }

            Enrollment enrollment = new Enrollment();
            enrollment.setStudent(student);
            enrollment.setClassSection(targetClass);
            enrollment.setStatus(EnrollmentStatus.PENDING);
            enrollmentRepository.save(enrollment);

            log.info(
                    "[Kafka Consumer] Saved PENDING enrollment: studentCode={}, classCode={}",
                    student.getStudentCode(),
                    targetClass.getClassCode()
            );
            if (message.getRequestId() != null) {
                enrollmentRequestStatusService.markSuccess(
                        message.getRequestId(),
                        "Da them lop " + targetClass.getClassCode() + " vao danh sach cho xac nhan."
                );
            }
            pushStatus(
                    message.getUsername(),
                    message.getRequestId(),
                    EnrollmentRequestStatus.SUCCESS,
                    targetClass.getClassCode(),
                    "Da them lop " + targetClass.getClassCode() + " vao danh sach cho xac nhan."
            );
            semesterRealtimeService.publishAfterCommit(
                    targetClass.getSemester().getId(), "ENROLLMENTS");

        } catch (DataIntegrityViolationException dup) {
            log.warn("[Kafka Consumer] Duplicate enrollment by unique constraint: {}", dup.getMessage());
        } catch (Exception e) {
            log.error("[Kafka Consumer] Message processing error: {}", e.getMessage(), e);
            if (message != null && message.getRequestId() != null) {
                enrollmentRequestStatusService.markFailed(message.getRequestId(), "Loi he thong: " + e.getMessage());
            }
            if (message != null) {
                pushStatus(
                        message.getUsername(),
                        message.getRequestId(),
                        EnrollmentRequestStatus.FAILED,
                        null,
                        "Dang ky that bai: " + e.getMessage()
                );
            }
            throw new RuntimeException("Kafka consumer error: " + e.getMessage(), e);
        }
    }

    private boolean isSelectionFull(ClassSection targetClass) {
        if (targetClass.getMaxSlots() == null) {
            return false;
        }
        long activeSlots = enrollmentRepository.countByClassSectionIdAndStatusIn(
                targetClass.getId(),
                List.of(EnrollmentStatus.PENDING, EnrollmentStatus.REGISTERED)
        );
        return activeSlots >= targetClass.getMaxSlots();
    }

    private void pushStatus(String username, String requestId,
                            EnrollmentRequestStatus status, String classCode, String msg) {
        if (username == null) return;
        try {
            EnrollmentStatusNotification notification = EnrollmentStatusNotification.builder()
                    .requestId(requestId)
                    .status(status)
                    .classCode(classCode)
                    .message(msg)
                    .timestamp(System.currentTimeMillis())
                    .build();
            messagingTemplate.convertAndSendToUser(username, "/queue/enrollment-status", notification);
        } catch (Exception e) {
            log.warn("[Kafka Consumer] Cannot push WebSocket status to user {}: {}", username, e.getMessage());
        }
    }
}
