package com.example.ThangLongUniversityWeb.repository;

import com.example.ThangLongUniversityWeb.entity.ClassSection;
import com.example.ThangLongUniversityWeb.enums.ClassSectionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import com.example.ThangLongUniversityWeb.entity.RegistrationRound;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassSectionRepository extends JpaRepository<ClassSection, Long> {
    @EntityGraph(attributePaths = {
            "course",
            "course.major",
            "course.major.department",
            "semester",
            "registrationRound",
            "teacher",
            "teacher.user",
            "schedules",
            "schedules.room",
            "schedules.startPeriod",
            "schedules.endPeriod"
    })
    List<ClassSection> findBySemesterId(Long semesterId);

    Optional<ClassSection> findBySemesterIdAndClassCode(Long semesterId, String classCode);

    Optional<ClassSection> findBySourceExamSessionId(Long examSessionId);

    @EntityGraph(attributePaths = {
            "course",
            "course.major",
            "course.major.department",
            "semester",
            "registrationRound",
            "teacher",
            "teacher.user",
            "schedules",
            "schedules.room",
            "schedules.startPeriod",
            "schedules.endPeriod"
    })
    List<ClassSection> findByRegistrationRoundId(Long registrationRoundId);

    @EntityGraph(attributePaths = {
            "course",
            "course.major",
            "course.major.department",
            "semester",
            "registrationRound",
            "teacher",
            "teacher.user",
            "schedules",
            "schedules.room",
            "schedules.startPeriod",
            "schedules.endPeriod"
    })
    List<ClassSection> findBySemesterIdAndCourseId(Long semesterId, Long courseId);

    @EntityGraph(attributePaths = {
            "course",
            "course.major",
            "course.major.department",
            "semester",
            "registrationRound",
            "teacher",
            "teacher.user",
            "schedules",
            "schedules.room",
            "schedules.startPeriod",
            "schedules.endPeriod"
    })
    List<ClassSection> findByTeacherIdAndSemesterId(Long teacherId, Long semesterId);

    @EntityGraph(attributePaths = {
            "course",
            "course.major",
            "course.major.department",
            "semester",
            "registrationRound",
            "teacher",
            "teacher.user",
            "schedules",
            "schedules.room",
            "schedules.startPeriod",
            "schedules.endPeriod"
    })
    List<ClassSection> findByTeacherId(Long teacherId);

    @Query("select count(cs) from ClassSection cs " +
            "where cs.semester.id = :semesterId " +
            "and cs.room.id = :roomId " +
            "and cs.dayOfWeek = :dayOfWeek " +
            "and cs.id <> coalesce(:excludeId, -1) " +
            "and cs.startPeriod.periodNumber <= :endPeriodNumber " +
            "and cs.endPeriod.periodNumber >= :startPeriodNumber")
    long countRoomConflicts(
            @Param("semesterId") Long semesterId,
            @Param("roomId") Long roomId,
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("startPeriodNumber") Integer startPeriodNumber,
            @Param("endPeriodNumber") Integer endPeriodNumber,
            @Param("excludeId") Long excludeId);

    @Query("SELECT cs FROM ClassSection cs " +
            "WHERE cs.semester.id = :semesterId " +
            "AND cs.teacher.id = :teacherId " +
            "AND cs.dayOfWeek = :dayOfWeek " +
            "AND cs.id <> COALESCE(:excludeId, -1) " +
            "AND cs.startPeriod.periodNumber <= :endPeriodNumber " +
            "AND cs.endPeriod.periodNumber >= :startPeriodNumber")
    List<ClassSection> findTeacherConflicts(
            @Param("semesterId") Long semesterId,
            @Param("teacherId") Long teacherId,
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("startPeriodNumber") Integer startPeriodNumber,
            @Param("endPeriodNumber") Integer endPeriodNumber,
            @Param("excludeId") Long excludeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select cs from ClassSection cs where cs.id = :id")
    Optional<ClassSection> findByIdForUpdate(@Param("id") Long id);

    long countBySemesterId(Long semesterId);

    long countByRegistrationRoundId(Long registrationRoundId);

    @Query("""
            SELECT cs FROM ClassSection cs
            LEFT JOIN cs.course c
            LEFT JOIN cs.teacher t
            WHERE (:semesterId IS NULL OR cs.semester.id = :semesterId)
              AND (:keyword IS NULL OR :keyword = ''
                OR LOWER(cs.classCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(t.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:status IS NULL OR cs.status = :status)
            """)
    Page<ClassSection> searchAdmin(
            @Param("semesterId") Long semesterId,
            @Param("keyword") String keyword,
            @Param("status") ClassSectionStatus status,
            Pageable pageable
    );

    @Modifying
    @Query("UPDATE ClassSection cs SET cs.registrationRound = :round WHERE cs.semester.id = :semesterId AND cs.status = 'DRAFT' AND cs.registrationRound IS NULL")
    int assignUnassignedDraftSectionsToRound(@Param("semesterId") Long semesterId, @Param("round") RegistrationRound round);

    @Modifying
    @Query("UPDATE ClassSection cs SET cs.status = :targetStatus WHERE cs.registrationRound.id = :roundId AND cs.status IN :sourceStatuses")
    int updateStatusForRoundSections(@Param("roundId") Long roundId, @Param("targetStatus") ClassSectionStatus targetStatus, @Param("sourceStatuses") List<ClassSectionStatus> sourceStatuses);
}
