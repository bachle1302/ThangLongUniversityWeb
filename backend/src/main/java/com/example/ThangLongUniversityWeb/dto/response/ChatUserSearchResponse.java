package com.example.ThangLongUniversityWeb.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatUserSearchResponse {
    private Long id;
    private String username;
    private String email;
    private String role;
    private String code;
    private String fullName;
    private String subtitle;
    private String avatarUrl;
}
