package com.example.ThangLongUniversityWeb.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ket qua validate lop hoc phan truoc khi tao/cap nhat")
public class ClassSectionValidationResponse {
    @Schema(description = "True neu khong co loi chan tao lop")
    private boolean valid;

    @Schema(description = "Danh sach loi chan tao/cap nhat lop")
    private List<ClassSectionValidationIssueResponse> errors;

    @Schema(description = "Danh sach canh bao van cho phep tao/cap nhat lop")
    private List<ClassSectionValidationIssueResponse> warnings;

    @Schema(description = "Danh sach thong tin tham khao, khong anh huong tao/cap nhat lop")
    private List<ClassSectionValidationIssueResponse> infos;
}
