package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.dto.response.ChatbotMessageResponse;
import com.example.ThangLongUniversityWeb.dto.response.ChatbotResponse;

import java.util.List;

public interface ChatbotService {
    ChatbotResponse sendMessage(String message, String sessionId);
    List<ChatbotMessageResponse> getHistory(String sessionId);
    void clearHistory(String sessionId);
}
