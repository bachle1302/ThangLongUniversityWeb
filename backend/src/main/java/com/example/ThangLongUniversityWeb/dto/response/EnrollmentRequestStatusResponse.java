package com.example.ThangLongUniversityWeb.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Trạng thái yêu cầu ghi danh")
public class EnrollmentRequestStatusResponse {
    @Schema(description = "ID yêu cầu", example = "req_001")
    private String requestId;
    
    @Schema(description = "Trạng thái yêu cầu", example = "PENDING", allowableValues = {"PENDING", "PROCESSING", "SUCCESS", "FAILED"})
    private String status;
    
    @Schema(description = "Thông điệp", example = "Yêu cầu đang chờ xử lý")
    private String message;
}

