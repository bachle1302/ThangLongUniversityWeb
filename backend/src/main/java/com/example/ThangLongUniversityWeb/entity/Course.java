package com.example.ThangLongUniversityWeb.entity;

import com.example.ThangLongUniversityWeb.enums.CourseType;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "courses")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    private String name;
    private Integer credits;
    private String description;

    @Enumerated(EnumType.STRING)
    private CourseType courseType = CourseType.REQUIRED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id")
    private Major major;

    @ManyToMany
    @JoinTable(
            name = "course_prerequisites",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "prerequisite_course_id")
    )
    private Set<Course> prerequisites = new HashSet<>();
}
