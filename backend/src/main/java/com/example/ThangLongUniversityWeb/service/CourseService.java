package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.dto.request.CourseRequest;
import com.example.ThangLongUniversityWeb.dto.response.CourseResponse;
import com.example.ThangLongUniversityWeb.entity.Course;
import com.example.ThangLongUniversityWeb.entity.Major;
import com.example.ThangLongUniversityWeb.enums.CourseType;
import com.example.ThangLongUniversityWeb.exception.ConflictException;
import com.example.ThangLongUniversityWeb.repository.CourseRepository;
import com.example.ThangLongUniversityWeb.repository.MajorRepository;
import com.example.ThangLongUniversityWeb.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final MajorRepository majorRepository;
    private final StudentRepository studentRepository;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "courses")
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getCoursesByStudentMajor(String username) {
        var student = studentRepository.findByUser_Username(username)
                .orElseThrow(() -> new RuntimeException("Khong tim thay sinh vien!"));
        if (student.getMajor() == null) {
            return getAllCourses();
        }
        return courseRepository.findByMajorId(student.getMajor().getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(cacheNames = {"courses", "classSectionOptions", "adminDashboard"}, allEntries = true)
    public CourseResponse createCourse(CourseRequest request) {
        String code = normalizeCode(request.getCode());
        if (courseRepository.findByCode(code).isPresent()) {
            throw new RuntimeException("Ma mon hoc da ton tai!");
        }

        Course course = new Course();
        applyCourseFields(course, request, code);
        applyMajorAndPrerequisites(course, request);

        return mapToResponse(courseRepository.save(course));
    }

    @Transactional
    @CacheEvict(cacheNames = {"courses", "classSectionOptions", "adminDashboard"}, allEntries = true)
    public CourseResponse updateCourse(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay mon hoc!"));

        String code = normalizeCode(request.getCode());
        courseRepository.findByCode(code).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new RuntimeException("Ma mon hoc da ton tai!");
            }
        });

        applyCourseFields(course, request, code);
        applyMajorAndPrerequisites(course, request);

        return mapToResponse(courseRepository.save(course));
    }

    @Transactional
    @CacheEvict(cacheNames = {"courses", "classSectionOptions", "adminDashboard"}, allEntries = true)
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay mon hoc!"));
        try {
            courseRepository.delete(course);
            courseRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Khong the xoa mon hoc vi dang duoc su dung trong lop hoc, dang ky hoc, diem hoac mon tien quyet.");
        }
    }

    private void applyCourseFields(Course course, CourseRequest request, String code) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new RuntimeException("Ten mon hoc khong duoc de trong!");
        }
        if (request.getCredits() == null || request.getCredits() < 1) {
            throw new RuntimeException("So tin chi phai lon hon 0!");
        }
        course.setCode(code);
        course.setName(request.getName().trim());
        course.setCredits(request.getCredits());
        course.setDescription(request.getDescription());
        course.setCourseType(request.getCourseType() != null ? request.getCourseType() : CourseType.REQUIRED);
    }

    private CourseResponse mapToResponse(Course course) {
        CourseType courseType = course.getCourseType() != null ? course.getCourseType() : CourseType.REQUIRED;
        return CourseResponse.builder()
                .id(course.getId())
                .code(course.getCode())
                .name(course.getName())
                .credits(course.getCredits())
                .description(course.getDescription())
                .courseType(courseType)
                .courseTypeLabel(courseType == CourseType.ELECTIVE ? "Tu do" : "Bat buoc")
                .majorId(course.getMajor() != null ? course.getMajor().getId() : null)
                .majorName(course.getMajor() != null ? course.getMajor().getName() : "Dai cuong")
                .departmentId(course.getMajor() != null && course.getMajor().getDepartment() != null
                        ? course.getMajor().getDepartment().getId()
                        : null)
                .departmentName(course.getMajor() != null && course.getMajor().getDepartment() != null
                        ? course.getMajor().getDepartment().getName()
                        : null)
                .prerequisiteCourseIds(course.getPrerequisites().stream()
                        .map(Course::getId)
                        .collect(Collectors.toList()))
                .prerequisiteNames(course.getPrerequisites().stream()
                        .map(Course::getName)
                        .collect(Collectors.toSet()))
                .build();
    }

    private void applyMajorAndPrerequisites(Course course, CourseRequest request) {
        if (request.getMajorId() != null) {
            Major major = majorRepository.findById(request.getMajorId())
                    .orElseThrow(() -> new RuntimeException("Nganh khong ton tai!"));
            course.setMajor(major);
        } else {
            course.setMajor(null);
        }

        if (request.getPrerequisiteCourseIds() != null) {
            var prerequisites = new HashSet<Course>();
            for (Long prerequisiteId : request.getPrerequisiteCourseIds()) {
                if (prerequisiteId == null) {
                    continue;
                }
                if (course.getId() != null && prerequisiteId.equals(course.getId())) {
                    throw new RuntimeException("Mon hoc khong the la tien quyet cua chinh no!");
                }
                Course prerequisite = courseRepository.findById(prerequisiteId)
                        .orElseThrow(() -> new RuntimeException("Khong tim thay mon hoc tien quyet id=" + prerequisiteId));
                prerequisites.add(prerequisite);
            }
            course.setPrerequisites(prerequisites);
        }
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new RuntimeException("Ma mon hoc khong duoc de trong!");
        }
        return code.trim().toUpperCase();
    }
}
