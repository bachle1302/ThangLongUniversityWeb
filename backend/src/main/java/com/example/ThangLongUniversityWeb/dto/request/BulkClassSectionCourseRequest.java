package com.example.ThangLongUniversityWeb.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BulkClassSectionCourseRequest {
    @NotNull
    private Long courseId;

    @NotNull
    @Min(1)
    @Max(20)
    private Integer classCount;

    @NotNull
    @Min(1)
    private Integer maxSlots;

    @NotNull
    @Min(1)
    @Max(6)
    private Integer sessionsPerWeek;

    @NotNull
    @Min(1)
    @Max(14)
    private Integer periodsPerSession;
}
