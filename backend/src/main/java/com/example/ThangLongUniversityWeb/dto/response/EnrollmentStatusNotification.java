package com.example.ThangLongUniversityWeb.dto.response;

import com.example.ThangLongUniversityWeb.enums.EnrollmentRequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload WebSocket push về trạng thái đăng ký học phần.
 * Destination: /user/{username}/queue/enrollment-status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông báo realtime trạng thái đăng ký học phần qua WebSocket")
public class EnrollmentStatusNotification {

    @Schema(description = "Request ID của yêu cầu đăng ký", example = "abc-123-def")
    private String requestId;

    @Schema(description = "Trạng thái xử lý", example = "SUCCESS")
    private EnrollmentRequestStatus status;

    @Schema(description = "Mã lớp học phần", example = "IT001.N1")
    private String classCode;

    @Schema(description = "Thông điệp chi tiết", example = "Đăng ký lớp IT001.N1 thành công!")
    private String message;

    @Schema(description = "Timestamp (ms)", example = "1716100000000")
    private long timestamp;
}
