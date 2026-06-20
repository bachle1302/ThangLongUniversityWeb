package com.example.ThangLongUniversityWeb.repository;

import com.example.ThangLongUniversityWeb.entity.ExamRoomAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRoomAssignmentRepository extends JpaRepository<ExamRoomAssignment, Long> {
    @Query("select r from ExamRoomAssignment r where r.examSession.id = :examSessionId order by r.room.name asc")
    List<ExamRoomAssignment> findByExamSessionIdOrderByRoomNameAsc(Long examSessionId);

    void deleteByExamSessionId(Long examSessionId);
}
