package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.dto.request.DepartmentRequest;
import com.example.ThangLongUniversityWeb.dto.response.DepartmentResponse;
import com.example.ThangLongUniversityWeb.entity.Department;
import com.example.ThangLongUniversityWeb.repository.DepartmentRepository;
import com.example.ThangLongUniversityWeb.repository.MajorRepository;
import com.example.ThangLongUniversityWeb.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final TeacherRepository teacherRepository;
    private final MajorRepository majorRepository;

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(cacheNames = {"adminDashboard", "classSectionOptions", "courses"}, allEntries = true)
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (request.getDepartmentCode() == null || request.getDepartmentCode().isBlank()) {
            throw new RuntimeException("Mã khoa không được để trống");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new RuntimeException("Tên khoa không được để trống");
        }
        if (departmentRepository.existsByDepartmentCode(request.getDepartmentCode().trim())) {
            throw new RuntimeException("Mã khoa " + request.getDepartmentCode() + " đã tồn tại!");
        }
        if (departmentRepository.existsByName(request.getName().trim())) {
            throw new RuntimeException("Tên khoa " + request.getName() + " đã tồn tại!");
        }

        Department department = new Department();
        department.setDepartmentCode(request.getDepartmentCode().trim());
        department.setName(request.getName().trim());
        department.setDescription(request.getDescription());

        return toResponse(departmentRepository.save(department));
    }

    @Transactional
    @CacheEvict(cacheNames = {"adminDashboard", "classSectionOptions", "courses"}, allEntries = true)
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khoa/bộ môn!"));

        if (request.getDepartmentCode() != null && !request.getDepartmentCode().isBlank()) {
            String code = request.getDepartmentCode().trim();
            departmentRepository.findByDepartmentCode(code).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new RuntimeException("Mã khoa " + code + " đã tồn tại!");
                }
            });
            department.setDepartmentCode(code);
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            departmentRepository.findByName(request.getName().trim()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new RuntimeException("Tên khoa " + request.getName() + " đã tồn tại!");
                }
            });
            department.setName(request.getName().trim());
        }

        department.setDescription(request.getDescription());
        return toResponse(departmentRepository.save(department));
    }

    @Transactional
    @CacheEvict(cacheNames = {"adminDashboard", "classSectionOptions", "courses"}, allEntries = true)
    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy khoa/bộ môn!");
        }
        departmentRepository.deleteById(id);
    }

    private DepartmentResponse toResponse(Department department) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .departmentCode(department.getDepartmentCode())
                .name(department.getName())
                .description(department.getDescription())
                .teacherCount(teacherRepository.countByDepartmentId(department.getId()))
                .majorCount(majorRepository.countByDepartmentId(department.getId()))
                .build();
    }
}
