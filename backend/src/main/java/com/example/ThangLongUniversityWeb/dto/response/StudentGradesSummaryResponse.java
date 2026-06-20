package com.example.ThangLongUniversityWeb.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Schema(description = "Tóm tắt điểm số học kỳ và tích lũy")
public class StudentGradesSummaryResponse {
    @Schema(description = "ID học kỳ (nullable nếu chỉ xem tích lũy)", example = "1")
    private Long semesterId;
    
    @Schema(description = "GPA học kỳ", example = "3.5")
    private Double semesterGpa;
    
    @Schema(description = "GPA tích lũy", example = "3.45")
    private Double cumulativeGpa;
    
    @Schema(description = "Danh sách chi tiết điểm các môn học")
    private List<StudentGradeItemResponse> items;
}

