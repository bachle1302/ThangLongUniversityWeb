package com.example.ThangLongUniversityWeb.service.impl;

import com.example.ThangLongUniversityWeb.dto.response.EnrollmentRequestResponse;
import com.example.ThangLongUniversityWeb.dto.response.EnrollmentStatusNotification;
import com.example.ThangLongUniversityWeb.entity.ClassSection;
import com.example.ThangLongUniversityWeb.entity.Enrollment;
import com.example.ThangLongUniversityWeb.entity.Student;
import com.example.ThangLongUniversityWeb.enums.EnrollmentRequestStatus;
import com.example.ThangLongUniversityWeb.enums.EnrollmentStatus;
import com.example.ThangLongUniversityWeb.repository.ClassSectionRepository;
import com.example.ThangLongUniversityWeb.repository.EnrollmentRepository;
import com.example.ThangLongUniversityWeb.service.EnrollmentProcessor;
import com.example.ThangLongUniversityWeb.service.EnrollmentRequestStatusService;
import com.example.ThangLongUniversityWeb.service.SemesterRealtimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class DirectEnrollmentProcessor implements EnrollmentProcessor {

    private final EnrollmentRepository enrollmentRepository;
    private final ClassSectionRepository classSectionRepository;
    private final EnrollmentRequestStatusService statusService;
    private final SimpMessagingTemplate messagingTemplate;
    private final SemesterRealtimeService semesterRealtimeService;

    @Override
    @Transactional
    public EnrollmentRequestResponse process(Student student, ClassSection targetClass) {
        String requestId = UUID.randomUUID().toString();
        String username = student.getUser().getUsername();
        String classCode = targetClass.getClassCode();

        statusService.markProcessing(requestId, "Dang ghi nhan dang ky...");

        try {
            ClassSection lockedClass = classSectionRepository.findByIdForUpdate(targetClass.getId())
                    .orElseThrow(() -> new RuntimeException("Lop hoc phan khong ton tai."));

            if (lockedClass.isClosed() || isSelectionFull(lockedClass)) {
                throw new RuntimeException("Lop " + lockedClass.getClassCode() + " da day si so hoac da bi khoa.");
            }

            Enrollment enrollment = new Enrollment();
            enrollment.setStudent(student);
            enrollment.setClassSection(lockedClass);
            enrollment.setStatus(EnrollmentStatus.PENDING);
            enrollmentRepository.save(enrollment);

            String successMessage = "Da them lop " + classCode + " vao danh sach cho xac nhan.";
            statusService.markSuccess(requestId, successMessage);
            log.info("[Direct] Student {} selected class {}", username, classCode);

            pushStatus(username, requestId, EnrollmentRequestStatus.SUCCESS, classCode, successMessage);
            semesterRealtimeService.publishAfterCommit(
                    lockedClass.getSemester().getId(), "ENROLLMENTS");
            return new EnrollmentRequestResponse(requestId, successMessage);
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Loi he thong khi xu ly dang ky.";
            statusService.markFailed(requestId, errorMessage);
            log.error("[Direct] Enrollment failed for class {} and student {}: {}", classCode, username, errorMessage);

            pushStatus(username, requestId, EnrollmentRequestStatus.FAILED, classCode, "Dang ky that bai: " + errorMessage);
            throw new RuntimeException(errorMessage);
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
                            EnrollmentRequestStatus status, String classCode, String message) {
        try {
            EnrollmentStatusNotification notification = EnrollmentStatusNotification.builder()
                    .requestId(requestId)
                    .status(status)
                    .classCode(classCode)
                    .message(message)
                    .timestamp(System.currentTimeMillis())
                    .build();

            messagingTemplate.convertAndSendToUser(username, "/queue/enrollment-status", notification);
        } catch (Exception e) {
            log.warn("Cannot push enrollment status to user {}: {}", username, e.getMessage());
        }
    }
}
