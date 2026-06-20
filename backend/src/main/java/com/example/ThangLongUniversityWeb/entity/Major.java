package com.example.ThangLongUniversityWeb.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "majors",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_majors_code", columnNames = {"major_code"}),
                @UniqueConstraint(name = "uk_majors_name", columnNames = {"name"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Major {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "major_code", nullable = false, unique = true)
    private String majorCode;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
}
