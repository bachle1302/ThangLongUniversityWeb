package com.example.ThangLongUniversityWeb.repository;

import com.example.ThangLongUniversityWeb.entity.Semester;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, Long> {
    Optional<Semester> findByName(String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Semester s where s.id = :id")
    Optional<Semester> findByIdForUpdate(@Param("id") Long id);

    // Tìm các học kỳ ĐANG MỞ cửa cho sinh viên đăng ký tín chỉ
    List<Semester> findByIsRegistrationOpenTrue();
}
