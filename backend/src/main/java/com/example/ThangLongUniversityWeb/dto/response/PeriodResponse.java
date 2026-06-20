package com.example.ThangLongUniversityWeb.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin tiết học")
public class PeriodResponse {
    @Schema(
            description = "ID của tiết học",
            example = "1"
    )
    private Long id;
    
    @Schema(
            description = "Số thứ tự tiết (1-12)",
            example = "1"
    )
    private Integer periodNumber;
    
    @Schema(
            description = "Giờ bắt đầu",
            example = "07:00"
    )
    private LocalTime startTime;
    
    @Schema(
            description = "Giờ kết thúc",
            example = "08:00"
    )
    private LocalTime endTime;
}
