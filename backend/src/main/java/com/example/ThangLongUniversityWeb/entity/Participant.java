package com.example.ThangLongUniversityWeb.entity;

import com.example.ThangLongUniversityWeb.CompositeKey.ParticipantId;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "participants")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Participant {
    @EmbeddedId
    private ParticipantId id = new ParticipantId();

    @ManyToOne
    @MapsId("conversationId")
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime joinedAt = LocalDateTime.now();
}
