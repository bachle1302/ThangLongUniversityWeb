package com.example.ThangLongUniversityWeb.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Mon hoc du dieu kien thi lai hoac thi nang diem")
public class RetakeEligibleCourseResponse {
    private Long gradeId;
    private Long enrollmentId;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private Integer credits;
    private Float previousTotalScore;
    private Integer previousAttemptNumber;
    private String registrationType; // RETAKE hoac IMPROVE
    private Long retakeFee; // phi thi lai co dinh lay tu system_settings

    public RetakeEligibleCourseResponse() {
    }

    public RetakeEligibleCourseResponse(Long gradeId, Long enrollmentId, Long courseId, String courseCode,
                                        String courseName, Integer credits, Float previousTotalScore,
                                        Integer previousAttemptNumber, String registrationType, Long retakeFee) {
        this.gradeId = gradeId;
        this.enrollmentId = enrollmentId;
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credits = credits;
        this.previousTotalScore = previousTotalScore;
        this.previousAttemptNumber = previousAttemptNumber;
        this.registrationType = registrationType;
        this.retakeFee = retakeFee;
    }

    public Long getGradeId() { return gradeId; }
    public void setGradeId(Long gradeId) { this.gradeId = gradeId; }
    public Long getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(Long enrollmentId) { this.enrollmentId = enrollmentId; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }
    public Float getPreviousTotalScore() { return previousTotalScore; }
    public void setPreviousTotalScore(Float previousTotalScore) { this.previousTotalScore = previousTotalScore; }
    public Integer getPreviousAttemptNumber() { return previousAttemptNumber; }
    public void setPreviousAttemptNumber(Integer previousAttemptNumber) { this.previousAttemptNumber = previousAttemptNumber; }
    public String getRegistrationType() { return registrationType; }
    public void setRegistrationType(String registrationType) { this.registrationType = registrationType; }
    public Long getRetakeFee() { return retakeFee; }
    public void setRetakeFee(Long retakeFee) { this.retakeFee = retakeFee; }
}

