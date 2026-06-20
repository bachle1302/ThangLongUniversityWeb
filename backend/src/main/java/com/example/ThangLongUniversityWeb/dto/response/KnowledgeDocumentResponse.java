package com.example.ThangLongUniversityWeb.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeDocumentResponse {
    private Long id;
    private String title;
    private String sourceUrl;
    private String sourceType;
    private Integer priority;
    private Boolean isActive;
    private LocalDateTime fetchedAt;
    private long chunkCount;
    private long searchableChunkCount;
}
