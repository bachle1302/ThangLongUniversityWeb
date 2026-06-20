package com.example.ThangLongUniversityWeb.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Thông tin điểm số của sinh viên")
public class GradeResponse {
    @Schema(description = "ID", example = "1")
    private Long id;
    
    @Schema(description = "ID ghi danh", example = "1")
    private Long enrollmentId;
    
    @Schema(description = "ID sinh viên", example = "1")
    private Long studentId;
    
    @Schema(description = "Mã sinh viên", example = "SV001")
    private String studentCode;
    
    @Schema(description = "Tên sinh viên", example = "Nguyễn Văn A")
    private String studentName;
    
    @Schema(description = "ID môn học", example = "1")
    private Long courseId;
    
    @Schema(description = "Mã môn học", example = "IT001")
    private String courseCode;

    @Schema(description = "Mã lớp học phần", example = "IT001.N1")
    private String classCode;
    
    @Schema(description = "Tên môn học", example = "Java Core Programming")
    private String courseName;

    @Schema(description = "Số tín chỉ", example = "3")
    private Integer credits;
    
    @Schema(description = "ID học kỳ", example = "1")
    private Long semesterId;
    
    @Schema(description = "Tên học kỳ", example = "HK1 2025-2026")
    private String semesterName;
    
    @Schema(description = "Điểm chuyên cần (0-10)", example = "8.5")
    private Float participationScore;
    
    @Schema(description = "Điểm giữa kỳ (0-10)", example = "7.5")
    private Float midtermScore;
    
    @Schema(description = "Điểm cuối kỳ (0-10)", example = "8.0")
    private Float finalScore;
    
    @Schema(description = "Điểm thi lại / cải thiện", example = "9.0")
    private Float retestScore;
    
    @Schema(description = "Số lần thi", example = "1")
    private Integer attemptNumber;
    
    @Schema(description = "Loại ghi danh", example = "NORMAL")
    private String enrollmentType;
    
    @Schema(description = "Tổng điểm", example = "8.0")
    private Float totalScore;
    
    @Schema(description = "Điểm chữ (A, B, C, D, F)", example = "B")
    private String letterGrade;
    
    @Schema(description = "Điểm hệ 4", example = "3.0")
    private Float gpa4;

    @Schema(description = "Điểm hệ 4 alias cho frontend", example = "3.0")
    private Float gradePoint;
    
    @Schema(description = "Thời gian tạo", example = "2025-12-20T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Thời gian cập nhật", example = "2025-12-20T10:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Trạng thái kết quả học môn", example = "IN_PROGRESS")
    private String courseStatus;

    @Schema(description = "Số buổi vắng", example = "2")
    private Long absenceCount;

    @Schema(description = "ID đăng ký thi lại/nâng (nếu có)")
    private Long examRegistrationId;

    @Schema(description = "Lần thi lại/nâng theo đăng ký")
    private Integer examAttemptNumber;

    @Schema(description = "Học kỳ học môn gốc (khi là dòng thi lại/nâng)")
    private String studySemesterName;
}
