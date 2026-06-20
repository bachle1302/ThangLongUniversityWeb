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
public class AdminClassSectionOptionsResponse {
    private List<CourseResponse> courses;
    private List<StudentSemesterResponse> semesters;
    private List<TeacherResponse> teachers;
    private List<RoomResponse> rooms;
    private List<PeriodResponse> periods;
}
