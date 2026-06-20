package com.example.ThangLongUniversityWeb.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRoundResponse {
    private Long id;
    private Long semesterId;
    private String semesterName;
    private String name;
    private Integer roundNumber;
    private boolean registrationOpen;
    private boolean locked;
    private int classSectionCount;
    private int pendingEnrollments;
    private int registeredEnrollments;
    private LocalDateTime createdAt;
    private LocalDateTime lockedAt;
    private String roundType;
    private java.util.List<RegistrationTimeSlotResponse> timeSlots;
}
