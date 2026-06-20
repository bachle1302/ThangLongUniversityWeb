package com.example.ThangLongUniversityWeb.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "exam_seat_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamSeatAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_session_id", nullable = false)
    private ExamSession examSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_assignment_id", nullable = false)
    private ExamRoomAssignment roomAssignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id")
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_registration_id")
    private ExamRegistration examRegistration;

    @Column(nullable = false)
    private String sourceType;
}
