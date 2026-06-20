package com.example.ThangLongUniversityWeb.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Schema(description = "Thông tin kỳ thi của sinh viên")
public class StudentExamResponse {
    @Schema(description = "Mã lớp học", example = "IT001.N1")
    private String classCode;

    @Schema(description = "Loại thi", example = "NORMAL")
    private String examSourceType;
    
    @Schema(description = "Tên môn học", example = "Java Core Programming")
    private String courseName;
    
    @Schema(description = "Số tín chỉ", example = "3")
    private Integer credits;
    
    @Schema(description = "Thời gian thi")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime examAt;
    
    @Schema(description = "Phòng thi", example = "A301")
    private String examRoom;
}

