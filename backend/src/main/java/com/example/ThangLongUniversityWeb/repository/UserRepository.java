package com.example.ThangLongUniversityWeb.repository;

import com.example.ThangLongUniversityWeb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Tìm user bằng username để check đăng nhập
    Optional<User> findByUsername(String username);

    // Kiểm tra user có tồn tại không (dùng khi đăng ký)
    Boolean existsByUsername(String username);

        Boolean existsByEmail(String email);

        Boolean existsByUsernameAndIdNot(String username, Long id);

        Boolean existsByEmailAndIdNot(String email, Long id);

        @Query("""
          SELECT DISTINCT u FROM User u
          LEFT JOIN FETCH u.student
          LEFT JOIN FETCH u.teacher
          ORDER BY u.id ASC
          """)
        List<User> findAllWithProfiles();

    // Tìm kiếm user theo username (dùng cho chat search)
    List<User> findTop20ByUsernameContainingIgnoreCase(String q);

    @Query("""
            SELECT DISTINCT u FROM User u
            LEFT JOIN u.student s
            LEFT JOIN u.teacher t
            WHERE u.isActive = true
              AND (
                LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(COALESCE(s.studentCode, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(COALESCE(s.fullName, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(COALESCE(t.teacherCode, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(COALESCE(t.fullName, '')) LIKE LOWER(CONCAT('%', :q, '%'))
              )
            ORDER BY u.username
            """)
    List<User> searchChatUsers(String q);
}
