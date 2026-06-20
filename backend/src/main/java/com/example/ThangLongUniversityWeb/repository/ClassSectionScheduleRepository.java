package com.example.ThangLongUniversityWeb.repository;

import com.example.ThangLongUniversityWeb.entity.ClassSection;
import com.example.ThangLongUniversityWeb.entity.ClassSectionSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassSectionScheduleRepository extends JpaRepository<ClassSectionSchedule, Long> {
    List<ClassSectionSchedule> findByClassSectionId(Long classSectionId);

    @Query("select count(s) from ClassSectionSchedule s " +
            "where s.classSection.semester.id = :semesterId " +
            "and s.classSection.status <> com.example.ThangLongUniversityWeb.enums.ClassSectionStatus.CANCELLED " +
            "and s.room.id = :roomId " +
            "and s.dayOfWeek = :dayOfWeek " +
            "and s.classSection.id <> coalesce(:excludeClassSectionId, -1) " +
            "and s.startPeriod.periodNumber <= :endPeriodNumber " +
            "and s.endPeriod.periodNumber >= :startPeriodNumber")
    long countRoomConflicts(
            @Param("semesterId") Long semesterId,
            @Param("roomId") Long roomId,
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("startPeriodNumber") Integer startPeriodNumber,
            @Param("endPeriodNumber") Integer endPeriodNumber,
            @Param("excludeClassSectionId") Long excludeClassSectionId);

    @Query("select s.classSection from ClassSectionSchedule s " +
            "where s.classSection.semester.id = :semesterId " +
            "and s.classSection.status <> com.example.ThangLongUniversityWeb.enums.ClassSectionStatus.CANCELLED " +
            "and s.classSection.teacher.id = :teacherId " +
            "and s.dayOfWeek = :dayOfWeek " +
            "and s.classSection.id <> coalesce(:excludeClassSectionId, -1) " +
            "and s.startPeriod.periodNumber <= :endPeriodNumber " +
            "and s.endPeriod.periodNumber >= :startPeriodNumber")
    List<ClassSection> findTeacherConflicts(
            @Param("semesterId") Long semesterId,
            @Param("teacherId") Long teacherId,
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("startPeriodNumber") Integer startPeriodNumber,
            @Param("endPeriodNumber") Integer endPeriodNumber,
            @Param("excludeClassSectionId") Long excludeClassSectionId);
}
