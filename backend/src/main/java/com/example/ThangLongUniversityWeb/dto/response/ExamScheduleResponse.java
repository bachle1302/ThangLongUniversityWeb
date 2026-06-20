package com.example.ThangLongUniversityWeb.dto.response;

import com.example.ThangLongUniversityWeb.enums.ExamType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamScheduleResponse {
    private Long classSectionId;
    private String classCode;
    private String courseName;
    private String courseCode;
    private Integer credits;
    private String teacherName;
    private LocalDateTime examAt;
    private String examRoom;
    private ExamType examType;
    private Integer studentCount;
    private Long semesterId;
    private String semesterName;
}
