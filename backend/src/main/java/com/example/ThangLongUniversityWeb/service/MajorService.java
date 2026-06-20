package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.dto.request.MajorRequest;
import com.example.ThangLongUniversityWeb.dto.response.MajorResponse;
import com.example.ThangLongUniversityWeb.entity.Department;
import com.example.ThangLongUniversityWeb.entity.Major;
import com.example.ThangLongUniversityWeb.repository.CourseRepository;
import com.example.ThangLongUniversityWeb.repository.DepartmentRepository;
import com.example.ThangLongUniversityWeb.repository.MajorRepository;
import com.example.ThangLongUniversityWeb.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MajorService {

    private final MajorRepository majorRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public List<MajorResponse> getAllMajors() {
        return majorRepository.findAllWithDepartment().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(cacheNames = {"adminDashboard", "classSectionOptions", "courses"}, allEntries = true)
    public MajorResponse createMajor(MajorRequest request) {
        validateMajorRequest(request);
        if (majorRepository.existsByMajorCode(request.getMajorCode().trim())) {
            throw new RuntimeException("Mã ngành " + request.getMajorCode() + " đã tồn tại!");
        }
        if (majorRepository.existsByName(request.getName().trim())) {
            throw new RuntimeException("Tên ngành " + request.getName() + " đã tồn tại!");
        }

        Major major = new Major();
        major.setMajorCode(request.getMajorCode().trim());
        major.setName(request.getName().trim());
        major.setDescription(request.getDescription());
        major.setDepartment(getDepartmentOrNull(request.getDepartmentId()));

        return toResponse(majorRepository.save(major));
    }

    @Transactional
    @CacheEvict(cacheNames = {"adminDashboard", "classSectionOptions", "courses"}, allEntries = true)
    public MajorResponse updateMajor(Long id, MajorRequest request) {
        Major major = majorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ngành học!"));

        if (request.getMajorCode() != null && !request.getMajorCode().isBlank()) {
            String code = request.getMajorCode().trim();
            majorRepository.findByMajorCode(code).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new RuntimeException("Mã ngành " + code + " đã tồn tại!");
                }
            });
            major.setMajorCode(code);
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            majorRepository.findByName(request.getName().trim()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new RuntimeException("Tên ngành " + request.getName() + " đã tồn tại!");
                }
            });
            major.setName(request.getName().trim());
        }

        major.setDescription(request.getDescription());
        major.setDepartment(getDepartmentOrNull(request.getDepartmentId()));
        return toResponse(majorRepository.save(major));
    }

    @Transactional
    @CacheEvict(cacheNames = {"adminDashboard", "classSectionOptions", "courses"}, allEntries = true)
    public void deleteMajor(Long id) {
        majorRepository.deleteById(id);
    }

    private void validateMajorRequest(MajorRequest request) {
        if (request.getMajorCode() == null || request.getMajorCode().isBlank()) {
            throw new RuntimeException("majorCode không được để trống");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new RuntimeException("Tên ngành không được để trống");
        }
    }

    private MajorResponse toResponse(Major major) {
        Department department = major.getDepartment();
        return MajorResponse.builder()
                .id(major.getId())
                .majorCode(major.getMajorCode())
                .name(major.getName())
                .description(major.getDescription())
                .studentCount(studentRepository.countByMajorId(major.getId()))
                .courseCount(courseRepository.countByMajorId(major.getId()))
                .departmentId(department != null ? department.getId() : null)
                .departmentName(department != null ? department.getName() : null)
                .build();
    }

    private Department getDepartmentOrNull(Long departmentId) {
        if (departmentId == null) {
            return null;
        }
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay khoa/bo mon!"));
    }
}
