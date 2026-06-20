package com.example.ThangLongUniversityWeb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "registration_time_slots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationTimeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_round_id", nullable = false)
    @JsonIgnore
    private RegistrationRound registrationRound;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "allowed_major_ids", length = 500)
    private String allowedMajorIds;

    @Column(name = "allowed_cohorts", length = 500)
    private String allowedCohorts;
}
