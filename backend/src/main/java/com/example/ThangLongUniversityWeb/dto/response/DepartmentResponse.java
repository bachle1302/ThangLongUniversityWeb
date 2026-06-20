package com.example.ThangLongUniversityWeb.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Thông tin khoa/bộ môn")
public class DepartmentResponse {
    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "Mã khoa/bộ môn", example = "CNTT")
    private String departmentCode;

    @Schema(description = "Tên khoa/bộ môn", example = "Khoa Công nghệ thông tin")
    private String name;

    @Schema(description = "Mô tả")
    private String description;

    @Schema(description = "So giang vien thuoc khoa", example = "12")
    private Long teacherCount;

    @Schema(description = "So nganh thuoc khoa", example = "4")
    private Long majorCount;
}
