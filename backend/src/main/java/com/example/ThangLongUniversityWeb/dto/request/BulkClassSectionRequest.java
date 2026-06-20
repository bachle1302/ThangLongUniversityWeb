package com.example.ThangLongUniversityWeb.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkClassSectionRequest {
    @Valid
    @NotEmpty
    private List<ClassSectionRequest> items;
}
