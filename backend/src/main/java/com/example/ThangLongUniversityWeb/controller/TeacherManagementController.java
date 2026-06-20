package com.example.ThangLongUniversityWeb.controller;

import com.example.ThangLongUniversityWeb.dto.request.TeacherRequest;
import com.example.ThangLongUniversityWeb.dto.request.TeacherUpdateRequest;
import com.example.ThangLongUniversityWeb.enums.TeacherStatus;
import com.example.ThangLongUniversityWeb.repository.TeacherRepository;
import com.example.ThangLongUniversityWeb.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/teachers")
@RequiredArgsConstructor
@Tag(name = "Admin - Quản lý Giảng viên")
@SecurityRequirement(name = "bearerAuth")
public class TeacherManagementController {

    private final TeacherService teacherService;
    private final TeacherRepository teacherRepository;

    @Operation(summary = "Lấy danh sách tất cả giảng viên")
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllTeachers() {
        return ResponseEntity.ok(teacherService.getAllTeachers());
    }

    @Operation(summary = "Tim kiem giang vien co phan trang")
    @GetMapping("/search")
    @Transactional(readOnly = true)
    public ResponseEntity<?> searchTeachers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) TeacherStatus status
    ) {
        var pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.ASC, "teacherCode"));
        return ResponseEntity.ok(teacherRepository.searchAdmin(keyword, departmentId, status, pageable)
                .map(teacherService::mapToResponse));
    }

    @Operation(summary = "Thêm mới một giảng viên")
    @PostMapping
    public ResponseEntity<?> createTeacher(@RequestBody TeacherRequest request) {
        return ResponseEntity.ok(teacherService.createTeacher(request));
    }

    @Operation(summary = "Cập nhật thông tin giảng viên")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTeacher(@PathVariable Long id, @RequestBody TeacherUpdateRequest request) {
        return ResponseEntity.ok(teacherService.updateTeacherPartial(id, request));
    }

    @Operation(summary = "Xóa giảng viên")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTeacher(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return ResponseEntity.ok("Xóa giảng viên thành công!");
    }
}
