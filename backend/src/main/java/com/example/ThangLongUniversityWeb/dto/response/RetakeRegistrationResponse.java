package com.example.ThangLongUniversityWeb.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Ket qua dang ky thi lai / thi nang diem")
public class RetakeRegistrationResponse {
    private List<RetakeRegisteredItemResponse> registeredCourses;
    private Long totalFee;
    private int registeredCount;

    public RetakeRegistrationResponse() {
    }

    public RetakeRegistrationResponse(List<RetakeRegisteredItemResponse> registeredCourses, Long totalFee) {
        this.registeredCourses = registeredCourses;
        this.totalFee = totalFee;
        this.registeredCount = registeredCourses != null ? registeredCourses.size() : 0;
    }

    public List<RetakeRegisteredItemResponse> getRegisteredCourses() { return registeredCourses; }
    public void setRegisteredCourses(List<RetakeRegisteredItemResponse> registeredCourses) {
        this.registeredCourses = registeredCourses;
        this.registeredCount = registeredCourses != null ? registeredCourses.size() : 0;
    }
    public Long getTotalFee() { return totalFee; }
    public void setTotalFee(Long totalFee) { this.totalFee = totalFee; }
    public int getRegisteredCount() { return registeredCount; }
    public void setRegisteredCount(int registeredCount) { this.registeredCount = registeredCount; }
}
