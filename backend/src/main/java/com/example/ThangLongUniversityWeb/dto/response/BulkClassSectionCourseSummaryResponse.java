package com.example.ThangLongUniversityWeb.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkClassSectionCourseSummaryResponse {
    private Long courseId;
    private String courseCode;
    private String courseName;
    private Integer requestedCount;
    private Integer proposedCount;
    private Integer missingCount;
    private String message;
}
