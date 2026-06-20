package com.example.ThangLongUniversityWeb.entity;

import com.example.ThangLongUniversityWeb.enums.ClassSectionStatus;
import com.example.ThangLongUniversityWeb.enums.ExamType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "class_sections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassSection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String classCode;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne
    @JoinColumn(name = "semester_id")
    private Semester semester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_round_id")
    private RegistrationRound registrationRound;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    private Integer dayOfWeek; // 2-8 (Monday to Sunday)

    @ManyToOne
    @JoinColumn(name = "start_period_id")
    private Period startPeriod;

    @ManyToOne
    @JoinColumn(name = "end_period_id")
    private Period endPeriod;

    @OneToMany(mappedBy = "classSection", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ClassSectionSchedule> schedules = new ArrayList<>();

    private Integer maxSlots;
    private Integer currentSlots = 0; // Default 0
    @Enumerated(EnumType.STRING)
    @Column
    private ClassSectionStatus status = ClassSectionStatus.DRAFT;
    private boolean gradeLocked = false; // Khóa điểm sau khi nhập xong

    private LocalDateTime examAt;
    private String examRoom;

    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type")
    private ExamType examType = ExamType.NORMAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_exam_session_id")
    private ExamSession sourceExamSession;

    public boolean isClosed() {
        return status == ClassSectionStatus.CLOSED || status == ClassSectionStatus.CANCELLED;
    }

    public void setClosed(boolean closed) {
        this.status = closed ? ClassSectionStatus.CLOSED : ClassSectionStatus.OPEN;
    }

    // Method to check if this class section overlaps with another class section
    public boolean isOverlapping(ClassSection other) {
        if (other == null) return false;

        // Check overlap between all schedules of this class and all schedules of other class
        for (ClassSectionSchedule thisSchedule : this.schedules) {
            for (ClassSectionSchedule otherSchedule : other.getSchedules()) {
                if (thisSchedule.getDayOfWeek().equals(otherSchedule.getDayOfWeek())) {
                    // Same day, check if time periods overlap
                    if (thisSchedule.getStartPeriod().getPeriodNumber() <= otherSchedule.getEndPeriod().getPeriodNumber() &&
                        thisSchedule.getEndPeriod().getPeriodNumber() >= otherSchedule.getStartPeriod().getPeriodNumber()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
