package com.example.ThangLongUniversityWeb.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu tạo/cập nhật khoa/bộ môn")
public class DepartmentRequest {
    @Schema(description = "Mã khoa/bộ môn", example = "CNTT", required = true)
    @NotBlank(message = "Mã khoa không được để trống")
    private String departmentCode;

    @Schema(description = "Tên khoa/bộ môn", example = "Khoa Công nghệ thông tin", required = true)
    @NotBlank(message = "Tên khoa không được để trống")
    private String name;

    @Schema(description = "Mô tả", example = "Khoa đào tạo các ngành CNTT")
    private String description;
}
