package com.example.ThangLongUniversityWeb.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

import java.time.LocalTime;

@Data
@Schema(description = "Yêu cầu tạo/cập nhật tiết học")
public class PeriodRequest {
    @Schema(
            description = "Số thứ tự tiết học (1-12)",
            example = "1",
            required = true,
            minimum = "1",
            maximum = "12"
    )
    @NotNull(message = "Số tiết học không được để trống")
    @Min(value = 1, message = "Số tiết phải từ 1-12")
    @Max(value = 12, message = "Số tiết phải từ 1-12")
    private Integer periodNumber;
    
    @Schema(
            description = "Giờ bắt đầu tiết học",
            example = "07:00",
            required = true
    )
    @NotNull(message = "Giờ bắt đầu không được để trống")
    private LocalTime startTime;
    
    @Schema(
            description = "Giờ kết thúc tiết học",
            example = "08:00",
            required = true
    )
    @NotNull(message = "Giờ kết thúc không được để trống")
    private LocalTime endTime;
}