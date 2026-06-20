package com.example.ThangLongUniversityWeb.entity;

import com.example.ThangLongUniversityWeb.enums.CourseStudyStatus;
import com.example.ThangLongUniversityWeb.enums.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "enrollments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_enrollments_student_class",
                columnNames = {"student_id", "class_section_id"}
        )
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "class_section_id")
    private ClassSection classSection;

    /** Trạng thái đăng ký học phần (luồng đăng ký, hủy đăng ký) */
    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status;

    /** Trạng thái kết quả học môn (điểm danh + điểm số) */
    @Enumerated(EnumType.STRING)
    @Column(name = "course_status")
    private CourseStudyStatus courseStatus = CourseStudyStatus.IN_PROGRESS;

    @Column(name = "enrolled_at")
    private LocalDateTime enrolledAt;

    /**
     * Grade là nguồn sự thật duy nhất cho điểm số.
     * TASK-011: Các field midTermScore/finalScore/totalScore đã bị xóa khỏi Enrollment.
     */
    @OneToOne(mappedBy = "enrollment", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Grade grade;

    @PrePersist
    protected void onCreate() {
        if (enrolledAt == null) {
            enrolledAt = LocalDateTime.now();
        }
    }
}

