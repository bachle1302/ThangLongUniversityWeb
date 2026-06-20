package com.example.ThangLongUniversityWeb.entity;

import com.example.ThangLongUniversityWeb.enums.ExamType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "exam_sessions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_exam_session_semester_course_type",
                columnNames = {"semester_id", "course_id", "exam_type", "candidate_selection"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", nullable = false)
    private ExamType examType = ExamType.NORMAL;

    @Column(name = "candidate_selection", nullable = false)
    private String candidateSelection = "ALL"; // "ALL", "NORMAL_ONLY", "RETAKE_ONLY"

    @Column(nullable = false)
    private LocalDateTime examAt;

    @OneToMany(mappedBy = "examSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExamRoomAssignment> roomAssignments = new ArrayList<>();
}
