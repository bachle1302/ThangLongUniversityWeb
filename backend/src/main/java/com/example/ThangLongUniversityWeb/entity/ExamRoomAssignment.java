package com.example.ThangLongUniversityWeb.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "exam_room_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamRoomAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_session_id", nullable = false)
    private ExamSession examSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private Integer capacity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proctor_id")
    private Teacher proctor;
}

