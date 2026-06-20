package com.example.ThangLongUniversityWeb.repository;

import com.example.ThangLongUniversityWeb.entity.ChatbotMessage;
import com.example.ThangLongUniversityWeb.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatbotMessageRepository extends JpaRepository<ChatbotMessage, Long> {

    List<ChatbotMessage> findByUserAndSessionIdOrderByCreatedAtDesc(User user, String sessionId, Pageable pageable);

    List<ChatbotMessage> findByUserAndSessionIdOrderByCreatedAtAsc(User user, String sessionId);

    void deleteByUserAndSessionId(User user, String sessionId);
}
