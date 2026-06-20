package com.example.ThangLongUniversityWeb.repository;

import com.example.ThangLongUniversityWeb.entity.ExamRegistration;
import com.example.ThangLongUniversityWeb.enums.EnrollmentStatus;
import com.example.ThangLongUniversityWeb.enums.EnrollmentType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamRegistrationRepository extends JpaRepository<ExamRegistration, Long> {

    @Query("SELECT e FROM ExamRegistration e WHERE e.student.id = :studentId AND e.semester.id = :semesterId AND e.registrationType IN :types")
    List<ExamRegistration> findRetakeRequests(
            @Param("studentId") Long studentId,
            @Param("semesterId") Long semesterId,
            @Param("types") List<EnrollmentType> types
    );
    
    Optional<ExamRegistration> findByStudentIdAndClassSectionId(Long studentId, Long classSectionId);

    Optional<ExamRegistration> findByStudentIdAndCourseIdAndSemesterId(Long studentId, Long courseId, Long semesterId);

    List<ExamRegistration> findByClassSectionId(Long classSectionId);

    List<ExamRegistration> findByClassSectionIdAndStatus(Long classSectionId, EnrollmentStatus status);

    @EntityGraph(attributePaths = {"student", "classSection", "course", "semester"})
    List<ExamRegistration> findBySemesterId(Long semesterId);

    @EntityGraph(attributePaths = {"student", "classSection", "course", "semester"})
    List<ExamRegistration> findBySemesterIdAndStatus(Long semesterId, EnrollmentStatus status);

    List<ExamRegistration> findBySemesterIdAndCourseIdAndStatus(Long semesterId, Long courseId, EnrollmentStatus status);

    @EntityGraph(attributePaths = {
            "semester", "course", "originalGrade", "originalGrade.enrollment",
            "originalGrade.enrollment.classSection", "originalGrade.enrollment.classSection.semester",
            "originalGrade.enrollment.classSection.course", "originalGrade.enrollment.student"
    })
    List<ExamRegistration> findByStudentIdAndSemesterIdAndStatus(Long studentId, Long semesterId, EnrollmentStatus status);

    @EntityGraph(attributePaths = {
            "semester", "course", "originalGrade", "originalGrade.enrollment",
            "originalGrade.enrollment.classSection", "originalGrade.enrollment.classSection.semester",
            "originalGrade.enrollment.classSection.course", "originalGrade.enrollment.student"
    })
    List<ExamRegistration> findByStudentIdAndStatus(Long studentId, EnrollmentStatus status);

    List<ExamRegistration> findByOriginalGrade_Enrollment_Id(Long enrollmentId);

    List<ExamRegistration> findByRegistrationRoundIdAndStatus(Long registrationRoundId, EnrollmentStatus status);

    List<ExamRegistration> findByRegistrationRoundId(Long registrationRoundId);

    long countByStudentIdAndCourseIdAndRegistrationTypeAndStatusIn(
            Long studentId, Long courseId, EnrollmentType registrationType, List<EnrollmentStatus> statuses);

    long countByStudentIdAndCourseIdAndRegistrationType(
            Long studentId, Long courseId, EnrollmentType registrationType);

    @Query("""
            SELECT COUNT(e) FROM ExamRegistration e
            WHERE e.student.id = :studentId
              AND e.course.id = :courseId
              AND e.registrationType = :registrationType
              AND e.semester.id <> :semesterId
              AND e.status IN :statuses
            """)
    long countByStudentIdAndCourseIdAndRegistrationTypeAndSemesterIdNotAndStatusIn(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId,
            @Param("registrationType") EnrollmentType registrationType,
            @Param("semesterId") Long semesterId,
            @Param("statuses") List<EnrollmentStatus> statuses);

    Optional<ExamRegistration> findByStudentIdAndCourseIdAndSemesterIdAndStatusIn(
            Long studentId, Long courseId, Long semesterId, List<EnrollmentStatus> statuses);

    @Query("SELECT COUNT(e) FROM ExamRegistration e WHERE e.classSection.id = :classSectionId AND e.status = :status")
    long countByClassSectionIdAndStatus(@Param("classSectionId") Long classSectionId, @Param("status") EnrollmentStatus status);

    @Query("""
            SELECT e FROM ExamRegistration e
            WHERE e.originalGrade.enrollment.id = :enrollmentId
              AND e.status = :status
            ORDER BY e.id DESC
            """)
    List<ExamRegistration> findActiveByOriginalEnrollmentId(
            @Param("enrollmentId") Long enrollmentId,
            @Param("status") EnrollmentStatus status);
}
