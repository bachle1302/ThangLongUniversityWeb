package com.example.ThangLongUniversityWeb.repository;

import com.example.ThangLongUniversityWeb.entity.Grade;
import com.example.ThangLongUniversityWeb.enums.EnrollmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    
    /**
     * Tìm Grade bằng Enrollment ID
     */
    Optional<Grade> findByEnrollmentId(Long enrollmentId);

    long countByRetestScoreIsNotNull();

    /**
     * Lấy danh sách Grade của sinh viên theo Semester
     */
    @Query(value = "SELECT g FROM Grade g " +
           "JOIN g.enrollment e " +
           "WHERE e.student.id = :studentId " +
           "AND e.classSection.semester.id = :semesterId")
    List<Grade> findByStudentIdAndSemesterId(@Param("studentId") Long studentId, 
                                             @Param("semesterId") Long semesterId);

    /**
     * Lấy danh sách Grade của sinh viên tất cả kỳ
     */
    @Query(value = "SELECT g FROM Grade g " +
           "JOIN g.enrollment e " +
           "WHERE e.student.id = :studentId " +
           "ORDER BY e.classSection.semester.id DESC")
    List<Grade> findByStudentId(@Param("studentId") Long studentId);

    /**
     * Lấy danh sách Grade của lớp (ClassSection)
     */
    @Query(value = "SELECT g FROM Grade g " +
           "JOIN g.enrollment e " +
           "WHERE e.classSection.id = :classSectionId")
    List<Grade> findByClassSectionId(@Param("classSectionId") Long classSectionId);

    @Query("SELECT g FROM Grade g " +
           "JOIN FETCH g.enrollment e " +
           "JOIN FETCH e.classSection cs " +
           "JOIN FETCH cs.course c " +
           "JOIN FETCH cs.semester s " +
           "WHERE e.student.id = :studentId " +
           "AND g.totalScore IS NOT NULL " +
           "ORDER BY c.id ASC, COALESCE(g.attemptNumber, 1) DESC, g.id DESC")
    List<Grade> findCompletedGradesByStudentIdForRetake(@Param("studentId") Long studentId);

    @Query("SELECT g FROM Grade g " +
           "JOIN FETCH g.enrollment e " +
           "JOIN FETCH e.classSection cs " +
           "JOIN FETCH cs.course c " +
           "JOIN FETCH cs.semester s " +
           "WHERE e.student.id = :studentId " +
           "AND (:semesterId IS NULL OR cs.semester.id = :semesterId) " +
           "AND g.enrollmentType IN :types " +
           "ORDER BY e.id DESC")
    List<Grade> findRetakeRequests(
            @Param("studentId") Long studentId,
            @Param("semesterId") Long semesterId,
            @Param("types") List<EnrollmentType> types);
}
