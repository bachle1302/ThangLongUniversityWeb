package com.example.ThangLongUniversityWeb.dto.request;

import lombok.Data;

@Data
public class ChatbotRequest {
    private String message;
    private String sessionId;
}
