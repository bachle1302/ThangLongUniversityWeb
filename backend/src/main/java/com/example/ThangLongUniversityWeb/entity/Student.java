package com.example.ThangLongUniversityWeb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "students")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonIgnore
    private User user;

    @Column(name = "student_code", unique = true, nullable = false, length = 30)
    private String studentCode;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(name = "date_of_birth")
    private LocalDate dob;

    @Column(length = 20)
    private String gender;

    @Column(length = 20)
    private String phone;

    @Column(name = "national_id", length = 30)
    private String nationalId;

    @Column(name = "place_of_birth", length = 150)
    private String placeOfBirth;

    @Column(length = 255)
    private String hometown;

    @Column(name = "permanent_address", length = 500)
    private String permanentAddress;

    @Column(name = "current_address", length = 500)
    private String currentAddress;

    @Column(name = "emergency_contact", length = 255)
    private String emergencyContact;

    @Column(length = 500)
    private String address;

    @Column(length = 30)
    private String cohort;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "homeroom_id")
    private Homeroom homeroom;

    @Column(length = 50)
    private String status;

    @Column(name = "training_type", length = 120)
    private String trainingType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id")
    private Major major;

    @Column(name = "academic_year")
    private Integer academicYear;
}
