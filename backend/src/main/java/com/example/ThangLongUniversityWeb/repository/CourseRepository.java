package com.example.ThangLongUniversityWeb.repository;

import com.example.ThangLongUniversityWeb.entity.Course;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    // Ép Hibernate lấy kèm dữ liệu major và prerequisites cho phương thức findAll()
    @Override
    @EntityGraph(attributePaths = {"major", "major.department", "prerequisites"})
    List<Course> findAll();

    // Lấy kèm dữ liệu khi tìm theo ID (dùng trong update/delete)
    @Override
    @EntityGraph(attributePaths = {"major", "major.department", "prerequisites"})
    Optional<Course> findById(Long id);

    // Lấy kèm dữ liệu khi tìm theo Code (dùng trong create)
    @EntityGraph(attributePaths = {"major", "major.department", "prerequisites"})
    Optional<Course> findByCode(String code);

    // Lấy danh sach mon hoc theo nganh (dung cho chuong trinh dao tao)
    @EntityGraph(attributePaths = {"major", "major.department", "prerequisites"})
    List<Course> findByMajorId(Long majorId);

    long countByMajorId(Long majorId);
}
