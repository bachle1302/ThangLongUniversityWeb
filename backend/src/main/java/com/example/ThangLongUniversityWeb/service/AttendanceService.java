package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.dto.request.AttendanceRecordRequest;
import com.example.ThangLongUniversityWeb.dto.response.AttendanceRecordResponse;
import com.example.ThangLongUniversityWeb.dto.response.AttendanceSessionResponse;
import com.example.ThangLongUniversityWeb.entity.*;
import com.example.ThangLongUniversityWeb.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;
    private final ClassSectionRepository classSectionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseOutcomeService courseOutcomeService;

    // ─────────────────────────────────────────────────────────────────────────
    // GET all sessions of a class
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<AttendanceSessionResponse> getSessions(Long classSectionId) {
        return sessionRepository.findByClassSectionIdOrderBySessionNumber(classSectionId).stream()
                .map(this::toSessionResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET one session (create lazily if not exists)
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional
    public AttendanceSessionResponse getOrCreateSession(Long classSectionId, Integer sessionNumber) {
        AttendanceSession session = sessionRepository
                .findByClassSectionIdAndSessionNumber(classSectionId, sessionNumber)
                .orElseGet(() -> createEmptySession(classSectionId, sessionNumber));
        return toSessionResponse(session);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT bulk attendance records for a session
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional
    public AttendanceSessionResponse saveRecords(Long classSectionId, Integer sessionNumber,
                                                  List<AttendanceRecordRequest> requests) {
        AttendanceSession session = sessionRepository
                .findByClassSectionIdAndSessionNumber(classSectionId, sessionNumber)
                .orElseGet(() -> createEmptySession(classSectionId, sessionNumber));

        if (session.isLocked()) {
            throw new RuntimeException("Buổi điểm danh này đã được khoá, không thể sửa.");
        }

        // Build lookup map: enrollmentId → existing record
        Map<Long, AttendanceRecord> existing = session.getRecords().stream()
                .collect(Collectors.toMap(r -> r.getEnrollment().getId(), Function.identity()));

        for (AttendanceRecordRequest req : requests) {
            Enrollment enrollment = enrollmentRepository.findById(req.getEnrollmentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy enrollment " + req.getEnrollmentId()));

            AttendanceRecord record = existing.computeIfAbsent(req.getEnrollmentId(), id -> {
                AttendanceRecord r = new AttendanceRecord();
                r.setAttendanceSession(session);
                r.setEnrollment(enrollment);
                session.getRecords().add(r);
                return r;
            });

            record.setStatus(req.getStatus());
            record.setNote(req.getNote());
        }

        AttendanceSession saved = sessionRepository.save(session);

        // Sau khi lưu, tính lại courseStatus cho từng enrollment liên quan
        saved.getRecords().stream()
                .map(AttendanceRecord::getEnrollment)
                .distinct()
                .forEach(courseOutcomeService::recalculate);

        return toSessionResponse(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOCK a session
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional
    public AttendanceSessionResponse lockSession(Long classSectionId, Integer sessionNumber) {
        AttendanceSession session = sessionRepository
                .findByClassSectionIdAndSessionNumber(classSectionId, sessionNumber)
                .orElseGet(() -> createEmptySession(classSectionId, sessionNumber));
        session.setLocked(true);
        return toSessionResponse(sessionRepository.save(session));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private AttendanceSession createEmptySession(Long classSectionId, Integer sessionNumber) {
        ClassSection classSection = classSectionRepository.findById(classSectionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học phần " + classSectionId));
        AttendanceSession s = new AttendanceSession();
        s.setClassSection(classSection);
        s.setSessionNumber(sessionNumber);
        return sessionRepository.save(s);
    }

    private AttendanceSessionResponse toSessionResponse(AttendanceSession session) {
        List<AttendanceRecordResponse> recordResponses = session.getRecords().stream()
                .map(this::toRecordResponse)
                .collect(Collectors.toList());
        return AttendanceSessionResponse.builder()
                .id(session.getId())
                .classSectionId(session.getClassSection().getId())
                .sessionNumber(session.getSessionNumber())
                .weekNumber(session.getWeekNumber())
                .meetingIndex(session.getMeetingIndex())
                .sessionDate(session.getSessionDate())
                .locked(session.isLocked())
                .records(recordResponses)
                .build();
    }

    private AttendanceRecordResponse toRecordResponse(AttendanceRecord record) {
        Student student = record.getEnrollment().getStudent();
        return AttendanceRecordResponse.builder()
                .id(record.getId())
                .enrollmentId(record.getEnrollment().getId())
                .studentCode(student.getStudentCode())
                .studentName(student.getFullName())
                .status(record.getStatus())
                .note(record.getNote())
                .build();
    }
}
