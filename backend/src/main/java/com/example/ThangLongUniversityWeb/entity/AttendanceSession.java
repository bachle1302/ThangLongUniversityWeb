package com.example.ThangLongUniversityWeb.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "attendance_sessions",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_attendance_session_class_number",
        columnNames = {"class_section_id", "session_number"}
    )
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class AttendanceSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_section_id", nullable = false)
    private ClassSection classSection;

    /** Số thứ tự buổi trong toàn khoá học (1, 2, 3, ...) */
    @Column(name = "session_number", nullable = false)
    private Integer sessionNumber;

    /** Tuần học (1–15) */
    @Column(name = "week_number")
    private Integer weekNumber;

    /** Buổi trong tuần (1 hoặc 2 nếu lớp có 2 buổi/tuần) */
    @Column(name = "meeting_index")
    private Integer meetingIndex;

    /** Ngày diễn ra buổi học */
    @Column(name = "session_date")
    private LocalDate sessionDate;

    /** Đã khoá điểm danh, không cho sửa */
    private boolean locked = false;

    @OneToMany(mappedBy = "attendanceSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttendanceRecord> records = new ArrayList<>();
}
