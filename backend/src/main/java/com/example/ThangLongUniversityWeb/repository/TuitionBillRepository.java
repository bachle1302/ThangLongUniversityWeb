package com.example.ThangLongUniversityWeb.repository;

import com.example.ThangLongUniversityWeb.entity.TuitionBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TuitionBillRepository extends JpaRepository<TuitionBill, Long> {
    // Tìm hóa đơn của 1 sinh viên trong 1 học kỳ cụ thể
    Optional<TuitionBill> findByStudentIdAndSemesterId(Long studentId, Long semesterId);

    boolean existsByStudentId(Long studentId);
}