package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.dto.request.ExamSessionRequest;
import com.example.ThangLongUniversityWeb.dto.response.ExamRoomAssignmentResponse;
import com.example.ThangLongUniversityWeb.dto.response.ExamSeatAssignmentResponse;
import com.example.ThangLongUniversityWeb.dto.response.ExamSessionResponse;
import com.example.ThangLongUniversityWeb.dto.response.StudentExamResponse;
import com.example.ThangLongUniversityWeb.entity.*;
import com.example.ThangLongUniversityWeb.enums.CourseStudyStatus;
import com.example.ThangLongUniversityWeb.enums.EnrollmentStatus;
import com.example.ThangLongUniversityWeb.enums.ExamType;
import com.example.ThangLongUniversityWeb.enums.AttendanceStatus;
import com.example.ThangLongUniversityWeb.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.ThangLongUniversityWeb.dto.response.ExamConflictResponse;

@Service
@RequiredArgsConstructor
public class ExamSessionService {
    private final ExamSessionRepository examSessionRepository;
    private final ExamRoomAssignmentRepository examRoomAssignmentRepository;
    private final ExamSeatAssignmentRepository examSeatAssignmentRepository;
    private final SemesterRepository semesterRepository;
    private final CourseRepository courseRepository;
    private final RoomRepository roomRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ExamRegistrationRepository examRegistrationRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final TeacherRepository teacherRepository;
    private final RetakeClassAssignmentService retakeClassAssignmentService;

