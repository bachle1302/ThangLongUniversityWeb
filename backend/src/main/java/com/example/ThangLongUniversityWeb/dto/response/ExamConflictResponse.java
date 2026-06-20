package com.example.ThangLongUniversityWeb.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamConflictResponse {
    private String studentCode;
    private String studentName;
    private String conflictingCourseCode;
    private String conflictingCourseName;
}
