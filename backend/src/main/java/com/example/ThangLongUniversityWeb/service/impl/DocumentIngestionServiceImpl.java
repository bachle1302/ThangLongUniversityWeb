package com.example.ThangLongUniversityWeb.service.impl;

import com.example.ThangLongUniversityWeb.entity.KnowledgeChunk;
import com.example.ThangLongUniversityWeb.entity.KnowledgeDocument;
import com.example.ThangLongUniversityWeb.repository.KnowledgeChunkRepository;
import com.example.ThangLongUniversityWeb.repository.KnowledgeDocumentRepository;
import com.example.ThangLongUniversityWeb.service.DocumentIngestionService;
import com.example.ThangLongUniversityWeb.service.TextChunker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentIngestionServiceImpl implements DocumentIngestionService {

    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeChunkRepository chunkRepository;
    private final TextChunker textChunker;

    @Override
    @Transactional
    public Long ingestText(String title, String content, String sourceUrl, String sourceType, int priority) {
        String hash = sha256(content);

        // Skip if content hasn't changed
        if (documentRepository.findByContentHash(hash).isPresent()) {
            log.info("Document '{}' already indexed (same hash), skipping.", title);
            return documentRepository.findByContentHash(hash).get().getId();
        }

        KnowledgeDocument doc = KnowledgeDocument.builder()
                .title(title)
                .sourceUrl(sourceUrl)
                .sourceType(sourceType)
                .priority(priority)
                .contentHash(hash)
                .fetchedAt(LocalDateTime.now())
                .build();
        documentRepository.save(doc);

        chunkAndSave(doc, content);
        log.info("Ingested '{}' → {} chunks", title, chunkRepository.countByDocument(doc));
        return doc.getId();
    }

    @Override
    @Transactional
    public Long ingestUrl(String url, String title, String sourceType, int priority) {
        try {
            Document jsoupDoc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10_000)
                    .get();
            // Remove script/style noise
            jsoupDoc.select("script, style, nav, footer, header, .menu, .sidebar").remove();
            String text = jsoupDoc.body().text();
            return ingestText(title, text, url, sourceType, priority);
        } catch (Exception e) {
            log.error("Failed to crawl URL {}: {}", url, e.getMessage());
            throw new RuntimeException("URL crawl failed: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteDocument(Long documentId) {
        KnowledgeDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));
        chunkRepository.deleteByDocument(doc);
        documentRepository.delete(doc);
    }

    @Override
    @Transactional
    public void reindexDocument(Long documentId) {
        // We don't store the original raw text in the entity, so we re-chunk from existing chunks
        KnowledgeDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));
        List<KnowledgeChunk> existing = chunkRepository.findByDocumentOrderByChunkIndexAsc(doc);
        if (existing.isEmpty()) return;

        // Rebuild content from existing chunks (strip overlap noise best-effort)
        String combined = existing.stream()
                .map(KnowledgeChunk::getContent)
                .distinct()
                .reduce("", (a, b) -> a + "\n\n" + b);

        chunkRepository.deleteByDocument(doc);
        chunkAndSave(doc, combined);
        log.info("Re-indexed doc {} → {} chunks", documentId, chunkRepository.countByDocument(doc));
    }

    @Override
    @Transactional
    public void reindexAll() {
        List<Long> ids = listActiveDocumentIds();
        log.info("Re-indexing {} documents...", ids.size());
        for (Long id : ids) {
            try {
                reindexDocument(id);
            } catch (Exception e) {
                log.error("Failed to re-index doc {}: {}", id, e.getMessage());
            }
        }
    }

    @Override
    public List<Long> listActiveDocumentIds() {
        return documentRepository.findByIsActiveTrueOrderByPriorityAscFetchedAtDesc()
                .stream().map(KnowledgeDocument::getId).toList();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void chunkAndSave(KnowledgeDocument doc, String content) {
        List<String> texts = textChunker.chunk(content);
        if (texts.isEmpty()) return;

        List<KnowledgeChunk> chunks = new ArrayList<>();
        for (int i = 0; i < texts.size(); i++) {
            chunks.add(KnowledgeChunk.builder()
                    .document(doc)
                    .chunkIndex(i)
                    .content(texts.get(i))
                    .build());
        }
        chunkRepository.saveAll(chunks);
        chunkRepository.refreshSearchVectorsForDocument(doc.getId());
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return String.valueOf(input.hashCode());
        }
    }
}
