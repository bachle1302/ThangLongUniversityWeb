package com.example.ThangLongUniversityWeb.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Thong tin ghi danh mon hoc")
public class EnrollmentResponse {
    @Schema(
            description = "ID cua ghi danh",
            example = "1"
    )
    private Long enrollmentId;

    @Schema(
            description = "ID lop hoc phan",
            example = "1"
    )
    private Long classSectionId;

    @Schema(
            description = "Ma mon hoc",
            example = "IT001"
    )
    private String courseCode;

    @Schema(
            description = "Ma lop hoc",
            example = "IT001.N1"
    )
    private String classCode;

    @Schema(
            description = "Ten mon hoc",
            example = "Java Core Programming"
    )
    private String courseName;

    @Schema(
            description = "So tin chi",
            example = "3"
    )
    private Integer credits;

    @Schema(
            description = "Phong hoc",
            example = "A301"
    )
    private String room;

    @Schema(description = "Danh sach lich hoc cua lop hoc phan")
    private List<ClassSectionScheduleResponse> schedules;

    @Schema(
            description = "Ngay trong tuan (2-8)",
            example = "2"
    )
    private Integer dayOfWeek;

    @Schema(
            description = "Tiet hoc bat dau (1-12)",
            example = "1"
    )
    private Integer startPeriod;

    @Schema(
            description = "Tiet hoc ket thuc (1-12)",
            example = "4"
    )
    private Integer endPeriod;

    @Schema(
            description = "Ten giang vien",
            example = "Thay Nguyen Van B"
    )
    private String teacherName;

    @Schema(
            description = "Ma giang vien",
            example = "GV101"
    )
    private String teacherCode;

    @Schema(
            description = "Email giang vien",
            example = "teacher1@tlu.edu.vn"
    )
    private String teacherEmail;

    @Schema(
            description = "Diem giua ky (0-10)",
            example = "7.5"
    )
    private Float midTermScore;

    @Schema(
            description = "Diem cuoi ky (0-10)",
            example = "8.0"
    )
    private Float finalScore;

    @Schema(
            description = "Diem tong ket",
            example = "7.75"
    )
    private Float totalScore;

    @Schema(
            description = "Trang thai ghi danh",
            example = "REGISTERED",
            allowableValues = {"PENDING", "REGISTERED", "PASSED", "FAILED", "CANCELED"}
    )
    private String status;
}
