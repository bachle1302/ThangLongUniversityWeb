package com.example.ThangLongUniversityWeb.dto.request;

import lombok.Data;

@Data
public class RegistrationRoundRequest {
    private String name;
    private Boolean open;
    private String roundType;
    private java.util.List<RegistrationTimeSlotRequest> timeSlots;
}
