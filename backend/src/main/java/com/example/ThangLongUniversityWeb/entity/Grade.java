package com.example.ThangLongUniversityWeb.entity;

import com.example.ThangLongUniversityWeb.enums.EnrollmentType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "grades")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Grade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false, unique = true)
    private Enrollment enrollment;

    // Các thành phần điểm
    private Float participationScore;  // Chuyên cần (0-10)
    private Float midtermScore;        // Giữa kỳ (0-10)
    private Float finalScore;          // Cuối kỳ (0-10)
    private Float retestScore;         // Điểm thi lại / cải thiện (nếu có)

    // Thông tin lần học và loại đăng ký
    private Integer attemptNumber = 1;

    @Enumerated(EnumType.STRING)
    private EnrollmentType enrollmentType = EnrollmentType.ORDINARY;

    /** Loại thi dùng khi tính điểm (không persist). */
    @Transient
    private EnrollmentType calculationRegistrationType;

    // Điểm tổng kết
    private Float totalScore;          // Tổng điểm = (chuyên_cần * 0.1) + (giữa_kỳ * 0.3) + (cuối_kỳ * 0.6)
    private String letterGrade;        // A, B, C, D, F
    private Float gpa4;                // Điểm hệ 4

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void prepareGradeCalculation(EnrollmentType registrationType) {
        this.calculationRegistrationType = registrationType;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        calculateGrade();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculateGrade();
    }

    /**
     * Tự động tính totalScore, letterGrade, và gpa4
     */
    private void calculateGrade() {
        if (participationScore == null || midtermScore == null || (finalScore == null && retestScore == null)) {
            this.totalScore = null;
            this.letterGrade = null;
            this.gpa4 = null;
            return;
        }

        EnrollmentType examType = calculationRegistrationType != null
                ? calculationRegistrationType
                : enrollmentType;

        float effectiveFinal;
        if (retestScore != null) {
            if (examType == EnrollmentType.IMPROVE && finalScore != null) {
                effectiveFinal = Math.max(retestScore, finalScore);
            } else {
                effectiveFinal = retestScore;
            }
        } else {
            effectiveFinal = finalScore;
        }

        this.totalScore = (participationScore * 0.1f) + (midtermScore * 0.3f) + (effectiveFinal * 0.6f);

        if (totalScore >= 8.5) {
            this.letterGrade = "A";
            this.gpa4 = 4.0f;
        } else if (totalScore >= 8.0) {
            this.letterGrade = "A";
            this.gpa4 = 3.7f;
        } else if (totalScore >= 7.0) {
            this.letterGrade = "B";
            this.gpa4 = 3.0f;
        } else if (totalScore >= 6.0) {
            this.letterGrade = "C";
            this.gpa4 = 2.0f;
        } else if (totalScore >= 5.0) {
            this.letterGrade = "D";
            this.gpa4 = 1.0f;
        } else {
            this.letterGrade = "F";
            this.gpa4 = 0.0f;
        }

        if (retestScore != null && examType == EnrollmentType.RETAKE
                && ("A".equals(this.letterGrade) || "B".equals(this.letterGrade))) {
            this.letterGrade = "C";
            this.gpa4 = 2.0f;
        }
    }
}
