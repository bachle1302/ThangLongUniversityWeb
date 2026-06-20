package com.example.ThangLongUniversityWeb.repository;

import com.example.ThangLongUniversityWeb.entity.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {

    List<AttendanceSession> findByClassSectionIdOrderBySessionNumber(Long classSectionId);

    Optional<AttendanceSession> findByClassSectionIdAndSessionNumber(Long classSectionId, Integer sessionNumber);
}
