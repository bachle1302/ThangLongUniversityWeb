package com.example.ThangLongUniversityWeb.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminClassSectionStudentResponse {
    private Long enrollmentId;
    private Long studentId;
    private String studentCode;
    private String fullName;
    private String email;
    private Long majorId;
    private String majorCode;
    private String majorName;
    private String cohort;
    private Integer academicYear;
    private String enrolledAt;
    private String status;
}
