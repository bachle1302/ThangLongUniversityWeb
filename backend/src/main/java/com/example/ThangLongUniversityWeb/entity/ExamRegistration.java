package com.example.ThangLongUniversityWeb.entity;

import com.example.ThangLongUniversityWeb.enums.EnrollmentStatus;
import com.example.ThangLongUniversityWeb.enums.EnrollmentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "exam_registrations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_exam_reg_student_semester_course",
                columnNames = {"student_id", "semester_id", "course_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id")
    private Semester semester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_section_id")
    private ClassSection classSection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_grade_id", nullable = false)
    private Grade originalGrade;

    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status = EnrollmentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private EnrollmentType registrationType; // RETAKE or IMPROVE

    private Long feeCharged;

    private Integer attemptNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_round_id")
    private RegistrationRound registrationRound;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
