package com.example.ThangLongUniversityWeb.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Chi tiet mot dong trong hoa don hoc phi")
public class TuitionItemResponse {
    private String feeType;
    private String courseCode;
    private String courseName;
    private Integer credits;
    private Long pricePerCredit;
    private Long subtotal;

    public TuitionItemResponse() {
    }

    public TuitionItemResponse(String courseCode, String courseName, Integer credits,
                                Long pricePerCredit, Long subtotal) {
        this("COURSE", courseCode, courseName, credits, pricePerCredit, subtotal);
    }

    public TuitionItemResponse(String feeType, String courseCode, String courseName, Integer credits,
                                Long pricePerCredit, Long subtotal) {
        this.feeType = feeType;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credits = credits;
        this.pricePerCredit = pricePerCredit;
        this.subtotal = subtotal;
    }

    public String getFeeType() { return feeType; }
    public void setFeeType(String feeType) { this.feeType = feeType; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }
    public Long getPricePerCredit() { return pricePerCredit; }
    public void setPricePerCredit(Long pricePerCredit) { this.pricePerCredit = pricePerCredit; }
    public Long getSubtotal() { return subtotal; }
    public void setSubtotal(Long subtotal) { this.subtotal = subtotal; }
}
