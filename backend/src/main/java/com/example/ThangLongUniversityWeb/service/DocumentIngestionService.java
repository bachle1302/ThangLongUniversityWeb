package com.example.ThangLongUniversityWeb.service;

import java.util.List;

public interface DocumentIngestionService {

    /**
     * Ingest raw text: chunk → embed → persist.
     *
     * @param title      Document title shown in admin UI
     * @param content    Full raw text content
     * @param sourceUrl  Origin URL (nullable)
     * @param sourceType WEBSITE | HANDBOOK | ANNOUNCEMENT | FAQ | WIKIPEDIA
     * @param priority   1=highest → 5=lowest
     * @return ID of the created KnowledgeDocument
     */
    Long ingestText(String title, String content, String sourceUrl, String sourceType, int priority);

    /**
     * Crawl a URL with Jsoup, extract main text, then ingest.
     *
     * @return ID of the created KnowledgeDocument
     */
    Long ingestUrl(String url, String title, String sourceType, int priority);

    /** Delete document and all its chunks. */
    void deleteDocument(Long documentId);

    /** Re-chunk and re-embed all chunks for a document. */
    void reindexDocument(Long documentId);

    /** Re-index every active document. */
    void reindexAll();

    /** List of all document IDs currently active. */
    List<Long> listActiveDocumentIds();
}