    @Transactional(readOnly = true)
    public List<ExamSessionResponse> listSessions(Long semesterId) {
        return examSessionRepository.findBySemesterIdOrderByExamAtAsc(semesterId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ExamSessionResponse saveSession(Long semesterId, ExamSessionRequest request) {
        if (request == null || request.getCourseId() == null || request.getExamAt() == null
                || request.getRoomIds() == null || request.getRoomIds().isEmpty()) {
            throw new RuntimeException("Can chon mon hoc, thoi gian thi va it nhat mot phong thi.");
        }

        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay hoc ky."));
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Khong tim thay mon hoc."));

        String selection = request.getCandidateSelection();
        if (selection == null || selection.isBlank()) {
            selection = "ALL";
        }

        ExamSession session = examSessionRepository
                .findBySemesterIdAndCourseIdAndExamTypeAndCandidateSelection(semesterId, course.getId(), ExamType.NORMAL, selection)
                .orElseGet(ExamSession::new);
        session.setSemester(semester);
        session.setCourse(course);
        session.setExamType(ExamType.NORMAL);
        session.setExamAt(request.getExamAt());
        session.setCandidateSelection(selection);
        session = examSessionRepository.save(session);

        examSeatAssignmentRepository.deleteByExamSessionId(session.getId());
        examRoomAssignmentRepository.deleteByExamSessionId(session.getId());

        List<Room> rooms = request.getRoomIds().stream()
                .distinct()
                .map(roomId -> roomRepository.findById(roomId)
                        .orElseThrow(() -> new RuntimeException("Khong tim thay phong thi id=" + roomId)))
                .toList();
        int totalCapacity = rooms.stream().mapToInt(room -> room.getCapacity() != null ? room.getCapacity() : 0).sum();
        List<ExamCandidate> candidates = collectCandidates(semesterId, course.getId(), selection);
        if (totalCapacity < candidates.size()) {
            throw new RuntimeException("Tong suc chua phong thi (" + totalCapacity + ") khong du cho " + candidates.size() + " sinh vien.");
        }

        List<ExamRoomAssignment> assignments = new ArrayList<>();
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            ExamRoomAssignment assignment = new ExamRoomAssignment();
            assignment.setExamSession(session);
            assignment.setRoom(room);
            assignment.setCapacity(room.getCapacity() != null ? room.getCapacity() : 0);
            if (request.getProctorIds() != null && i < request.getProctorIds().size()) {
                Long proctorId = request.getProctorIds().get(i);
                if (proctorId != null) {
                    Teacher proctor = teacherRepository.findById(proctorId)
                            .orElseThrow(() -> new RuntimeException("Khong tim thay giang vien coi thi id=" + proctorId));
                    assignment.setProctor(proctor);
                }
            }
            assignments.add(examRoomAssignmentRepository.save(assignment));
        }

        int[] targetCounts = new int[assignments.size()];
        if ("BALANCED".equalsIgnoreCase(request.getAllocationMethod())) {
            int totalCandidates = candidates.size();
            int allocated = 0;
            for (int i = 0; i < assignments.size(); i++) {
                int cap = assignments.get(i).getCapacity();
                targetCounts[i] = (int) Math.floor((double) totalCandidates * cap / totalCapacity);
                allocated += targetCounts[i];
            }
            int remaining = totalCandidates - allocated;
            int i = 0;
            while (remaining > 0) {
                if (targetCounts[i] < assignments.get(i).getCapacity()) {
                    targetCounts[i]++;
                    remaining--;
                }
                i = (i + 1) % assignments.size();
            }
        } else {
            int totalCandidates = candidates.size();
            int allocated = 0;
            for (int i = 0; i < assignments.size(); i++) {
                int cap = assignments.get(i).getCapacity();
                int toAllocate = Math.min(totalCandidates - allocated, cap);
                targetCounts[i] = toAllocate;
                allocated += toAllocate;
            }
        }

        int roomIndex = 0;
        int usedInRoom = 0;
        for (ExamCandidate candidate : candidates) {
            while (roomIndex < assignments.size() && usedInRoom >= targetCounts[roomIndex]) {
                roomIndex++;
                usedInRoom = 0;
            }
            if (roomIndex >= assignments.size()) {
                throw new RuntimeException("Khong du phong thi de xep sinh vien.");
            }
            ExamSeatAssignment seat = new ExamSeatAssignment();
            seat.setExamSession(session);
            seat.setRoomAssignment(assignments.get(roomIndex));
            seat.setStudent(candidate.student());
            seat.setEnrollment(candidate.enrollment());
            seat.setExamRegistration(candidate.examRegistration());
            seat.setSourceType(candidate.sourceType());
            examSeatAssignmentRepository.save(seat);
            usedInRoom++;
        }

        List<com.example.ThangLongUniversityWeb.entity.ExamRegistration> retakeRegistrations =
                examRegistrationRepository.findBySemesterIdAndCourseIdAndStatus(
                        semesterId, course.getId(), EnrollmentStatus.REGISTERED);

        RetakeClassAssignmentService.AssignmentResult assignmentResult =
                retakeClassAssignmentService.syncAssignmentsAfterSeatAllocation(
                        session, assignments, retakeRegistrations);

        ExamSessionResponse response = toResponse(session);
        response.setAssignedRetakeCount(assignmentResult.assignedCount());
        response.setVirtualClassCode(assignmentResult.virtualClassCode());
        response.setVirtualClassSectionId(assignmentResult.virtualClassSectionId());
        response.setAssignmentWarnings(assignmentResult.warnings());
        return response;
    }

    @Transactional(readOnly = true)
    public List<ExamConflictResponse> validateConflicts(Long semesterId, ExamSessionRequest request) {
        if (request == null || request.getCourseId() == null || request.getExamAt() == null) {
            return List.of();
        }

        String selection = request.getCandidateSelection();
        if (selection == null || selection.isBlank()) {
            selection = "ALL";
        }

        List<ExamCandidate> candidates = collectCandidates(semesterId, request.getCourseId(), selection);
        if (candidates.isEmpty()) {
            return List.of();
        }

        List<ExamSession> overlappingSessions = examSessionRepository
                .findBySemesterIdAndExamAt(semesterId, request.getExamAt())
                .stream()
                .filter(session -> !session.getCourse().getId().equals(request.getCourseId()))
                .toList();

        if (overlappingSessions.isEmpty()) {
            return List.of();
        }

        List<ExamConflictResponse> conflicts = new ArrayList<>();

        for (ExamSession session : overlappingSessions) {
            List<ExamSeatAssignment> assignments = examSeatAssignmentRepository
                    .findByExamSessionIdOrderByRoomAssignmentRoomNameAscStudentStudentCodeAsc(session.getId());
            if (assignments.isEmpty()) {
                continue;
            }

            Map<Long, ExamSeatAssignment> assignedStudents = new LinkedHashMap<>();
            for (ExamSeatAssignment assignment : assignments) {
                assignedStudents.put(assignment.getStudent().getId(), assignment);
            }

            for (ExamCandidate candidate : candidates) {
                Long studentId = candidate.student().getId();
                if (assignedStudents.containsKey(studentId)) {
                    conflicts.add(ExamConflictResponse.builder()
                            .studentCode(candidate.student().getStudentCode())
                            .studentName(candidate.student().getFullName())
                            .conflictingCourseCode(session.getCourse().getCode())
                            .conflictingCourseName(session.getCourse().getName())
                            .build());
                }
            }
        }

        return conflicts;
    }

    @Transactional(readOnly = true)
    public List<ExamSeatAssignmentResponse> listSeats(Long examSessionId) {
        return examSeatAssignmentRepository.findByExamSessionIdOrderByRoomAssignmentRoomNameAscStudentStudentCodeAsc(examSessionId)
                .stream()
                .map(this::toSeatResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<com.example.ThangLongUniversityWeb.dto.response.ExamCandidateResponse> getCandidates(Long semesterId, Long courseId, String candidateSelection) {
        return collectCandidates(semesterId, courseId, candidateSelection).stream()
                .map(c -> {
                    String classCode = null;
                    if (c.enrollment() != null && c.enrollment().getClassSection() != null) {
                        classCode = c.enrollment().getClassSection().getClassCode();
                    } else if (c.examRegistration() != null && c.examRegistration().getClassSection() != null) {
                        classCode = c.examRegistration().getClassSection().getClassCode();
                    }
                    return com.example.ThangLongUniversityWeb.dto.response.ExamCandidateResponse.builder()
                            .studentId(c.student().getId())
                            .studentCode(c.student().getStudentCode())
                            .studentName(c.student().getFullName())
                            .sourceType(c.sourceType())
                            .classCode(classCode)
                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StudentExamResponse> getStudentExams(Long studentId, Long semesterId) {
        return examSeatAssignmentRepository.findByStudentIdAndExamSessionSemesterIdOrderByExamSessionExamAtAsc(studentId, semesterId)
                .stream()
                .map(seat -> new StudentExamResponse(
                        seat.getEnrollment() != null ? seat.getEnrollment().getClassSection().getClassCode()
                                : (seat.getExamRegistration() != null && seat.getExamRegistration().getClassSection() != null
                                ? seat.getExamRegistration().getClassSection().getClassCode()
                                : seat.getSourceType()),
                        seat.getSourceType() != null ? seat.getSourceType() : "NORMAL",
                        seat.getExamSession().getCourse().getName(),
                        seat.getExamSession().getCourse().getCredits(),
                        seat.getExamSession().getExamAt(),
                        seat.getRoomAssignment().getRoom().getName()
                ))
                .toList();
    }

    private boolean isEligibleForExam(Enrollment e) {
        Grade grade = e.getGrade();
        if (grade == null || grade.getParticipationScore() == null || grade.getMidtermScore() == null) {
            return false;
        }
        float preFinalAvg = grade.getParticipationScore() * 0.25f + grade.getMidtermScore() * 0.75f;
        long absences = attendanceRecordRepository.countByEnrollmentIdAndStatus(e.getId(), AttendanceStatus.ABSENT);
        return preFinalAvg >= 4.0f && absences <= 3;
    }

    private List<ExamCandidate> collectCandidates(Long semesterId, Long courseId, String candidateSelection) {
        Map<Long, ExamCandidate> byStudent = new LinkedHashMap<>();
        
        boolean includeNormal = candidateSelection == null || "ALL".equalsIgnoreCase(candidateSelection) || "NORMAL_ONLY".equalsIgnoreCase(candidateSelection);
        boolean includeRetake = candidateSelection == null || "ALL".equalsIgnoreCase(candidateSelection) || "RETAKE_ONLY".equalsIgnoreCase(candidateSelection);

        // 1. First-time students
        if (includeNormal) {
            enrollmentRepository.findByClassSectionSemesterIdAndStatus(semesterId, EnrollmentStatus.REGISTERED)
                    .stream()
                    .filter(e -> e.getClassSection().getCourse().getId().equals(courseId))
                    .filter(this::isEligibleForExam)
                    .forEach(e -> byStudent.putIfAbsent(e.getStudent().getId(), new ExamCandidate(e.getStudent(), e, null, "NORMAL")));
        }

        // 2. Retake & Improve students
        if (includeRetake) {
            examRegistrationRepository.findBySemesterIdAndCourseIdAndStatus(semesterId, courseId, EnrollmentStatus.REGISTERED)
                    .stream()
                    .filter(r -> r.getRegistrationType() != null)
                    .forEach(r -> {
                        String type = r.getRegistrationType().name();
                        byStudent.putIfAbsent(r.getStudent().getId(), new ExamCandidate(r.getStudent(), null, r, type));
                    });
        }

        List<ExamCandidate> list = new ArrayList<>(byStudent.values());
        list.sort(Comparator.comparing(c -> c.student().getStudentCode()));
        return list;
    }

    private ExamSessionResponse toResponse(ExamSession session) {
        List<ExamSeatAssignment> seats = examSeatAssignmentRepository
                .findByExamSessionIdOrderByRoomAssignmentRoomNameAscStudentStudentCodeAsc(session.getId());
        Map<Long, Integer> countsByRoomAssignment = new LinkedHashMap<>();
        for (ExamSeatAssignment seat : seats) {
            countsByRoomAssignment.merge(seat.getRoomAssignment().getId(), 1, Integer::sum);
        }
        List<ExamRoomAssignmentResponse> rooms = examRoomAssignmentRepository
                .findByExamSessionIdOrderByRoomNameAsc(session.getId())
                .stream()
                .map(room -> ExamRoomAssignmentResponse.builder()
                        .id(room.getId())
                        .roomId(room.getRoom().getId())
                        .roomName(room.getRoom().getName())
                        .capacity(room.getCapacity())
                        .assignedCount(countsByRoomAssignment.getOrDefault(room.getId(), 0))
                        .proctorId(room.getProctor() != null ? room.getProctor().getId() : null)
                        .proctorCode(room.getProctor() != null ? room.getProctor().getTeacherCode() : null)
                        .proctorName(room.getProctor() != null ? room.getProctor().getFullName() : null)
                        .build())
                .toList();
        return ExamSessionResponse.builder()
                .id(session.getId())
                .semesterId(session.getSemester().getId())
                .semesterName(session.getSemester().getName())
                .courseId(session.getCourse().getId())
                .courseCode(session.getCourse().getCode())
                .courseName(session.getCourse().getName())
                .credits(session.getCourse().getCredits())
                .examType(session.getExamType())
                .examAt(session.getExamAt())
                .studentCount(seats.size())
                .rooms(rooms)
                .candidateSelection(session.getCandidateSelection())
                .build();
    }

    private ExamSeatAssignmentResponse toSeatResponse(ExamSeatAssignment seat) {
        String classCode = null;
        if (seat.getEnrollment() != null && seat.getEnrollment().getClassSection() != null) {
            classCode = seat.getEnrollment().getClassSection().getClassCode();
        } else if (seat.getExamRegistration() != null && seat.getExamRegistration().getClassSection() != null) {
            classCode = seat.getExamRegistration().getClassSection().getClassCode();
        }
        return ExamSeatAssignmentResponse.builder()
                .id(seat.getId())
                .studentId(seat.getStudent().getId())
                .studentCode(seat.getStudent().getStudentCode())
                .studentName(seat.getStudent().getFullName())
                .roomId(seat.getRoomAssignment().getRoom().getId())
                .roomName(seat.getRoomAssignment().getRoom().getName())
                .roomAssignmentId(seat.getRoomAssignment().getId())
                .sourceType(seat.getSourceType())
                .enrollmentId(seat.getEnrollment() != null ? seat.getEnrollment().getId() : null)
                .examRegistrationId(seat.getExamRegistration() != null ? seat.getExamRegistration().getId() : null)
                .classCode(classCode)
                .build();
    }

    @Transactional
    public void moveSeat(Long seatId, Long targetRoomAssignmentId) {
        ExamSeatAssignment seat = examSeatAssignmentRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chỗ ngồi thi id=" + seatId));
        ExamRoomAssignment targetRoom = examRoomAssignmentRepository.findById(targetRoomAssignmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng thi id=" + targetRoomAssignmentId));

        if (!seat.getExamSession().getId().equals(targetRoom.getExamSession().getId())) {
            throw new RuntimeException("Phòng thi đích phải thuộc cùng một ca thi.");
        }

        // Check capacity of target room
        int currentAssigned = examSeatAssignmentRepository.countByRoomAssignmentId(targetRoomAssignmentId);
        if (currentAssigned >= targetRoom.getCapacity()) {
            throw new RuntimeException("Phòng thi " + targetRoom.getRoom().getName() + " đã hết chỗ (Sĩ số: " + currentAssigned + "/" + targetRoom.getCapacity() + ").");
        }

        seat.setRoomAssignment(targetRoom);
        examSeatAssignmentRepository.save(seat);
    }

    private record ExamCandidate(Student student, Enrollment enrollment, ExamRegistration examRegistration, String sourceType) {
    }
}
