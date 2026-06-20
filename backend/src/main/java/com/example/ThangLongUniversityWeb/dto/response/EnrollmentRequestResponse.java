package com.example.ThangLongUniversityWeb.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Phản hồi yêu cầu ghi danh")
public class EnrollmentRequestResponse {
    @Schema(
            description = "ID yêu cầu",
            example = "req_001"
    )
    private String requestId;
    
    @Schema(
            description = "Thông điếp phản hồi",
            example = "Yêu cầu ghi danh đã được gửi thành công"
    )
    private String message;
}

