package com.example.ThangLongUniversityWeb.repository;

import com.example.ThangLongUniversityWeb.entity.ExamSeatAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamSeatAssignmentRepository extends JpaRepository<ExamSeatAssignment, Long> {
    @Query("select s from ExamSeatAssignment s where s.examSession.id = :examSessionId order by s.roomAssignment.room.name asc, s.student.studentCode asc")
    List<ExamSeatAssignment> findByExamSessionIdOrderByRoomAssignmentRoomNameAscStudentStudentCodeAsc(Long examSessionId);

    List<ExamSeatAssignment> findByStudentIdAndExamSessionSemesterIdOrderByExamSessionExamAtAsc(Long studentId, Long semesterId);

    void deleteByExamSessionId(Long examSessionId);

    int countByRoomAssignmentId(Long roomAssignmentId);
}
