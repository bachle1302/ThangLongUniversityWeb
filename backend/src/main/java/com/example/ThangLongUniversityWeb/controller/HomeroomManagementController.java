package com.example.ThangLongUniversityWeb.controller;

import com.example.ThangLongUniversityWeb.dto.request.HomeroomRequest;
import com.example.ThangLongUniversityWeb.dto.request.HomeroomStudentsRequest;
import com.example.ThangLongUniversityWeb.service.HomeroomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/homerooms")
@RequiredArgsConstructor
@Tag(name = "Admin - Quản lý Lớp hành chính", description = "Quản lý lớp hành chính, cố vấn học tập và sinh viên trong lớp")
@SecurityRequirement(name = "bearerAuth")
public class HomeroomManagementController {

    private final HomeroomService homeroomService;

    @Operation(summary = "Lấy danh sách tất cả lớp hành chính")
    @GetMapping
    public ResponseEntity<?> getAllHomerooms() {
        return ResponseEntity.ok(homeroomService.getAllHomerooms());
    }

    @Operation(summary = "Thêm mới một lớp hành chính")
    @PostMapping
    public ResponseEntity<?> createHomeroom(@RequestBody HomeroomRequest request) {
        return ResponseEntity.ok(homeroomService.createHomeroom(request));
    }

    @Operation(summary = "Cập nhật thông tin lớp hành chính")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateHomeroom(@PathVariable Long id, @RequestBody HomeroomRequest request) {
        return ResponseEntity.ok(homeroomService.updateHomeroom(id, request));
    }

    @Operation(summary = "Xóa lớp hành chính")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHomeroom(@PathVariable Long id) {
        homeroomService.deleteHomeroom(id);
        return ResponseEntity.ok("Xóa lớp hành chính thành công!");
    }

    @Operation(summary = "Lấy danh sách sinh viên trong lớp")
    @GetMapping("/{id}/students")
    public ResponseEntity<?> getStudentsByHomeroom(@PathVariable Long id) {
        return ResponseEntity.ok(homeroomService.getStudentsByHomeroom(id));
    }

    @Operation(summary = "Thêm nhiều sinh viên vào lớp")
    @PostMapping("/{id}/students")
    public ResponseEntity<?> addStudentsToHomeroom(
            @PathVariable Long id,
            @RequestBody HomeroomStudentsRequest request) {
        homeroomService.addStudentsToHomeroom(id, request);
        return ResponseEntity.ok("Thêm sinh viên vào lớp thành công!");
    }

    @Operation(summary = "Gỡ sinh viên khỏi lớp")
    @DeleteMapping("/{id}/students/{studentId}")
    public ResponseEntity<?> removeStudentFromHomeroom(
            @PathVariable Long id,
            @PathVariable Long studentId) {
        homeroomService.removeStudentFromHomeroom(id, studentId);
        return ResponseEntity.ok("Gỡ sinh viên khỏi lớp thành công!");
    }
}
