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
public class TeacherDashboardResponse {
    private UserProfileResponse profile;
    private StudentSemesterResponse currentSemester;
    private List<ClassSectionResponse> classes;
    private List<ClassSectionResponse> todaySchedule;
    private Integer classCount;
    private Integer totalStudents;
    private Integer ungradedClassCount;
    private Integer todayScheduleCount;
}
