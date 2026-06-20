package com.example.ThangLongUniversityWeb.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "departments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_departments_code", columnNames = {"department_code"}),
                @UniqueConstraint(name = "uk_departments_name", columnNames = {"name"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "department_code", nullable = false, unique = true, length = 30)
    private String departmentCode;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(length = 500)
    private String description;
}
