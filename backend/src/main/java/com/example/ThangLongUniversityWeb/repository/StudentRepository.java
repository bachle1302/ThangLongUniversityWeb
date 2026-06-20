package com.example.ThangLongUniversityWeb.repository;

import com.example.ThangLongUniversityWeb.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    // Tìm sinh viên theo mã SV (sau này sẽ cần dùng)
    Optional<Student> findByStudentCode(String studentCode);

    Optional<Student> findByUser_Username(String username);

    List<Student> findByHomeroomId(Long homeroomId);

    long countByHomeroomId(Long homeroomId);

    long countByMajorId(Long majorId);

    @Query("""
            SELECT s FROM Student s
            LEFT JOIN s.user u
            LEFT JOIN s.major m
            WHERE (:keyword IS NULL OR :keyword = ''
                OR LOWER(s.studentCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:majorId IS NULL OR m.id = :majorId)
              AND (:status IS NULL OR :status = '' OR LOWER(s.status) = LOWER(:status))
            """)
    Page<Student> searchAdmin(
            @Param("keyword") String keyword,
            @Param("majorId") Long majorId,
            @Param("status") String status,
            Pageable pageable
    );
}
