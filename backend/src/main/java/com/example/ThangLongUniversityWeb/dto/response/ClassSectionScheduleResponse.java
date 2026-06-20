package com.example.ThangLongUniversityWeb.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
@Schema(description = "Thông tin lịch học của một lớp học phần")
public class ClassSectionScheduleResponse {
    @Schema(description = "ID lịch học", example = "1")
    private Long id;

    @Schema(description = "Ngày trong tuần (2-8: T2-CN)", example = "2")
    private Integer dayOfWeek;

    @Schema(description = "ID tiết học bắt đầu", example = "1")
    private Long startPeriodId;

    @Schema(description = "Số thứ tự tiết bắt đầu", example = "1")
    private Integer startPeriod;

    @Schema(description = "ID tiết học kết thúc", example = "4")
    private Long endPeriodId;

    @Schema(description = "Số thứ tự tiết kết thúc", example = "4")
    private Integer endPeriod;

    @Schema(description = "So tiet hoc cua lich nay", example = "3")
    private Integer lessonCount;

    @Schema(description = "Khoang tiet hoc dang text", example = "1-3")
    private String periodRange;

    @Schema(description = "Gio bat dau tiet dau", example = "07:00:00")
    private LocalTime startTime;

    @Schema(description = "Gio ket thuc tiet cuoi", example = "09:50:00")
    private LocalTime endTime;

    @Schema(description = "ID phòng học cho lịch này", example = "1")
    private Long roomId;

    @Schema(description = "Tên phòng học cho lịch này", example = "A301")
    private String roomName;
}
