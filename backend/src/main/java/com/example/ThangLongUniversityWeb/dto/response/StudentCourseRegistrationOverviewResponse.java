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
public class StudentCourseRegistrationOverviewResponse {
    private List<StudentSemesterResponse> semesters;
    private StudentSemesterResponse currentSemester;
    private List<ClassSectionResponse> availableClasses;
    private List<EnrollmentResponse> selectedEnrollments;
    private boolean readonly;
    private String registrationStatus;
}
