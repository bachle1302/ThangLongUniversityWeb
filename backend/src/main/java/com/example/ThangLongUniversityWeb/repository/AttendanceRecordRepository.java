package com.example.ThangLongUniversityWeb.repository;

import com.example.ThangLongUniversityWeb.entity.AttendanceRecord;
import com.example.ThangLongUniversityWeb.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findByAttendanceSessionId(Long sessionId);

    List<AttendanceRecord> findByEnrollmentId(Long enrollmentId);

    /** Đếm số buổi vắng của một enrollment */
    @Query("SELECT COUNT(r) FROM AttendanceRecord r " +
           "WHERE r.enrollment.id = :enrollmentId AND r.status = :status")
    long countByEnrollmentIdAndStatus(@Param("enrollmentId") Long enrollmentId,
                                      @Param("status") AttendanceStatus status);

    /** Lấy toàn bộ records của một lớp học phần (qua session) */
    @Query("SELECT r FROM AttendanceRecord r " +
           "WHERE r.attendanceSession.classSection.id = :classSectionId")
    List<AttendanceRecord> findByClassSectionId(@Param("classSectionId") Long classSectionId);
}
