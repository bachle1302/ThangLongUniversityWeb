package com.example.ThangLongUniversityWeb.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class AttendanceSessionResponse {
    private Long id;
    private Long classSectionId;
    private Integer sessionNumber;
    private Integer weekNumber;
    private Integer meetingIndex;
    private LocalDate sessionDate;
    private boolean locked;
    private List<AttendanceRecordResponse> records;
}
