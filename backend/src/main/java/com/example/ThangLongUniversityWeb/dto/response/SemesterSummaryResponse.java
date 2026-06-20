package com.example.ThangLongUniversityWeb.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemesterSummaryResponse {
    private Long semesterId;
    private String name;
    private String startDate;
    private String endDate;

    // Class section counts
    private int classSectionCount;
    private int examScheduledCount;
    private int examNotScheduledCount;

    // Enrollment counts
    private int enrollmentCount;
    private int pendingEnrollments;
    private int registeredEnrollments;

    // Retake counts
    private int retakeRegistrations;
    private int retakePending;
    private int retakeRegistered;

    // Lifecycle flags
    private boolean registrationOpen;
    private boolean locked;
    private boolean examPublished;
    private boolean retakeOpen;
    private boolean retakeLocked;
    private boolean ended;
    private int maxCreditsPerSemester;

    private Long activeRegistrationRoundId;
    private String activeRegistrationRoundName;
    private Integer activeRegistrationRoundNumber;
    private int registrationRoundCount;
}
