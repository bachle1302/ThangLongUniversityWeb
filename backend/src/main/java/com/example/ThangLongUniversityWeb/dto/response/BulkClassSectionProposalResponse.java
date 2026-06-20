package com.example.ThangLongUniversityWeb.dto.response;

import com.example.ThangLongUniversityWeb.dto.request.ClassSectionRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkClassSectionProposalResponse {
    private List<ClassSectionRequest> items;
    private List<BulkClassSectionCourseSummaryResponse> summaries;
}
