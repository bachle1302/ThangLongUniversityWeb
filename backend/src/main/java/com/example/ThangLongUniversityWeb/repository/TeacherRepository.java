package com.example.ThangLongUniversityWeb.repository;

import com.example.ThangLongUniversityWeb.entity.Teacher;
import com.example.ThangLongUniversityWeb.enums.TeacherStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    // Tìm giảng viên theo mã GV (sau này sẽ cần dùng)
    Optional<Teacher> findByTeacherCode(String teacherCode);

    Optional<Teacher> findByUser_Username(String username);

    long countByDepartmentId(Long departmentId);

    @Query("""
            SELECT t FROM Teacher t
            LEFT JOIN t.user u
            LEFT JOIN t.department d
            WHERE (:keyword IS NULL OR :keyword = ''
                OR LOWER(t.teacherCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(t.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:departmentId IS NULL OR d.id = :departmentId)
              AND (:status IS NULL OR t.status = :status)
            """)
    Page<Teacher> searchAdmin(
            @Param("keyword") String keyword,
            @Param("departmentId") Long departmentId,
            @Param("status") TeacherStatus status,
            Pageable pageable
    );
}
