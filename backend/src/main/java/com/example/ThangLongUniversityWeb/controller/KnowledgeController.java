package com.example.ThangLongUniversityWeb.controller;

import com.example.ThangLongUniversityWeb.dto.request.IngestTextRequest;
import com.example.ThangLongUniversityWeb.dto.request.IngestUrlRequest;
import com.example.ThangLongUniversityWeb.dto.response.KnowledgeChunkResponse;
import com.example.ThangLongUniversityWeb.dto.response.KnowledgeDocumentResponse;
import com.example.ThangLongUniversityWeb.entity.KnowledgeDocument;
import com.example.ThangLongUniversityWeb.repository.KnowledgeChunkRepository;
import com.example.ThangLongUniversityWeb.repository.KnowledgeDocumentRepository;
import com.example.ThangLongUniversityWeb.service.DocumentIngestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final DocumentIngestionService ingestionService;
    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeChunkRepository chunkRepository;

    @GetMapping("/documents")
    public ResponseEntity<List<KnowledgeDocumentResponse>> listDocuments() {
        List<KnowledgeDocument> docs = documentRepository.findByIsActiveTrueOrderByPriorityAscFetchedAtDesc();
        List<KnowledgeDocumentResponse> result = docs.stream().map(d -> KnowledgeDocumentResponse.builder()
                .id(d.getId())
                .title(d.getTitle())
                .sourceUrl(d.getSourceUrl())
                .sourceType(d.getSourceType())
                .priority(d.getPriority())
                .isActive(d.getIsActive())
                .fetchedAt(d.getFetchedAt())
                .chunkCount(chunkRepository.countByDocument(d))
                .searchableChunkCount(chunkRepository.countSearchableByDocumentId(d.getId()))
                .build()
        ).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/documents/{id}/chunks")
    public ResponseEntity<List<KnowledgeChunkResponse>> listDocumentChunks(@PathVariable Long id) {
        KnowledgeDocument doc = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + id));
        List<KnowledgeChunkResponse> chunks = chunkRepository.findByDocumentOrderByChunkIndexAsc(doc)
                .stream()
                .map(c -> KnowledgeChunkResponse.builder()
                        .id(c.getId())
                        .chunkIndex(c.getChunkIndex())
                        .content(c.getContent())
                        .createdAt(c.getCreatedAt())
                        .build())
                .toList();
        return ResponseEntity.ok(chunks);
    }

    @PostMapping("/ingest")
    public ResponseEntity<Map<String, Long>> ingestText(@Valid @RequestBody IngestTextRequest req) {
        Long id = ingestionService.ingestText(
                req.getTitle(), req.getContent(), req.getSourceUrl(),
                req.getSourceType(), req.getPriority());
        return ResponseEntity.ok(Map.of("documentId", id));
    }

    @PostMapping("/ingest-url")
    public ResponseEntity<Map<String, Long>> ingestUrl(@Valid @RequestBody IngestUrlRequest req) {
        Long id = ingestionService.ingestUrl(
                req.getUrl(), req.getTitle(), req.getSourceType(), req.getPriority());
        return ResponseEntity.ok(Map.of("documentId", id));
    }

    @DeleteMapping("/documents/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        ingestionService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/documents/{id}/reindex")
    public ResponseEntity<Void> reindexDocument(@PathVariable Long id) {
        ingestionService.reindexDocument(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reindex-all")
    public ResponseEntity<Void> reindexAll() {
        ingestionService.reindexAll();
        return ResponseEntity.ok().build();
    }
}
