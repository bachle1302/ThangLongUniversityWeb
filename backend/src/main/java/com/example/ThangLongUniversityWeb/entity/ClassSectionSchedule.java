package com.example.ThangLongUniversityWeb.entity;

import com.example.ThangLongUniversityWeb.entity.Room;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "class_section_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassSectionSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "class_section_id", nullable = false)
    private ClassSection classSection;

    @Column(nullable = false)
    private Integer dayOfWeek; // 2-8 (Monday to Sunday)

    @ManyToOne
    @JoinColumn(name = "start_period_id", nullable = false)
    private Period startPeriod;

    @ManyToOne
    @JoinColumn(name = "end_period_id", nullable = false)
    private Period endPeriod;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = true)
    private Room room;
}
