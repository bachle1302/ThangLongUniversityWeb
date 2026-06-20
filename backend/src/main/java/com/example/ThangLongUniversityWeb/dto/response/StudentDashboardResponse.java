package com.example.ThangLongUniversityWeb.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Du lieu tong hop cho student dashboard")
public class StudentDashboardResponse {
    private UserProfileResponse profile;
    private StudentSemesterResponse currentSemester;
    private LearningResultsResponse learningResults;
    private StudentGradesSummaryResponse grades;
    private TuitionResponse tuition;
    private List<EnrollmentResponse> schedule;
    private List<EnrollmentResponse> todaySchedule;
    private List<StudentExamResponse> exams;
    private List<StudentExamResponse> upcomingExams;
    private Float semesterGpa;
    private Float cumulativeGpa;
    private Integer registeredCredits;
    private Integer earnedCredits;
    private Integer gradedCourseCount;
    private Integer activeCourseCount;
    private Integer upcomingExamCount;
    private Long tuitionRemaining;
    private String tuitionStatus;
    private String registrationStatus;
}
