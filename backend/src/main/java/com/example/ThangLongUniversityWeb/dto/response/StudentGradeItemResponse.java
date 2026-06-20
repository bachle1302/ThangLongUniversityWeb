package com.example.ThangLongUniversityWeb.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Thông tin chi tiết điểm một môn học")
public class StudentGradeItemResponse {
    @Schema(description = "ID ghi danh", example = "1")
    private Long enrollmentId;
    
    @Schema(description = "ID học kỳ", example = "1")
    private Long semesterId;
    
    @Schema(description = "Tên học kỳ", example = "HK1 2025-2026")
    private String semesterName;
    
    @Schema(description = "Mã lớp học", example = "IT001.N1")
    private String classCode;
    
    @Schema(description = "Tên môn học", example = "Java Core Programming")
    private String courseName;
    
    @Schema(description = "Số tín chỉ", example = "3")
    private Integer credits;
    
    @Schema(description = "Tổng điểm (thang 10)", example = "8.0")
    private Float totalScore;
    
    @Schema(description = "Điểm hệ 4", example = "3.5")
    private Double gradePoint;
}

