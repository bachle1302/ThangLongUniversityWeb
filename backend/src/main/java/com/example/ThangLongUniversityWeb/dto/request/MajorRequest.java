package com.example.ThangLongUniversityWeb.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu tạo/cập nhật ngành học")
public class MajorRequest {
    @Schema(
            description = "Mã ngành học",
            example = "CNTT",
            required = true,
            minLength = 1,
            maxLength = 10
    )
    @NotBlank(message = "Mã ngành học không được để trống")
    @Size(min = 1, max = 10, message = "Mã ngành học phải từ 1-10 ký tự")
    private String majorCode;
    
    @Schema(
            description = "Tên ngành học",
            example = "Công nghệ thông tin",
            required = true,
            minLength = 2,
            maxLength = 200
    )
    @NotBlank(message = "Tên ngành học không được để trống")
    @Size(min = 2, max = 200, message = "Tên ngành học phải từ 2-200 ký tự")
    private String name;
    
    @Schema(
            description = "Mô tả ngành học",
            example = "Chuyên ngành đào tạo về công nghệ thông tin...",
            maxLength = 1000
    )
    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;

    @Schema(
            description = "ID khoa/bo mon quan ly nganh",
            example = "1"
    )
    private Long departmentId;
}
