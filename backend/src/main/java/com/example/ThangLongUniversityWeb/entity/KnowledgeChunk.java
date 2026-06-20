package com.example.ThangLongUniversityWeb.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "knowledge_chunks", indexes = {
        @Index(name = "idx_kchunk_document_id", columnList = "document_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private KnowledgeDocument document;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * Embedding stored as JSON array string: "[0.1, -0.2, ...]"
     * Dimension matches the configured embedding model (default 768 for multilingual-mpnet-base-v2).
     */
    @Column(columnDefinition = "TEXT")
    private String embedding;

    /** Optional JSON metadata: {"ngành":"CNTT","loại":"học_phí"} */
    @Column(columnDefinition = "TEXT")
    private String metadata;

    /** PostgreSQL full-text search vector; populated via native SQL after save. */
    @Column(name = "search_vector", columnDefinition = "tsvector", insertable = false, updatable = false)
    private String searchVector;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
