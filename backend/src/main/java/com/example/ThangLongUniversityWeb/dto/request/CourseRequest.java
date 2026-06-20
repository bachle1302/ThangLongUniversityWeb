package com.example.ThangLongUniversityWeb.dto.request;

import com.example.ThangLongUniversityWeb.enums.CourseType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Yêu cầu tạo/cập nhật môn học")
public class CourseRequest {

    @Schema(
            description = "Mã môn học",
            example = "CS101",
            required = true,
            minLength = 2,
            maxLength = 20
    )
    @NotBlank(message = "Mã môn học không được để trống")
    @Size(min = 2, max = 20, message = "Mã môn học phải từ 2-20 ký tự")
    private String code;

    @Schema(
            description = "Tên môn học",
            example = "Java Core Programming",
            required = true,
            minLength = 2,
            maxLength = 200
    )
    @NotBlank(message = "Tên môn học không được để trống")
    @Size(min = 2, max = 200, message = "Tên môn học phải từ 2-200 ký tự")
    private String name;

    @Schema(
            description = "Số tín chỉ",
            example = "3",
            required = true,
            minimum = "1",
            maximum = "10"
    )
    @NotNull(message = "Số tín chỉ không được để trống")
    @Min(value = 1, message = "Số tín chỉ phải lớn hơn 0")
    private Integer credits;

    @Schema(
            description = "Mô tả môn học",
            example = "Khóa học lập trình Java cơ bản cho sinh viên năm 1",
            maxLength = 1000
    )
    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;

    @Schema(description = "Loai mon hoc: REQUIRED = bat buoc, ELECTIVE = tu do", example = "REQUIRED")
    private CourseType courseType = CourseType.REQUIRED;

    @Schema(
            description = "ID ngành học",
            example = "1",
            required = true
    )
    @NotNull(message = "ID ngành học không được để trống")
    private Long majorId;

    @Schema(
            description = "Danh sách ID các môn học tiên quyết",
            example = "[1, 2, 3]"
    )
    private List<Long> prerequisiteCourseIds;
}
