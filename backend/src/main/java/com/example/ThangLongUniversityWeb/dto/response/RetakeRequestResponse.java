package com.example.ThangLongUniversityWeb.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dang ky thi lai / thi nang diem da ghi nhan")
public class RetakeRequestResponse {
    private Long enrollmentId;
    private Long classSectionId;
    private String classCode;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private Long semesterId;
    private String semesterName;
    private String status;
    private String enrollmentType;
    private Integer attemptNumber;
    private Float totalScore;

    public RetakeRequestResponse() {
    }

    public RetakeRequestResponse(Long enrollmentId, Long classSectionId, String classCode, Long courseId,
                                 String courseCode, String courseName, Long semesterId, String semesterName,
                                 String status, String enrollmentType, Integer attemptNumber, Float totalScore) {
        this.enrollmentId = enrollmentId;
        this.classSectionId = classSectionId;
        this.classCode = classCode;
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.semesterId = semesterId;
        this.semesterName = semesterName;
        this.status = status;
        this.enrollmentType = enrollmentType;
        this.attemptNumber = attemptNumber;
        this.totalScore = totalScore;
    }

    public Long getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(Long enrollmentId) { this.enrollmentId = enrollmentId; }
    public Long getClassSectionId() { return classSectionId; }
    public void setClassSectionId(Long classSectionId) { this.classSectionId = classSectionId; }
    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public Long getSemesterId() { return semesterId; }
    public void setSemesterId(Long semesterId) { this.semesterId = semesterId; }
    public String getSemesterName() { return semesterName; }
    public void setSemesterName(String semesterName) { this.semesterName = semesterName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getEnrollmentType() { return enrollmentType; }
    public void setEnrollmentType(String enrollmentType) { this.enrollmentType = enrollmentType; }
    public Integer getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(Integer attemptNumber) { this.attemptNumber = attemptNumber; }
    public Float getTotalScore() { return totalScore; }
    public void setTotalScore(Float totalScore) { this.totalScore = totalScore; }
}
