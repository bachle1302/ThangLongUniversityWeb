package com.example.ThangLongUniversityWeb.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "registration_rounds",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_registration_round_semester_number_type",
                columnNames = {"semester_id", "round_number", "round_type"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRound {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @Column(nullable = false)
    private String name;

    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    @Column(name = "registration_open", nullable = false)
    private boolean registrationOpen = false;

    @Column(nullable = false)
    private boolean locked = false;

    @Column(name = "round_type", nullable = false, length = 50)
    private String roundType = "COURSE";

    @OneToMany(mappedBy = "registrationRound", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RegistrationTimeSlot> timeSlots = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lockedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (roundType == null || roundType.isBlank()) {
            roundType = "COURSE";
        }
    }
}
