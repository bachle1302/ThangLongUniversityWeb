package com.example.ThangLongUniversityWeb.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu nhập/cập nhật điểm số sinh viên")
public class GradeRequest {
    @Schema(description = "ID của đơn đăng ký môn học", example = "1", required = true)
    @NotNull(message = "enrollmentId không được để trống")
    private Long enrollmentId;

    @Schema(description = "Điểm chuyên cần (0-10)", example = "8.5")
    @DecimalMin(value = "0.0", message = "Điểm chuyên cần phải >= 0")
    @DecimalMax(value = "10.0", message = "Điểm chuyên cần phải <= 10")
    private Float participationScore;

    @Schema(description = "Điểm giữa kỳ (0-10)", example = "7.5")
    @DecimalMin(value = "0.0", message = "Điểm giữa kỳ phải >= 0")
    @DecimalMax(value = "10.0", message = "Điểm giữa kỳ phải <= 10")
    private Float midTermScore;

    @Schema(description = "Điểm cuối kỳ (0-10)", example = "8.0")
    @DecimalMin(value = "0.0", message = "Điểm cuối kỳ phải >= 0")
    @DecimalMax(value = "10.0", message = "Điểm cuối kỳ phải <= 10")
    private Float finalScore;

    @Schema(description = "Điểm thi lại / cải thiện (0-10)", example = "9.0")
    @DecimalMin(value = "0.0", message = "Điểm thi lại phải >= 0")
    @DecimalMax(value = "10.0", message = "Điểm thi lại phải <= 10")
    private Float retestScore;
}