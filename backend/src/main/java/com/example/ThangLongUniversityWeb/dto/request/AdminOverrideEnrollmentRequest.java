package com.example.ThangLongUniversityWeb.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu admin ghi danh sinh viên vào lớp học")
public class AdminOverrideEnrollmentRequest {
    @Schema(
            description = "ID của sinh viên",
            example = "1",
            required = true
    )
    @NotNull(message = "ID sinh viên không được để trống")
    private Long studentId;
    
    @Schema(
            description = "ID của lớp học",
            example = "1",
            required = true
    )
    @NotNull(message = "ID lớp học không được để trống")
    private Long classSectionId;
    
    @Schema(
            description = "Ghi chú của admin",
            example = "Ghi danh vào lớp theo yêu cầu của sinh viên"
    )
    private String note;
}

