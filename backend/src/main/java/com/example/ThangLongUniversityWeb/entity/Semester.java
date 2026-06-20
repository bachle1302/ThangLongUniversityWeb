package com.example.ThangLongUniversityWeb.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "semesters")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Semester {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isRegistrationOpen;
    @Column(name = "is_locked", nullable = false)
    private boolean isLocked;

    // Lifecycle fields (Phương án A)
    @Column(name = "exam_published", nullable = false, columnDefinition = "boolean DEFAULT false NOT NULL")
    private boolean examPublished = false;

    @Column(name = "retake_open", nullable = false, columnDefinition = "boolean DEFAULT false NOT NULL")
    private boolean retakeOpen = false;

    @Column(name = "retake_locked", nullable = false, columnDefinition = "boolean DEFAULT false NOT NULL")
    private boolean retakeLocked = false;

    @Column(name = "ended", nullable = false, columnDefinition = "boolean DEFAULT false NOT NULL")
    private boolean ended = false;

    // Credit limit per semester
    @Column(name = "max_credits_per_semester", nullable = false, columnDefinition = "integer DEFAULT 20 NOT NULL")
    private int maxCreditsPerSemester = 20;
}
