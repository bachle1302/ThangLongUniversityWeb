package com.example.ThangLongUniversityWeb.dto.response;

import com.example.ThangLongUniversityWeb.enums.CourseType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Course information")
public class CourseResponse {

    @Schema(
            description = "Course ID",
            example = "1"
    )
    private Long id;

    @Schema(
            description = "Course code",
            example = "CS101"
    )
    private String code;

    @Schema(
            description = "Course name",
            example = "Java Core Programming"
    )
    private String name;

    @Schema(
            description = "Course credits",
            example = "3"
    )
    private Integer credits;

    @Schema(
            description = "Course description",
            example = "Basic Java programming course for first year students"
    )
    private String description;

    @Schema(description = "Course type", example = "REQUIRED")
    private CourseType courseType;

    @Schema(description = "Course type display label", example = "Bắt buộc")
    private String courseTypeLabel;

    @Schema(description = "Major ID", example = "1")
    private Long majorId;

    @Schema(
            description = "Major name",
            example = "Computer Science"
    )
    private String majorName;

    @Schema(description = "Department ID owning the course major", example = "1")
    private Long departmentId;

    @Schema(description = "Department name owning the course major", example = "Khoa Toan Tin")
    private String departmentName;

    @Schema(description = "List of prerequisite course IDs", example = "[1, 2]")
    private List<Long> prerequisiteCourseIds;

    @Schema(
            description = "List of prerequisite course names",
            example = "[\"CS100\", \"MATH101\"]"
    )
    private Set<String> prerequisiteNames;
}
