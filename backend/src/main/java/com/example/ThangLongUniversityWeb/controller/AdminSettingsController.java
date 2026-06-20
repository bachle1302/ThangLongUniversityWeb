package com.example.ThangLongUniversityWeb.controller;

import com.example.ThangLongUniversityWeb.entity.SystemSettings;
import com.example.ThangLongUniversityWeb.repository.SystemSettingsRepository;
import com.example.ThangLongUniversityWeb.service.StudentRetakeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
@Tag(name = "Admin - System Settings")
@SecurityRequirement(name = "bearerAuth")
public class AdminSettingsController {

    private final SystemSettingsRepository systemSettingsRepository;

    @Operation(summary = "Lay phi thi lai hien tai (VND)")
    @GetMapping("/retake-fee")
    public ResponseEntity<Map<String, Long>> getRetakeFee() {
        long fee = systemSettingsRepository.findById(StudentRetakeService.KEY_RETAKE_FEE)
                .map(s -> Long.parseLong(s.getValue()))
                .orElse(StudentRetakeService.DEFAULT_RETAKE_FEE);
        return ResponseEntity.ok(Map.of("feePerCourse", fee));
    }

    @Operation(summary = "Cap nhat phi thi lai (VND)")
    @PutMapping("/retake-fee")
    public ResponseEntity<Map<String, Object>> updateRetakeFee(@RequestBody Map<String, Long> body) {
        Long fee = body.get("feePerCourse");
        if (fee == null || fee < 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "feePerCourse phai la so nguyen duong"));
        }
        SystemSettings setting = systemSettingsRepository.findById(StudentRetakeService.KEY_RETAKE_FEE)
                .orElseGet(() -> {
                    SystemSettings s = new SystemSettings();
                    s.setKey(StudentRetakeService.KEY_RETAKE_FEE);
                    s.setDescription("Phi thi lai moi mon (VND)");
                    return s;
                });
        setting.setValue(String.valueOf(fee));
        systemSettingsRepository.save(setting);
        return ResponseEntity.ok(Map.of("feePerCourse", fee, "message", "Cap nhat thanh cong"));
    }
}
