package com.example.ThangLongUniversityWeb.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Yêu cầu tạo/cập nhật lớp học")
public class ClassSectionRequest {
    @Schema(
            description = "Mã lớp học",
            example = "IT001.N1",
            required = true
    )
    @NotBlank(message = "Mã lớp học không được để trống")
    private String classCode;
    
    @Schema(
            description = "ID của môn học",
            example = "1",
            required = true
    )
    @NotNull(message = "ID môn học không được để trống")
    private Long courseId;
    
    @Schema(
            description = "ID của học kỳ",
            example = "1",
            required = true
    )
    @NotNull(message = "ID học kỳ không được để trống")
    private Long semesterId;

    @Schema(description = "ID dot dang ky trong hoc ky. Neu bo trong, backend gan vao dot dang mo.", example = "1")
    private Long registrationRoundId;
    
    @Schema(
            description = "ID của giảng viên (có thể null nếu chưa giao)",
            example = "1"
    )
    private Long teacherId;

    @Schema(
            description = "Danh sách lịch học (ngày + tiết + phòng)",
            required = true
    )
    @NotNull(message = "Danh sách lịch học không được để trống")
    @Valid
    private List<ClassSectionScheduleRequest> schedules;
    
    @Schema(
            description = "Sĩ số (số sinh viên tối đa)",
            example = "60",
            required = true,
            minimum = "1"
    )
    @NotNull(message = "Sĩ số không được để trống")
    @Min(value = 1, message = "Sĩ số phải lớn hơn 0")
    private Integer maxSlots;
}
