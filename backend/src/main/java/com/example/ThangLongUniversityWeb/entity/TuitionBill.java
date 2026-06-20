package com.example.ThangLongUniversityWeb.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tuition_bills")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class TuitionBill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "semester_id")
    private Semester semester;

    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private boolean isCompleted;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
