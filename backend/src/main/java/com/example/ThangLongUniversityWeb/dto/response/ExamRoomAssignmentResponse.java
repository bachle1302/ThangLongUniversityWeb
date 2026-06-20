package com.example.ThangLongUniversityWeb.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamRoomAssignmentResponse {
    private Long id;
    private Long roomId;
    private String roomName;
    private Integer capacity;
    private Integer assignedCount;
    private Long proctorId;
    private String proctorCode;
    private String proctorName;
}
