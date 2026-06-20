package com.example.ThangLongUniversityWeb.dto.request;

import com.example.ThangLongUniversityWeb.enums.ExamType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExamScheduleRequest {
    private Long classSectionId;
    private LocalDateTime examAt;
    private String examRoom;
    private ExamType examType; // NORMAL, RETAKE, IMPROVE
}
