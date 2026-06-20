package com.example.ThangLongUniversityWeb.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentSemesterResponse {
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean registrationOpen;
    private boolean locked;
    private boolean examPublished;
    private boolean retakeOpen;
    private boolean retakeLocked;
    private boolean ended;
    private Long activeRegistrationRoundId;
    private String activeRegistrationRoundName;
    private Integer activeRegistrationRoundNumber;
}
