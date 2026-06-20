package com.example.ThangLongUniversityWeb.controller;

import com.example.ThangLongUniversityWeb.dto.request.RoomRequest;
import com.example.ThangLongUniversityWeb.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/rooms")
@RequiredArgsConstructor
@Tag(name = "Admin - Quản lý Phòng học", description = "Quản lý phòng học và sức chứa")
@SecurityRequirement(name = "bearerAuth")
public class RoomManagementController {

    private final RoomService roomService;

    @Operation(summary = "Lấy danh sách tất cả phòng học")
    @GetMapping
    public ResponseEntity<?> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @Operation(summary = "Thêm mới một phòng học")
    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody RoomRequest request) {
        return ResponseEntity.ok(roomService.createRoom(request));
    }

    @Operation(summary = "Cập nhật thông tin phòng học")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoom(@PathVariable Long id, @RequestBody RoomRequest request) {
        return ResponseEntity.ok(roomService.updateRoom(id, request));
    }

    @Operation(summary = "Xóa một phòng học")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok("Xóa phòng học thành công!");
    }
}
