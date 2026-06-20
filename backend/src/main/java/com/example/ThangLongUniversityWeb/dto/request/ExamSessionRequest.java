package com.example.ThangLongUniversityWeb.dto.request;

import com.example.ThangLongUniversityWeb.enums.ExamType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExamSessionRequest {
    private Long courseId;
    private ExamType examType;
    private LocalDateTime examAt;
    private List<Long> roomIds;
    private List<Long> proctorIds;
    private String allocationMethod;
    private String candidateSelection;
}
