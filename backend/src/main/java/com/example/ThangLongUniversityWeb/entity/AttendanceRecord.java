package com.example.ThangLongUniversityWeb.entity;

import com.example.ThangLongUniversityWeb.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "attendance_records",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_attendance_record_session_enrollment",
        columnNames = {"attendance_session_id", "enrollment_id"}
    )
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_session_id", nullable = false)
    private AttendanceSession attendanceSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    private String note;
}
