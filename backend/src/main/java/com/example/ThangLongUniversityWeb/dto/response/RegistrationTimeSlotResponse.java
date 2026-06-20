package com.example.ThangLongUniversityWeb.dto.response;

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
public class RegistrationTimeSlotResponse {
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<Long> allowedMajorIds;
    private List<String> allowedCohorts;
}
