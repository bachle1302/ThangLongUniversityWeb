package com.example.ThangLongUniversityWeb.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Thông tin ngành học")
public class MajorResponse {
    @Schema(
            description = "ID ngành học",
            example = "1"
    )
    private Long id;
    
    @Schema(
            description = "Mã ngành học",
            example = "CNTT"
    )
    private String majorCode;
    
    @Schema(
            description = "Tên ngành học",
            example = "Công nghệ thông tin"
    )
    private String name;
    
    @Schema(
            description = "Mô tả ngành học",
            example = "Chuyên ngành đào tạo về công nghệ thông tin..."
    )
    private String description;

    @Schema(
            description = "So sinh vien thuoc nganh",
            example = "120"
    )
    private Long studentCount;

    @Schema(
            description = "So mon hoc thuoc nganh",
            example = "36"
    )
    private Long courseCount;

    @Schema(
            description = "ID khoa/bo mon quan ly nganh",
            example = "1"
    )
    private Long departmentId;

    @Schema(
            description = "Ten khoa/bo mon quan ly nganh",
            example = "Khoa Cong nghe thong tin"
    )
    private String departmentName;
}
