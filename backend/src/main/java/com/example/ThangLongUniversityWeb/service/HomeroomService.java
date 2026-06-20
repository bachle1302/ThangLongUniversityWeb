package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.dto.request.HomeroomRequest;
import com.example.ThangLongUniversityWeb.dto.request.HomeroomStudentsRequest;
import com.example.ThangLongUniversityWeb.dto.response.HomeroomResponse;
import com.example.ThangLongUniversityWeb.dto.response.StudentResponse;
import com.example.ThangLongUniversityWeb.entity.Homeroom;
import com.example.ThangLongUniversityWeb.entity.Major;
import com.example.ThangLongUniversityWeb.entity.Student;
import com.example.ThangLongUniversityWeb.entity.Teacher;
import com.example.ThangLongUniversityWeb.exception.ConflictException;
import com.example.ThangLongUniversityWeb.repository.HomeroomRepository;
import com.example.ThangLongUniversityWeb.repository.MajorRepository;
import com.example.ThangLongUniversityWeb.repository.StudentRepository;
import com.example.ThangLongUniversityWeb.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeroomService {

    private final HomeroomRepository homeroomRepository;
    private final TeacherRepository teacherRepository;
    private final MajorRepository majorRepository;
    private final StudentRepository studentRepository;
    private final StudentService studentService;

    @Transactional
    public List<HomeroomResponse> getAllHomerooms() {
        return homeroomRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public HomeroomResponse createHomeroom(HomeroomRequest request) {
        if (request.getClassName() == null || request.getClassName().isBlank()) {
            throw new RuntimeException("Ten lop khong duoc de trong");
        }
        String className = request.getClassName().trim();
        if (homeroomRepository.existsByClassName(className)) {
            throw new RuntimeException("Lop " + className + " da ton tai!");
        }

        Homeroom homeroom = new Homeroom();
        homeroom.setClassName(className);
        homeroom.setCohort(request.getCohort());
        homeroom.setAcademicYear(request.getAcademicYear());
        homeroom.setIsActive(calculateActive(request.getAcademicYear()));

        if (request.getMajorId() == null) {
            throw new RuntimeException("Lop hanh chinh phai co nganh hoc!");
        }
        Major major = resolveMajor(request.getMajorId());
        homeroom.setMajor(major);

        Teacher advisor = resolveAdvisor(request.getAdvisorId());
        validateAdvisorDepartment(advisor, major);
        homeroom.setAdvisor(advisor);

        return toResponse(homeroomRepository.save(homeroom));
    }

    @Transactional
    public HomeroomResponse updateHomeroom(Long id, HomeroomRequest request) {
        Homeroom homeroom = homeroomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay lop hanh chinh!"));

        if (request.getClassName() != null && !request.getClassName().isBlank()) {
            String className = request.getClassName().trim();
            homeroomRepository.findByClassName(className).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new RuntimeException("Lop " + className + " da ton tai!");
                }
            });
            homeroom.setClassName(className);
        }

        if (request.getMajorId() != null) {
            Major major = resolveMajor(request.getMajorId());
            homeroom.setMajor(major);
            validateExistingStudentsMajor(homeroom);
        }

        Teacher advisor = resolveAdvisor(request.getAdvisorId());
        validateAdvisorDepartment(advisor, homeroom.getMajor());
        homeroom.setAdvisor(advisor);

        if (request.getAcademicYear() != null) {
            homeroom.setAcademicYear(request.getAcademicYear());
            homeroom.setIsActive(calculateActive(request.getAcademicYear()));
        }
        if (request.getCohort() != null) {
            homeroom.setCohort(request.getCohort());
        }

        return toResponse(homeroomRepository.save(homeroom));
    }

    @Transactional
    public void deleteHomeroom(Long id) {
        Homeroom homeroom = homeroomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay lop hanh chinh!"));

        long studentCount = studentRepository.countByHomeroomId(id);
        if (studentCount > 0) {
            throw new ConflictException("Khong the xoa lop vi con " + studentCount + " sinh vien.");
        }

        homeroomRepository.delete(homeroom);
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getStudentsByHomeroom(Long homeroomId) {
        if (!homeroomRepository.existsById(homeroomId)) {
            throw new RuntimeException("Khong tim thay lop hanh chinh!");
        }
        return studentRepository.findByHomeroomId(homeroomId).stream()
                .map(studentService::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addStudentsToHomeroom(Long homeroomId, HomeroomStudentsRequest request) {
        Homeroom homeroom = homeroomRepository.findById(homeroomId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay lop hanh chinh!"));

        List<Student> students = studentRepository.findAllById(request.getStudentIds());
        if (students.size() != request.getStudentIds().size()) {
            throw new RuntimeException("Mot so sinh vien khong ton tai!");
        }

        for (Student student : students) {
            validateStudentMajor(student, homeroom);
            student.setHomeroom(homeroom);
        }
        studentRepository.saveAll(students);
    }

    @Transactional
    public void removeStudentFromHomeroom(Long homeroomId, Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay sinh vien!"));

        if (student.getHomeroom() == null || !student.getHomeroom().getId().equals(homeroomId)) {
            throw new RuntimeException("Sinh vien khong thuoc lop nay!");
        }

        student.setHomeroom(null);
        studentRepository.save(student);
    }

    private HomeroomResponse toResponse(Homeroom homeroom) {
        Teacher advisor = homeroom.getAdvisor();
        Major major = homeroom.getMajor();
        Boolean active = calculateActive(homeroom.getAcademicYear());
        if (!Objects.equals(homeroom.getIsActive(), active)) {
            homeroom.setIsActive(active);
        }

        return HomeroomResponse.builder()
                .id(homeroom.getId())
                .className(homeroom.getClassName())
                .advisorId(advisor != null ? advisor.getId() : null)
                .advisorCode(advisor != null ? advisor.getTeacherCode() : null)
                .advisorName(advisor != null ? advisor.getFullName() : null)
                .majorId(major != null ? major.getId() : null)
                .majorName(major != null ? major.getName() : null)
                .academicYear(homeroom.getAcademicYear())
                .cohort(homeroom.getCohort())
                .studentCount(studentRepository.countByHomeroomId(homeroom.getId()))
                .isActive(active)
                .build();
    }

    private Major resolveMajor(Long majorId) {
        if (majorId == null) {
            return null;
        }
        return majorRepository.findById(majorId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay nganh hoc!"));
    }

    private Teacher resolveAdvisor(Long advisorId) {
        if (advisorId == null) {
            return null;
        }
        return teacherRepository.findById(advisorId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay giang vien co van!"));
    }

    private Boolean calculateActive(Integer academicYear) {
        if (academicYear == null) {
            return true;
        }
        return Year.now().getValue() < academicYear + 4;
    }

    private void validateStudentMajor(Student student, Homeroom homeroom) {
        Major homeroomMajor = homeroom.getMajor();
        if (homeroomMajor == null) {
            return;
        }
        Major studentMajor = student.getMajor();
        if (studentMajor == null || !Objects.equals(studentMajor.getId(), homeroomMajor.getId())) {
            throw new RuntimeException("Sinh vien " + student.getStudentCode() + " khong cung nganh voi lop hanh chinh!");
        }
    }

    private void validateExistingStudentsMajor(Homeroom homeroom) {
        for (Student student : studentRepository.findByHomeroomId(homeroom.getId())) {
            validateStudentMajor(student, homeroom);
        }
    }

    private void validateAdvisorDepartment(Teacher advisor, Major major) {
        if (advisor == null || major == null || major.getDepartment() == null) {
            return;
        }
        if (advisor.getDepartment() == null
                || !Objects.equals(advisor.getDepartment().getId(), major.getDepartment().getId())) {
            throw new RuntimeException("Giang vien co van phai thuoc cung khoa voi nganh cua lop!");
        }
    }
}
