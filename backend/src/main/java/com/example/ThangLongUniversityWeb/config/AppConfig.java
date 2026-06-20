package com.example.ThangLongUniversityWeb.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Cực kỳ quan trọng: Đăng ký module này để Jackson hiểu được kiểu LocalDate, LocalDateTime
        mapper.registerModule(new JavaTimeModule());
        mapper.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        return mapper;
    }
}