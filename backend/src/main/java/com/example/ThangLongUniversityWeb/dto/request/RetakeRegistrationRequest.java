package com.example.ThangLongUniversityWeb.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Yeu cau dang ky thi lai / thi nang diem")
public class RetakeRegistrationRequest {
    @Schema(description = "ID hoc ky dang ky thi lai/nang diem", example = "2")
    private Long semesterId;

    @Schema(description = "Danh sach ID mon hoc (course) muon dang ky thi lai/nang diem", example = "[1, 2]")
    private List<Long> courseIds;

    public Long getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(Long semesterId) {
        this.semesterId = semesterId;
    }

    public List<Long> getCourseIds() {
        return courseIds;
    }

    public void setCourseIds(List<Long> courseIds) {
        this.courseIds = courseIds;
    }
}
