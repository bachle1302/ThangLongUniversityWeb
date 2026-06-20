package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.entity.ClassSection;
import com.example.ThangLongUniversityWeb.entity.Course;
import com.example.ThangLongUniversityWeb.entity.Enrollment;
import com.example.ThangLongUniversityWeb.entity.ExamRegistration;
import com.example.ThangLongUniversityWeb.entity.ExamRoomAssignment;
import com.example.ThangLongUniversityWeb.entity.ExamSession;
import com.example.ThangLongUniversityWeb.entity.Semester;
import com.example.ThangLongUniversityWeb.entity.Teacher;
import com.example.ThangLongUniversityWeb.enums.ClassSectionStatus;
import com.example.ThangLongUniversityWeb.enums.EnrollmentStatus;
import com.example.ThangLongUniversityWeb.enums.ExamType;
import com.example.ThangLongUniversityWeb.repository.ClassSectionRepository;
import com.example.ThangLongUniversityWeb.repository.EnrollmentRepository;
import com.example.ThangLongUniversityWeb.repository.ExamRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RetakeClassAssignmentService {

    private final ClassSectionRepository classSectionRepository;
    private final ExamRegistrationRepository examRegistrationRepository;
    private final EnrollmentRepository enrollmentRepository;

    public record AssignmentResult(
            int assignedCount,
            String virtualClassCode,
            Long virtualClassSectionId,
            List<String> warnings
    ) {}

    @Transactional
    public AssignmentResult syncAssignmentsAfterSeatAllocation(
            ExamSession session,
            List<ExamRoomAssignment> roomAssignments,
            List<ExamRegistration> retakeRegistrations
    ) {
        String selection = session.getCandidateSelection();
        if ("NORMAL_ONLY".equalsIgnoreCase(selection) || retakeRegistrations.isEmpty()) {
            return new AssignmentResult(0, null, null, List.of());
        }

        if ("RETAKE_ONLY".equalsIgnoreCase(selection)) {
            return assignToVirtualClass(session, roomAssignments, retakeRegistrations);
        }
        return assignForMixedExam(session, roomAssignments, retakeRegistrations);
    }

    private AssignmentResult assignForMixedExam(
            ExamSession session,
            List<ExamRoomAssignment> roomAssignments,
            List<ExamRegistration> retakeRegistrations
    ) {
        Long semesterId = session.getSemester().getId();
        Long courseId = session.getCourse().getId();
        List<String> warnings = new ArrayList<>();

        List<ClassSection> targetSections = classSectionRepository.findBySemesterIdAndCourseId(semesterId, courseId)
                .stream()
                .filter(cs -> cs.getSourceExamSession() == null)
                .filter(cs -> cs.getStatus() != ClassSectionStatus.CANCELLED)
                .filter(cs -> hasRegisteredEnrollments(cs.getId()))
                .sorted(Comparator.comparing(ClassSection::getClassCode))
                .toList();

        if (targetSections.isEmpty()) {
            warnings.add("Mon hoc khong co lop HP trong ky, tao lop ao thi lai.");
            return assignToVirtualClass(session, roomAssignments, retakeRegistrations, warnings);
        }

        int assigned = 0;
        for (ExamRegistration reg : retakeRegistrations) {
            ClassSection selected = targetSections.stream()
                    .min(Comparator.comparing(cs -> examRegistrationRepository
                            .countByClassSectionIdAndStatus(cs.getId(), EnrollmentStatus.REGISTERED)))
                    .orElse(targetSections.getFirst());
            reg.setClassSection(selected);
            examRegistrationRepository.save(reg);
            assigned++;
        }

        return new AssignmentResult(assigned, null, null, warnings);
    }

    private AssignmentResult assignToVirtualClass(
            ExamSession session,
            List<ExamRoomAssignment> roomAssignments,
            List<ExamRegistration> retakeRegistrations
    ) {
        return assignToVirtualClass(session, roomAssignments, retakeRegistrations, new ArrayList<>());
    }

    private AssignmentResult assignToVirtualClass(
            ExamSession session,
            List<ExamRoomAssignment> roomAssignments,
            List<ExamRegistration> retakeRegistrations,
            List<String> warnings
    ) {
        Teacher proctor = roomAssignments.stream()
                .map(ExamRoomAssignment::getProctor)
                .filter(p -> p != null)
                .findFirst()
                .orElse(null);

        if (proctor == null) {
            throw new RuntimeException("Can chon giam thi (giang vien cham diem) cho ca thi thi lai/nang diem.");
        }

        ClassSection virtualClass = classSectionRepository.findBySourceExamSessionId(session.getId())
                .orElseGet(() -> createVirtualClass(session, proctor, roomAssignments));

        if (!proctor.getId().equals(virtualClass.getTeacher().getId())) {
            virtualClass.setTeacher(proctor);
            classSectionRepository.save(virtualClass);
        }

        int assigned = 0;
        for (ExamRegistration reg : retakeRegistrations) {
            reg.setClassSection(virtualClass);
            examRegistrationRepository.save(reg);
            assigned++;
        }

        return new AssignmentResult(assigned, virtualClass.getClassCode(), virtualClass.getId(), warnings);
    }

    private ClassSection createVirtualClass(ExamSession session, Teacher proctor, List<ExamRoomAssignment> roomAssignments) {
        Course course = session.getCourse();
        Semester semester = session.getSemester();
        String datePart = session.getExamAt() != null
                ? session.getExamAt().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                : String.valueOf(session.getId());

        ClassSection section = new ClassSection();
        section.setClassCode(course.getCode() + "-TL-" + datePart);
        section.setCourse(course);
        section.setSemester(semester);
        section.setTeacher(proctor);
        section.setExamType(ExamType.RETAKE);
        section.setStatus(ClassSectionStatus.OPEN);
        section.setGradeLocked(false);
        section.setMaxSlots(Math.max(retakeCount(session), 1));
        section.setCurrentSlots(0);
        section.setSourceExamSession(session);
        section.setExamAt(session.getExamAt());
        roomAssignments.stream()
                .map(ExamRoomAssignment::getRoom)
                .filter(room -> room != null)
                .findFirst()
                .ifPresent(room -> section.setExamRoom(room.getName()));
        return classSectionRepository.save(section);
    }

    private int retakeCount(ExamSession session) {
        return examRegistrationRepository.findBySemesterIdAndCourseIdAndStatus(
                session.getSemester().getId(),
                session.getCourse().getId(),
                EnrollmentStatus.REGISTERED
        ).size();
    }

    private boolean hasRegisteredEnrollments(Long classSectionId) {
        return enrollmentRepository.findByClassSectionId(classSectionId).stream()
                .anyMatch(e -> e.getStatus() == EnrollmentStatus.REGISTERED);
    }
}
