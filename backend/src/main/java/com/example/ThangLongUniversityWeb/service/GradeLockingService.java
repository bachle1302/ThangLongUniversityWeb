package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.entity.ClassSection;
import com.example.ThangLongUniversityWeb.entity.Enrollment;
import com.example.ThangLongUniversityWeb.repository.ClassSectionRepository;
import com.example.ThangLongUniversityWeb.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradeLockingService {

    private final ClassSectionRepository classSectionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AcademicResultService academicResultService;

    /**
     * Khóa điểm cho tất cả lớp trong một học kỳ
     */
    @Transactional
    public void lockAllGradesInSemester(Long semesterId) {
        // Lấy tất cả class sections trong học kỳ
        List<ClassSection> classSections = classSectionRepository.findBySemesterId(semesterId);

        // Khóa điểm cho tất cả lớp
        for (ClassSection cs : classSections) {
            if (!cs.isGradeLocked()) {
                cs.setGradeLocked(true);
                classSectionRepository.save(cs);
            }
        }

        // Sau khi khóa thành công, tính GPA/CPA cho tất cả sinh viên trong học kỳ
        calculateGPAForAllStudentsInSemester(semesterId);
    }

    /**
     * Tính GPA/CPA cho tất cả sinh viên trong học kỳ (async)
     */
    @Async
    public void calculateGPAForAllStudentsInSemester(Long semesterId) {
        // Lấy danh sách sinh viên duy nhất trong học kỳ
        List<Long> studentIds = enrollmentRepository.findByClassSectionSemesterId(semesterId).stream()
                .map(e -> e.getStudent().getId())
                .distinct()
                .collect(Collectors.toList());

        // Tính GPA cho từng sinh viên
        for (Long studentId : studentIds) {
            try {
                // Tính GPA học kỳ
                academicResultService.calculateSemesterGPA(studentId, semesterId);

                // Tính CPA tích lũy
                academicResultService.calculateCumulativeGPA(studentId);

            } catch (Exception e) {
                // Log lỗi nhưng không dừng quá trình
                System.err.println("Lỗi khi tính GPA cho sinh viên " + studentId + ": " + e.getMessage());
            }
        }
    }
}
