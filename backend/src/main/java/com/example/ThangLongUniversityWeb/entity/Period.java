package com.example.ThangLongUniversityWeb.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "periods", uniqueConstraints = @UniqueConstraint(columnNames = "period_number"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Period {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "period_number", nullable = false, unique = true)
    private Integer periodNumber;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
}