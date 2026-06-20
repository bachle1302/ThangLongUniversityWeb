package com.example.ThangLongUniversityWeb.repository;

import com.example.ThangLongUniversityWeb.entity.KnowledgeChunk;
import com.example.ThangLongUniversityWeb.entity.KnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeChunkRepository extends JpaRepository<KnowledgeChunk, Long> {

    List<KnowledgeChunk> findByDocumentOrderByChunkIndexAsc(KnowledgeDocument document);

    void deleteByDocument(KnowledgeDocument document);

    long countByDocument(KnowledgeDocument document);

    @Query(value = """
            SELECT COUNT(*) FROM knowledge_chunks
            WHERE document_id = :docId AND search_vector IS NOT NULL
            """, nativeQuery = true)
    long countSearchableByDocumentId(@Param("docId") Long docId);

    @Modifying
    @Query(value = """
            UPDATE knowledge_chunks
            SET search_vector = to_tsvector('simple', coalesce(content, ''))
            WHERE document_id = :docId
            """, nativeQuery = true)
    void refreshSearchVectorsForDocument(@Param("docId") Long docId);

    @Query(value = """
            SELECT c.id FROM knowledge_chunks c
            INNER JOIN knowledge_documents d ON c.document_id = d.id
            WHERE d.is_active = true
              AND c.search_vector @@ plainto_tsquery('simple', :query)
            ORDER BY ts_rank(c.search_vector, plainto_tsquery('simple', :query)) *
              CASE d.priority
                WHEN 1 THEN 1.0 WHEN 2 THEN 0.9 WHEN 3 THEN 0.8 WHEN 4 THEN 0.7 WHEN 5 THEN 0.5
                ELSE 0.7 END DESC
            LIMIT :topK
            """, nativeQuery = true)
    List<Long> searchChunkIdsByFullText(@Param("query") String query, @Param("topK") int topK);

    @Query("SELECT c FROM KnowledgeChunk c JOIN FETCH c.document d WHERE c.id IN :ids")
    List<KnowledgeChunk> findAllByIdInWithDocument(@Param("ids") List<Long> ids);

    @Query("SELECT c FROM KnowledgeChunk c JOIN FETCH c.document d WHERE d.isActive = true")
    List<KnowledgeChunk> findAllActiveWithDocument();
}
