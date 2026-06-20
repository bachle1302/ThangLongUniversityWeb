package com.example.ThangLongUniversityWeb.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentRetakeOverviewResponse {
    private List<StudentSemesterResponse> semesters;
    private StudentSemesterResponse currentSemester;
    private List<RetakeEligibleCourseResponse> eligibleCourses;
    private List<RetakeRequestResponse> requests;
    private boolean readonly;
    private String registrationStatus;
}
