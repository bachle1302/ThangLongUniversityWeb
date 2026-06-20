package com.example.ThangLongUniversityWeb.controller;

import com.example.ThangLongUniversityWeb.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin - Dashboard")
@SecurityRequirement(name = "bearerAuth")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Du lieu tong hop cho admin dashboard")
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(@RequestParam(required = false) Long semesterId) {
        return ResponseEntity.ok(dashboardService.getAdminDashboard(semesterId));
    }
}
