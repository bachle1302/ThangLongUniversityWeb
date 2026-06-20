package com.example.ThangLongUniversityWeb.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDate;

@Data
@Schema(description = "Yêu cầu tạo/cập nhật học kỳ")
public class SemesterRequest {
    @Schema(
            description = "Tên học kỳ",
            example = "HK1 2025-2026",
            required = true
    )
    private String name;
    
    @Schema(
            description = "Ngày bắt đầu học kỳ",
            example = "2025-09-01",
            required = true
    )
    private LocalDate startDate;
    
    @Schema(
            description = "Ngày kết thúc học kỳ",
            example = "2025-12-31",
            required = true
    )
    private LocalDate endDate;
    
    @Schema(
            description = "Trạng thái đăng ký tín chỉ (mở/đóng)",
            example = "true"
    )
    private boolean isRegistrationOpen;
}