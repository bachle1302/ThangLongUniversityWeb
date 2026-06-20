package com.example.ThangLongUniversityWeb.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Mot mon hoc da dang ky thi lai thanh cong")
public class RetakeRegisteredItemResponse {
    private Long courseId;
    private String courseCode;
    private String courseName;
    private Integer credits;
    private String registrationType; // RETAKE hoac IMPROVE
    private Integer attemptNumber;
    private Long feeCharged; // phi cho mon nay
    private String examAt;   // lich thi (neu co)
    private String examRoom;

    public RetakeRegisteredItemResponse() {
    }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }
    public String getRegistrationType() { return registrationType; }
    public void setRegistrationType(String registrationType) { this.registrationType = registrationType; }
    public Integer getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(Integer attemptNumber) { this.attemptNumber = attemptNumber; }
    public Long getFeeCharged() { return feeCharged; }
    public void setFeeCharged(Long feeCharged) { this.feeCharged = feeCharged; }
    public String getExamAt() { return examAt; }
    public void setExamAt(String examAt) { this.examAt = examAt; }
    public String getExamRoom() { return examRoom; }
    public void setExamRoom(String examRoom) { this.examRoom = examRoom; }
}
