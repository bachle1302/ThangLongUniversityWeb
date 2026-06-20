package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.entity.KnowledgeChunk;
import com.example.ThangLongUniversityWeb.repository.KnowledgeChunkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Retrieves the most relevant knowledge chunks for a given query using
 * PostgreSQL full-text search with keyword fallback.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RetrieverService {

    private static final double[] PRIORITY_WEIGHTS = {0, 1.0, 0.9, 0.8, 0.7, 0.5};
    private static final Pattern NON_WORD = Pattern.compile("[^\\p{L}\\p{N}\\s]", Pattern.UNICODE_CHARACTER_CLASS);

    private final KnowledgeChunkRepository chunkRepository;

    /**
     * Retrieve top-K most relevant chunks for the query.
     */
    public List<KnowledgeChunk> retrieve(String query, int topK) {
        if (query == null || query.isBlank()) return List.of();

        String sanitized = sanitizeForTsQuery(query);
        if (!sanitized.isBlank()) {
            try {
                List<Long> ids = chunkRepository.searchChunkIdsByFullText(sanitized, topK);
                if (!ids.isEmpty()) {
                    return orderChunksByIds(ids, chunkRepository.findAllByIdInWithDocument(ids));
                }
            } catch (Exception e) {
                log.warn("Full-text search failed, falling back to keywords: {}", e.getMessage());
            }
        }

        return keywordFallback(query, topK);
    }

    /**
     * Build a context string from retrieved chunks, formatted for the system prompt.
     */
    public String buildContext(List<KnowledgeChunk> chunks) {
        if (chunks.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (KnowledgeChunk c : chunks) {
            sb.append("Nguồn: ").append(c.getDocument().getTitle()).append("\n");
            sb.append(c.getContent()).append("\n---\n");
        }
        return sb.toString().strip();
    }

    private List<KnowledgeChunk> keywordFallback(String query, int topK) {
        List<String> keywords = extractKeywords(query);
        if (keywords.isEmpty()) return List.of();

        String lowerQuery = query.toLowerCase(Locale.ROOT);
        List<KnowledgeChunk> allChunks = chunkRepository.findAllActiveWithDocument();
        List<ScoredChunk> scored = new ArrayList<>();

        for (KnowledgeChunk chunk : allChunks) {
            String content = chunk.getContent().toLowerCase(Locale.ROOT);
            int matches = 0;
            for (String kw : keywords) {
                if (content.contains(kw)) matches++;
            }
            if (matches == 0) continue;

            int priority = chunk.getDocument().getPriority();
            double weight = (priority >= 1 && priority <= 5) ? PRIORITY_WEIGHTS[priority] : 0.7;
            double score = matches * weight;
            if (lowerQuery.length() >= 5 && content.contains(lowerQuery)) {
                score += 2.0 * weight;
            }
            scored.add(new ScoredChunk(chunk, score));
        }

        scored.sort(Comparator.comparingDouble(ScoredChunk::score).reversed());
        return scored.stream()
                .limit(topK)
                .map(ScoredChunk::chunk)
                .toList();
    }

    private static List<KnowledgeChunk> orderChunksByIds(List<Long> ids, List<KnowledgeChunk> chunks) {
        Map<Long, KnowledgeChunk> byId = new LinkedHashMap<>();
        for (KnowledgeChunk c : chunks) {
            byId.put(c.getId(), c);
        }
        List<KnowledgeChunk> ordered = new ArrayList<>();
        for (Long id : ids) {
            KnowledgeChunk c = byId.get(id);
            if (c != null) ordered.add(c);
        }
        return ordered;
    }

    private static String sanitizeForTsQuery(String query) {
        return NON_WORD.matcher(query).replaceAll(" ").replaceAll("\\s+", " ").trim();
    }

    private static List<String> extractKeywords(String query) {
        String cleaned = sanitizeForTsQuery(query).toLowerCase(Locale.ROOT);
        if (cleaned.isBlank()) return List.of();

        List<String> keywords = new ArrayList<>();
        for (String word : cleaned.split("\\s+")) {
            if (word.length() >= 3 && !keywords.contains(word)) {
                keywords.add(word);
            }
            if (keywords.size() >= 8) break;
        }
        return keywords;
    }

    private record ScoredChunk(KnowledgeChunk chunk, double score) {}
}
