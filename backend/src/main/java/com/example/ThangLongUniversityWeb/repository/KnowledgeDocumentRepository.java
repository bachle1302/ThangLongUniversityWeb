package com.example.ThangLongUniversityWeb.repository;

import com.example.ThangLongUniversityWeb.entity.KnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, Long> {

    List<KnowledgeDocument> findByIsActiveTrueOrderByPriorityAscFetchedAtDesc();

    Optional<KnowledgeDocument> findByContentHash(String contentHash);
}
