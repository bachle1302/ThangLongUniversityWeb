package com.example.ThangLongUniversityWeb.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkClassSectionValidationItemResponse {
    private Integer index;
    private String classCode;
    private ClassSectionValidationResponse validation;
}
