package com.example.ThangLongUniversityWeb.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "academic_results")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class AcademicResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id")
    private Semester semester; // null nếu là CPA tích lũy

    private Float semesterGpa; // GPA học kỳ
    private Float cumulativeGpa; // CPA tích lũy

    private Integer totalCredits; // Tổng tín chỉ tính GPA
    private Integer cumulativeCredits; // Tổng tín chỉ tích lũy

    private LocalDateTime calculatedAt;

    @PrePersist
    protected void onCreate() {
        this.calculatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.calculatedAt = LocalDateTime.now();
    }
}
