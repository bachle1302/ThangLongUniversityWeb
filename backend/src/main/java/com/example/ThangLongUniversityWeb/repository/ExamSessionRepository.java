package com.example.ThangLongUniversityWeb.repository;

import com.example.ThangLongUniversityWeb.entity.ExamSession;
import com.example.ThangLongUniversityWeb.enums.ExamType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamSessionRepository extends JpaRepository<ExamSession, Long> {
    List<ExamSession> findBySemesterIdOrderByExamAtAsc(Long semesterId);

    Optional<ExamSession> findBySemesterIdAndCourseIdAndExamType(Long semesterId, Long courseId, ExamType examType);

    Optional<ExamSession> findBySemesterIdAndCourseIdAndExamTypeAndCandidateSelection(Long semesterId, Long courseId, ExamType examType, String candidateSelection);

    List<ExamSession> findBySemesterIdAndExamAt(Long semesterId, java.time.LocalDateTime examAt);
}
