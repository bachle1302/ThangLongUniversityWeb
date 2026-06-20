package com.example.ThangLongUniversityWeb.entity;

import com.example.ThangLongUniversityWeb.enums.ConversationType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversations")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private ConversationType type;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
