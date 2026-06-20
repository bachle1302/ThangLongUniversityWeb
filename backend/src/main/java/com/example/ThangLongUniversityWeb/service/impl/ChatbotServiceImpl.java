package com.example.ThangLongUniversityWeb.service.impl;

import com.example.ThangLongUniversityWeb.dto.response.ChatbotMessageResponse;
import com.example.ThangLongUniversityWeb.dto.response.ChatbotResponse;
import com.example.ThangLongUniversityWeb.entity.ChatbotMessage;
import com.example.ThangLongUniversityWeb.entity.KnowledgeChunk;
import com.example.ThangLongUniversityWeb.entity.User;
import com.example.ThangLongUniversityWeb.enums.ChatbotRole;
import com.example.ThangLongUniversityWeb.repository.ChatbotMessageRepository;
import com.example.ThangLongUniversityWeb.repository.UserRepository;
import com.example.ThangLongUniversityWeb.service.ChatbotService;
import com.example.ThangLongUniversityWeb.service.GroqService;
import com.example.ThangLongUniversityWeb.service.RetrieverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotServiceImpl implements ChatbotService {

    private static final int HISTORY_WINDOW = 20;
    private static final int RAG_TOP_K = 5;

    private final ChatbotMessageRepository chatbotMessageRepository;
    private final UserRepository userRepository;
    private final GroqService groqService;
    private final RetrieverService retrieverService;

    @Override
    @Transactional
    public ChatbotResponse sendMessage(String message, String sessionId) {
        User user = getCurrentUser();

        String resolvedSessionId = (sessionId != null && !sessionId.isBlank())
                ? sessionId
                : UUID.randomUUID().toString();

        // Save user message
        chatbotMessageRepository.save(ChatbotMessage.builder()
                .user(user)
                .role(ChatbotRole.USER)
                .content(message)
                .sessionId(resolvedSessionId)
                .build());

        // Load recent history for context (newest-first, then reverse)
        List<ChatbotMessage> recent = chatbotMessageRepository
                .findByUserAndSessionIdOrderByCreatedAtDesc(user, resolvedSessionId, PageRequest.of(0, HISTORY_WINDOW));
        // Remove the just-saved user message (last added = first in desc list)
        List<ChatbotMessage> history = new ArrayList<>(recent.subList(1, recent.size()));
        history.sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));

        // RAG: retrieve relevant knowledge chunks
        String retrievedContext = null;
        try {
            List<KnowledgeChunk> chunks = retrieverService.retrieve(message, RAG_TOP_K);
            if (!chunks.isEmpty()) {
                retrievedContext = retrieverService.buildContext(chunks);
                log.debug("RAG retrieved {} chunks for message", chunks.size());
            }
        } catch (Exception e) {
            log.warn("RAG retrieval failed, falling back to base prompt: {}", e.getMessage());
        }

        // Call Groq with optional RAG context
        String answer = groqService.chatWithContext(history, message, retrievedContext);

        // Save assistant response
        chatbotMessageRepository.save(ChatbotMessage.builder()
                .user(user)
                .role(ChatbotRole.ASSISTANT)
                .content(answer)
                .sessionId(resolvedSessionId)
                .build());

        return ChatbotResponse.builder()
                .answer(answer)
                .sessionId(resolvedSessionId)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public List<ChatbotMessageResponse> getHistory(String sessionId) {
        User user = getCurrentUser();
        String sid = (sessionId != null && !sessionId.isBlank()) ? sessionId : "";
        List<ChatbotMessage> messages = chatbotMessageRepository
                .findByUserAndSessionIdOrderByCreatedAtAsc(user, sid);
        return messages.stream().map(m -> ChatbotMessageResponse.builder()
                .id(m.getId())
                .role(m.getRole())
                .content(m.getContent())
                .createdAt(m.getCreatedAt())
                .build()).toList();
    }

    @Override
    @Transactional
    public void clearHistory(String sessionId) {
        User user = getCurrentUser();
        String sid = (sessionId != null && !sessionId.isBlank()) ? sessionId : "";
        chatbotMessageRepository.deleteByUserAndSessionId(user, sid);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }
}
