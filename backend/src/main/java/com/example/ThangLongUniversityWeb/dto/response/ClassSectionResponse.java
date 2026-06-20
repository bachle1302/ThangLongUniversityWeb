package com.example.ThangLongUniversityWeb.dto.response;

import com.example.ThangLongUniversityWeb.enums.CourseType;
import com.example.ThangLongUniversityWeb.enums.ExamType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin lớp học")
public class ClassSectionResponse {
    @Schema(description = "ID của lớp học", example = "1")
    private Long id;
    
    @Schema(description = "Mã lớp học", example = "IT001.N1")
    private String classCode;

    @Schema(description = "ID môn học", example = "1")
    private Long courseId;
    
    @Schema(description = "Mã môn học", example = "IT001")
    private String courseCode;
    
    @Schema(description = "Tên môn học", example = "Java Core")
    private String courseName;

    @Schema(description = "Ten nganh cua mon hoc", example = "Cong nghe thong tin")
    private String majorName;

    @Schema(description = "Loai mon hoc", example = "REQUIRED")
    private CourseType courseType;

    @Schema(description = "Nhan loai mon hoc", example = "Bat buoc")
    private String courseTypeLabel;
    
    @Schema(description = "Số tín chỉ", example = "3")
    private Integer credits;

    @Schema(description = "ID học kỳ", example = "1")
    private Long semesterId;
    
    @Schema(description = "Tên học kỳ", example = "HK1 2025-2026")
    private String semesterName;

    @Schema(description = "ID dot dang ky", example = "1")
    private Long registrationRoundId;

    @Schema(description = "Ten dot dang ky", example = "Dot 1")
    private String registrationRoundName;

    @Schema(description = "So thu tu dot dang ky", example = "1")
    private Integer registrationRoundNumber;
    
    @Schema(description = "ID giảng viên", example = "1")
    private Long teacherId;
    
    @Schema(description = "Tên giảng viên", example = "Nguyễn Văn A")
    private String teacherName;

    @Schema(description = "Phòng học", example = "A301")
    private String room;
    
    @Schema(description = "ID phòng học", example = "1")
    private Long roomId;
    
    @Schema(description = "Sức chứa phòng", example = "60")
    private Integer roomCapacity;
    
    @Schema(description = "Danh sách lịch học (ngày + tiết)")
    private List<ClassSectionScheduleResponse> schedules;

    @Schema(description = "Sĩ số tối đa", example = "60")
    private Integer maxSlots;
    
    @Schema(description = "Số sinh viên hiện tại", example = "50")
    private Integer currentSlots;

    @Schema(description = "Trang thai lop hoc phan", example = "OPEN", allowableValues = {"DRAFT", "OPEN", "CLOSED", "CANCELLED"})
    private String status;
    
    @Schema(description = "Trạng thái đóng ghi danh", example = "false")
    private boolean isClosed;

    @Schema(description = "Trạng thái khóa điểm", example = "false")
    private boolean gradeLocked;

    @Schema(description = "Thời gian thi", example = "2026-06-01T08:00:00")
    private LocalDateTime examAt;

    @Schema(description = "Phòng thi", example = "A301")
    private String examRoom;

    @Schema(description = "Loại thi", example = "NORMAL")
    private ExamType examType;

    @Schema(description = "ID ca thi nguon (lop ao thi lai/nang)")
    private Long sourceExamSessionId;

    @Schema(description = "Lop hoc phan ao chi de cham thi lai/nang")
    private boolean virtualRetakeClass;

    @Schema(description = "Trạng thái học kỳ đã kết thúc", example = "false")
    private boolean semesterEnded;
}
