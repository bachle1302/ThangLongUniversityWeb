package com.example.ThangLongUniversityWeb.repository;

import com.example.ThangLongUniversityWeb.entity.Conversation;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository cho Conversation entity
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /**
     * Idempotent upsert when {@code conversations.id == chat_room.id} (see {@code fk_chat_room_conversation}).
     * Postgres: ON CONFLICT DO NOTHING, then callers re-fetch.
     */
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(
            value = "INSERT INTO conversations (id, name, type, created_at) " +
                    "VALUES (:id, :name, CAST(:type AS VARCHAR), CURRENT_TIMESTAMP) " +
                    "ON CONFLICT (id) DO NOTHING",
            nativeQuery = true
    )
    int insertIgnoringConflict(
            @org.springframework.data.repository.query.Param("id") Long id,
            @org.springframework.data.repository.query.Param("name") String name,
            @org.springframework.data.repository.query.Param("type") String type);

    /** True if insertion failed due to concurrent insert with same PK (unexpected). */
    default boolean upsertConversationRow(Long id, String name, String conversationTypeEnumName) {
        try {
            insertIgnoringConflict(id, name, conversationTypeEnumName);
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }
}
