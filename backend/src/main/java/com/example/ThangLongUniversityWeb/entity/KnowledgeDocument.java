package com.example.ThangLongUniversityWeb.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "knowledge_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(name = "source_url", length = 1000)
    private String sourceUrl;

    /**
     * WEBSITE, HANDBOOK, ANNOUNCEMENT, FAQ, WIKIPEDIA
     */
    @Column(name = "source_type", length = 50)
    private String sourceType;

    /**
     * 1 = highest (new announcements), 5 = lowest (Wikipedia)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 3;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "fetched_at")
    @Builder.Default
    private LocalDateTime fetchedAt = LocalDateTime.now();

    /** SHA-256 of the source content — used to skip re-indexing unchanged docs */
    @Column(name = "content_hash", length = 64)
    private String contentHash;
}
