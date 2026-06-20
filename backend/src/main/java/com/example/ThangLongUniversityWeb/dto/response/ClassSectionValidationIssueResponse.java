package com.example.ThangLongUniversityWeb.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ket qua kiem tra tung rule khi tao/cap nhat lop hoc phan")
public class ClassSectionValidationIssueResponse {
    @Schema(description = "Ma rule", example = "TEACHER_CONFLICT")
    private String code;

    @Schema(description = "Thong diep hien thi cho admin")
    private String message;
}
