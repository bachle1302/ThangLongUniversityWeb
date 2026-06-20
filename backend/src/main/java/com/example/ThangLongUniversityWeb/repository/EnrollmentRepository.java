package com.example.ThangLongUniversityWeb.repository;

import com.example.ThangLongUniversityWeb.entity.ClassSection;
import com.example.ThangLongUniversityWeb.entity.Enrollment;
import com.example.ThangLongUniversityWeb.enums.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByClassSectionId(Long classSectionId);

    List<Enrollment> findByStudentIdAndClassSection_SemesterId(Long studentId, Long semesterId);

    List<Enrollment> findByStudentIdAndClassSection_SemesterIdAndStatus(Long studentId, Long semesterId, EnrollmentStatus status);

    List<Enrollment> findByStudentIdAndClassSection_SemesterIdAndStatusIn(Long studentId, Long semesterId, List<EnrollmentStatus> statuses);

    List<Enrollment> findByClassSectionSemesterId(Long semesterId);

    List<Enrollment> findByClassSectionSemesterIdAndStatus(Long semesterId, EnrollmentStatus status);

    List<Enrollment> findByClassSectionRegistrationRoundId(Long registrationRoundId);

    List<Enrollment> findByClassSectionRegistrationRoundIdAndStatus(Long registrationRoundId, EnrollmentStatus status);

    long countByClassSectionIdAndStatusIn(Long classSectionId, List<EnrollmentStatus> statuses);

    long countByStatusIn(List<EnrollmentStatus> statuses);

    long countByClassSectionSemesterIdAndStatus(Long semesterId, EnrollmentStatus status);

    List<Enrollment> findByStudentId(Long studentId);

    long deleteByStudentId(Long studentId);

    @Query("SELECT e FROM Enrollment e " +
            "WHERE e.student.id = :studentId AND e.classSection.course.id = :courseId " +
            "ORDER BY e.id DESC")
    List<Enrollment> findByStudentIdAndCourseIdOrderByIdDesc(@Param("studentId") Long studentId,
                                                            @Param("courseId") Long courseId);

    boolean existsByStudentIdAndClassSectionId(Long studentId, Long classSectionId);

    Optional<Enrollment> findByStudentIdAndClassSectionId(Long studentId, Long classSectionId);

    @Query("SELECT e.classSection.course.id FROM Enrollment e " +
            "WHERE e.student.id = :studentId AND e.status IN ('PASSED', 'REGISTERED')")
    List<Long> findEnrolledOrPassedCourseIdsByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT DISTINCT e.classSection.course.id FROM Enrollment e " +
            "WHERE e.student.id = :studentId AND e.courseStatus = 'PASSED'")
    List<Long> findPassedCourseIdsByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT e.classSection FROM Enrollment e " +
            "WHERE e.student.id = :studentId AND e.classSection.semester.id = :semesterId AND e.status = 'REGISTERED'")
    List<ClassSection> findCurrentRegisteredClasses(@Param("studentId") Long studentId, @Param("semesterId") Long semesterId);

    @Query("SELECT e.classSection FROM Enrollment e " +
            "WHERE e.student.id = :studentId AND e.classSection.semester.id = :semesterId AND e.status IN ('PENDING', 'REGISTERED')")
    List<ClassSection> findCurrentSelectedOrRegisteredClasses(@Param("studentId") Long studentId, @Param("semesterId") Long semesterId);

    @Query("""
            SELECT e FROM Enrollment e
            WHERE (:semesterId IS NULL OR e.classSection.semester.id = :semesterId)
              AND (:classSectionId IS NULL OR e.classSection.id = :classSectionId)
              AND (:status IS NULL OR e.status = :status)
            """)
    @EntityGraph(attributePaths = {
            "student",
            "student.homeroom",
            "student.major",
            "classSection",
            "classSection.course",
            "classSection.semester",
            "classSection.registrationRound"
    })
    Page<Enrollment> searchAdmin(
            @Param("semesterId") Long semesterId,
            @Param("classSectionId") Long classSectionId,
            @Param("status") EnrollmentStatus status,
            Pageable pageable
    );
}
