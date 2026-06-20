package com.example.ThangLongUniversityWeb.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BulkClassSectionProposalRequest {
    @NotNull
    private Long semesterId;

    @Valid
    @NotEmpty
    private List<BulkClassSectionCourseRequest> courses;
}
