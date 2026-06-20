package com.example.ThangLongUniversityWeb.repository;

import com.example.ThangLongUniversityWeb.entity.Homeroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HomeroomRepository extends JpaRepository<Homeroom, Long> {
    Optional<Homeroom> findByClassName(String className);
    boolean existsByClassName(String className);
    List<Homeroom> findByMajorId(Long majorId);
    List<Homeroom> findByAdvisorId(Long advisorId);
    List<Homeroom> findByAcademicYear(Integer academicYear);
    List<Homeroom> findByIsActiveTrue();
}
