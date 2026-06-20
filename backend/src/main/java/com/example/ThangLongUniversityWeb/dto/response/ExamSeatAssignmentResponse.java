package com.example.ThangLongUniversityWeb.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSeatAssignmentResponse {
    private Long id;
    private Long studentId;
    private String studentCode;
    private String studentName;
    private Long roomId;
    private String roomName;
    private Long roomAssignmentId;
    private String sourceType;
    private Long enrollmentId;
    private Long examRegistrationId;
    private String classCode;
}
