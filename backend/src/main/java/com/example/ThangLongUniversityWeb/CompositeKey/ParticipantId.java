package com.example.ThangLongUniversityWeb.CompositeKey;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ParticipantId implements Serializable {
    private Long conversationId;
    private Long userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParticipantId that = (ParticipantId) o;
        return Objects.equals(conversationId, that.conversationId) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conversationId, userId);
    }
}