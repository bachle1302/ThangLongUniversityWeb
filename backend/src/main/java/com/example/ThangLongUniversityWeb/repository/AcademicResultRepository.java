package com.example.ThangLongUniversityWeb.repository;

import com.example.ThangLongUniversityWeb.entity.AcademicResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AcademicResultRepository extends JpaRepository<AcademicResult, Long> {
    
    /**
     * Tìm AcademicResult của sinh viên trong học kỳ
     */
    Optional<AcademicResult> findByStudentIdAndSemesterId(Long studentId, Long semesterId);

    /**
     * Tìm AcademicResult tích lũy của sinh viên
     */
    Optional<AcademicResult> findByStudentIdAndSemesterIdIsNull(Long studentId);

    /**
     * Lấy danh sách AcademicResult của sinh viên theo thứ tự học kỳ giảm dần
     */
    @Query(value = "SELECT ar FROM AcademicResult ar " +
           "WHERE ar.student.id = :studentId " +
           "ORDER BY ar.semester.id DESC")
    java.util.List<AcademicResult> findByStudentIdOrderBySemesterDesc(@Param("studentId") Long studentId);
}
