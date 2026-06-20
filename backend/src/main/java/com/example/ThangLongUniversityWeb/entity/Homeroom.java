package com.example.ThangLongUniversityWeb.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "homerooms",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_homerooms_class_name", columnNames = {"class_name"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Homeroom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_name", nullable = false, unique = true, length = 80)
    private String className;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advisor_id")
    private Teacher advisor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id")
    private Major major;

    @Column(name = "academic_year")
    private Integer academicYear;

    @Column(length = 30)
    private String cohort;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
