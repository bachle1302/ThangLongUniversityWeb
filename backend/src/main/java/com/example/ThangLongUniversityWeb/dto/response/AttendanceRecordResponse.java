package com.example.ThangLongUniversityWeb.dto.response;

import com.example.ThangLongUniversityWeb.enums.AttendanceStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceRecordResponse {
    private Long id;
    private Long enrollmentId;
    private String studentCode;
    private String studentName;
    private AttendanceStatus status;
    private String note;
}
