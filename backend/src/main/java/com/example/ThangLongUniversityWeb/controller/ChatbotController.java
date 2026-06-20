package com.example.ThangLongUniversityWeb.controller;

import com.example.ThangLongUniversityWeb.dto.request.ChatbotRequest;
import com.example.ThangLongUniversityWeb.dto.response.ChatbotMessageResponse;
import com.example.ThangLongUniversityWeb.dto.response.ChatbotResponse;
import com.example.ThangLongUniversityWeb.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/send")
    public ResponseEntity<ChatbotResponse> send(@RequestBody ChatbotRequest request) {
        return ResponseEntity.ok(chatbotService.sendMessage(request.getMessage(), request.getSessionId()));
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatbotMessageResponse>> getHistory(
            @RequestParam(required = false) String sessionId) {
        return ResponseEntity.ok(chatbotService.getHistory(sessionId));
    }

    @DeleteMapping("/history")
    public ResponseEntity<Void> clearHistory(
            @RequestParam(required = false) String sessionId) {
        chatbotService.clearHistory(sessionId);
        return ResponseEntity.noContent().build();
    }
}
