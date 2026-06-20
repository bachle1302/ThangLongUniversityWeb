package com.example.ThangLongUniversityWeb.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Yêu cầu thêm/gỡ sinh viên vào lớp hành chính")
public class HomeroomStudentsRequest {
    @Schema(description = "Danh sách ID sinh viên", example = "[1, 2, 3, 5]", required = true)
    @NotEmpty(message = "Danh sách sinh viên không được rỗng")
    private List<Long> studentIds;
}
