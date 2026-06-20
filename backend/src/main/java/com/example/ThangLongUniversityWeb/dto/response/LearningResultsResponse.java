package com.example.ThangLongUniversityWeb.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Ket qua hoc tap tong hop: bang diem + GPA/CPA")
public class LearningResultsResponse {

    @Schema(description = "ID hoc ky dang xem (null = tat ca ky)")
    private Long semesterId;

    @Schema(description = "Ten hoc ky dang xem")
    private String semesterName;

    @Schema(description = "GPA hoc ky hien tai")
    private Float semesterGpa;

    @Schema(description = "CPA tich luy toan bo")
    private Float cumulativeGpa;

    @Schema(description = "Tong tin chi co diem trong ky")
    private Integer semesterCredits;

    @Schema(description = "Tong tin chi tich luy")
    private Integer cumulativeCredits;

    @Schema(description = "Bang diem chi tiet")
    private List<GradeResponse> grades;

    @Schema(description = "Lich su GPA/CPA qua cac hoc ky")
    private List<SemesterGpaSummary> semesterSummaries;

    @Data
    @Builder
    @Schema(description = "Tom tat GPA mot hoc ky")
    public static class SemesterGpaSummary {
        private Long semesterId;
        private String semesterName;
        private Float semesterGpa;
        private Float cumulativeGpa;
        private Integer totalCredits;
        private Integer cumulativeCredits;
    }
}
