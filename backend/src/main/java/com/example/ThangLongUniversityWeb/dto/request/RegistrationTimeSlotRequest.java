package com.example.ThangLongUniversityWeb.dto.request;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RegistrationTimeSlotRequest {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<Long> allowedMajorIds;
    private List<String> allowedCohorts;
}
