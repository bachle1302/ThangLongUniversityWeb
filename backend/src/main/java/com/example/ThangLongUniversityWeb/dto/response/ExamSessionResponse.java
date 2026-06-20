package com.example.ThangLongUniversityWeb.dto.response;

import com.example.ThangLongUniversityWeb.enums.ExamType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSessionResponse {
    private Long id;
    private Long semesterId;
    private String semesterName;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private Integer credits;
    private ExamType examType;
    private LocalDateTime examAt;
    private Integer studentCount;
    private List<ExamRoomAssignmentResponse> rooms;
    private String candidateSelection;
    private Integer assignedRetakeCount;
    private String virtualClassCode;
    private Long virtualClassSectionId;
    private List<String> assignmentWarnings;
}
