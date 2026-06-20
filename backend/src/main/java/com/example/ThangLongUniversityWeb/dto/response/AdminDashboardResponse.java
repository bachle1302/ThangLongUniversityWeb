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
public class AdminDashboardResponse {
    private StudentSemesterResponse currentSemester;
    private Long studentCount;
    private Long teacherCount;
    private Long courseCount;
    private Long departmentCount;
    private Long roomCount;
    private Long roomCapacity;
    private Long classSectionCount;
    private Long openClassCount;
    private Long assignedClassCount;
    private Long scheduledClassCount;
    private Long totalRegisteredSlots;
    private Long pendingEnrollmentCount;
    private Long registeredEnrollmentCount;
    private Long totalCapacity;
    private Long totalCourseCredits;
    private Double averageOccupancy;
    private Double assignedTeacherRate;
    private Double scheduledClassRate;
    private List<MajorStudentCount> studentsByMajor;
    private List<ClassSectionResponse> attentionClasses;
    private List<ClassSectionResponse> recentClasses;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MajorStudentCount {
        private Long majorId;
        private String majorCode;
        private String majorName;
        private Long studentCount;
    }
}
