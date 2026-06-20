package com.example.ThangLongUniversityWeb.repository;

import com.example.ThangLongUniversityWeb.entity.Period;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PeriodRepository extends JpaRepository<Period, Long> {
    Optional<Period> findByPeriodNumber(Integer periodNumber);
}