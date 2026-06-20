package com.example.ThangLongUniversityWeb.controller;

import com.example.ThangLongUniversityWeb.dto.request.ClassSectionRequest;
import com.example.ThangLongUniversityWeb.dto.request.BulkClassSectionProposalRequest;
import com.example.ThangLongUniversityWeb.dto.request.BulkClassSectionRequest;
import com.example.ThangLongUniversityWeb.dto.request.ExamScheduleRequest;
import com.example.ThangLongUniversityWeb.dto.request.ExamSessionRequest;
import com.example.ThangLongUniversityWeb.enums.ClassSectionStatus;
import com.example.ThangLongUniversityWeb.repository.ClassSectionRepository;
import com.example.ThangLongUniversityWeb.service.ClassSectionService;
import com.example.ThangLongUniversityWeb.service.BulkClassSectionService;
import com.example.ThangLongUniversityWeb.service.DashboardService;
import com.example.ThangLongUniversityWeb.service.ExamSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/class-sections")
@RequiredArgsConstructor
@Tag(name = "Admin - Quản lý Lớp học phần", description = "Mở lớp, xếp phòng, gán giảng viên")
@SecurityRequirement(name = "bearerAuth")
public class ClassSectionManagementController {

    private final ClassSectionService classSectionService;
    private final BulkClassSectionService bulkClassSectionService;
    private final ClassSectionRepository classSectionRepository;
    private final ExamSessionService examSessionService;
    private final DashboardService dashboardService;

    @Operation(summary = "Lấy danh sách TOÀN BỘ lớp học phần (Đã làm phẳng dữ liệu)")
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllClassSections() {
        var responseList = classSectionRepository.findAll()
                .stream()
                .map(classSectionService::mapToResponse) // Dùng hàm mapToResponse để chuyển Entity -> DTO phẳng
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
    }

