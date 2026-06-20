package com.example.ThangLongUniversityWeb.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu thêm lịch học (ngày + tiết)")
public class ClassSectionScheduleRequest {
    @Schema(
            description = "Ngày trong tuần (2-8: T2-CN)",
            example = "2",
            required = true,
            minimum = "2",
            maximum = "8"
    )
    @NotNull(message = "Ngày trong tuần không được để trống")
    @Min(value = 2, message = "Ngày phải từ 2-8")
    @Max(value = 8, message = "Ngày phải từ 2-8")
    private Integer dayOfWeek;

    @Schema(
            description = "ID tiết học bắt đầu",
            example = "1",
            required = true
    )
    @NotNull(message = "ID tiết học bắt đầu không được để trống")
    private Long startPeriodId;

    @Schema(
            description = "ID tiết học kết thúc",
            example = "4",
            required = true
    )
    @NotNull(message = "ID tiết học kết thúc không được để trống")
    private Long endPeriodId;

    @Schema(
            description = "ID của phòng học cho ngày này",
            example = "1",
            required = true
    )
    @NotNull(message = "ID phòng học cho ngày thực hiện không được để trống")
    private Long roomId;
}
