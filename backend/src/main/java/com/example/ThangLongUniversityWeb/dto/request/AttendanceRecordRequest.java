package com.example.ThangLongUniversityWeb.dto.request;

import com.example.ThangLongUniversityWeb.enums.AttendanceStatus;
import lombok.Data;

@Data
public class AttendanceRecordRequest {
    private Long enrollmentId;
    private AttendanceStatus status;
    private String note;
}