    @Operation(summary = "Tim kiem lop hoc phan co phan trang")
    @GetMapping("/search")
    @Transactional(readOnly = true)
    public ResponseEntity<?> searchClassSections(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ClassSectionStatus status
    ) {
        var pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "id"));
        return ResponseEntity.ok(classSectionRepository.searchAdmin(semesterId, keyword, status, pageable)
                .map(classSectionService::mapToResponse));
    }

    @Operation(summary = "Lay option cho form mo lop hoc phan")
    @GetMapping("/options")
    public ResponseEntity<?> getClassSectionOptions() {
        return ResponseEntity.ok(dashboardService.getClassSectionOptions());
    }

    @Operation(summary = "Lấy danh sách Lớp học phần THEO HỌC KỲ")
    @GetMapping("/semester/{semesterId}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getClassSectionsBySemester(@PathVariable Long semesterId) {
        var responseList = classSectionRepository.findBySemesterId(semesterId)
                .stream()
                .map(classSectionService::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
    }

    @Operation(summary = "Mở một lớp học phần mới")
    @PostMapping
    public ResponseEntity<?> createClassSection(@Valid @RequestBody ClassSectionRequest request) {
        return ResponseEntity.ok(classSectionService.createClassSection(request));
    }

    @Operation(summary = "Kiểm tra lớp học phần trước khi tạo/cập nhật")
    @PostMapping("/validate")
    public ResponseEntity<?> validateClassSection(
            @Valid @RequestBody ClassSectionRequest request,
            @RequestParam(required = false) Long excludeId
    ) {
        return ResponseEntity.ok(classSectionService.validateClassSection(request, request.getSemesterId(), excludeId));
    }

    @Operation(summary = "Sinh de xuat tao nhieu lop hoc phan khong trung lich")
    @PostMapping("/bulk/proposals")
    public ResponseEntity<?> proposeBulkClassSections(
            @Valid @RequestBody BulkClassSectionProposalRequest request
    ) {
        return ResponseEntity.ok(bulkClassSectionService.propose(request));
    }

    @Operation(summary = "Kiem tra toan bo de xuat tao nhieu lop hoc phan")
    @PostMapping("/bulk/validate")
    public ResponseEntity<?> validateBulkClassSections(
            @Valid @RequestBody BulkClassSectionRequest request
    ) {
        return ResponseEntity.ok(bulkClassSectionService.validate(request));
    }

    @Operation(summary = "Tao nhieu lop hoc phan trong mot transaction")
    @PostMapping("/bulk")
    public ResponseEntity<?> createBulkClassSections(
            @Valid @RequestBody BulkClassSectionRequest request
    ) {
        return ResponseEntity.ok(bulkClassSectionService.create(request));
    }

    @Operation(summary = "Cập nhật lớp học phần (Đổi phòng, đổi giảng viên...)")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateClassSection(@PathVariable Long id, @Valid @RequestBody ClassSectionRequest request) {
        return ResponseEntity.ok(classSectionService.updateClassSection(id, request));
    }

    @Operation(summary = "Xóa lớp học phần")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClassSection(@PathVariable Long id) {
        classSectionService.deleteClassSection(id);
        return ResponseEntity.ok("Xóa lớp học phần thành công!");
    }

    @Operation(summary = "Hủy lớp học phần")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelClassSection(@PathVariable Long id) {
        var section = classSectionService.cancelClassSection(id);
        return ResponseEntity.ok(Map.of(
                "message", "Đã hủy lớp học phần thành công",
                "classSection", section
        ));
    }

    @Operation(summary = "Lấy danh sách sinh viên đăng ký một lớp học phần")
    @GetMapping("/{id}/students")
    public ResponseEntity<?> getClassSectionStudents(@PathVariable Long id) {
        return ResponseEntity.ok(classSectionService.getClassSectionStudents(id));
    }

    @Operation(summary = "Cập nhật lịch thi cho một lớp học phần")
    @PutMapping("/{id}/exam-schedule")
    public ResponseEntity<?> updateExamSchedule(@PathVariable Long id, @RequestBody ExamScheduleRequest request) {
        return ResponseEntity.ok(classSectionService.updateExamSchedule(id, request));
    }

    @Operation(summary = "Cập nhật hàng loạt lịch thi cho các lớp trong một học kỳ")
    @PutMapping("/semester/{semesterId}/exam-schedules")
    public ResponseEntity<?> batchUpdateExamSchedules(
            @PathVariable Long semesterId,
            @RequestBody List<ExamScheduleRequest> requests
    ) {
        return ResponseEntity.ok(classSectionService.batchUpdateExamSchedules(semesterId, requests));
    }

    @Operation(summary = "Lấy danh sách lịch thi theo học kỳ")
    @GetMapping("/semester/{semesterId}/exam-schedules")
    public ResponseEntity<?> getExamSchedules(@PathVariable Long semesterId) {
        return ResponseEntity.ok(classSectionService.getExamSchedulesBySemester(semesterId));
    }

    @Operation(summary = "Lay lich thi theo mon va phong thi")
    @GetMapping("/semester/{semesterId}/exam-sessions")
    public ResponseEntity<?> getExamSessions(@PathVariable Long semesterId) {
        return ResponseEntity.ok(examSessionService.listSessions(semesterId));
    }

    @Operation(summary = "Tao/cap nhat lich thi theo mon va tu dong chia phong")
    @PostMapping("/semester/{semesterId}/exam-sessions")
    public ResponseEntity<?> saveExamSession(
            @PathVariable Long semesterId,
            @RequestBody ExamSessionRequest request
    ) {
        return ResponseEntity.ok(examSessionService.saveSession(semesterId, request));
    }

    @Operation(summary = "Kiem tra trung lich thi cho sinh vien")
    @PostMapping("/semester/{semesterId}/exam-sessions/validate-conflicts")
    public ResponseEntity<?> validateExamConflicts(
            @PathVariable Long semesterId,
            @RequestBody ExamSessionRequest request
    ) {
        return ResponseEntity.ok(examSessionService.validateConflicts(semesterId, request));
    }

    @Operation(summary = "Lay danh sach sinh vien du kien du thi")
    @GetMapping("/semester/{semesterId}/exam-sessions/candidates")
    public ResponseEntity<?> getExamCandidates(
            @PathVariable Long semesterId,
            @RequestParam Long courseId,
            @RequestParam(required = false, defaultValue = "ALL") String candidateSelection
    ) {
        return ResponseEntity.ok(examSessionService.getCandidates(semesterId, courseId, candidateSelection));
    }

    @Operation(summary = "Di chuyen phong thi cho sinh vien")
    @PutMapping("/exam-sessions/seats/{seatId}/move")
    public ResponseEntity<?> moveSeat(
            @PathVariable Long seatId,
            @RequestParam Long targetRoomAssignmentId
    ) {
        examSessionService.moveSeat(seatId, targetRoomAssignmentId);
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lay danh sach sinh vien trong mot lich thi theo mon")
    @GetMapping("/exam-sessions/{examSessionId}/seats")
    public ResponseEntity<?> getExamSessionSeats(@PathVariable Long examSessionId) {
        return ResponseEntity.ok(examSessionService.listSeats(examSessionId));
    }
}
