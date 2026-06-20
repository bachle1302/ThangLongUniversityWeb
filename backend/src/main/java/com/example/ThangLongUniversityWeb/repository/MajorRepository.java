package com.example.ThangLongUniversityWeb.repository;

import com.example.ThangLongUniversityWeb.entity.Major;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MajorRepository extends JpaRepository<Major, Long> {
    @Query("select m from Major m left join fetch m.department order by m.id asc")
    List<Major> findAllWithDepartment();

    Optional<Major> findByMajorCode(String majorCode);
    Optional<Major> findByName(String name);
    boolean existsByMajorCode(String majorCode);
    boolean existsByName(String name);
    long countByDepartmentId(Long departmentId);
}
