package com.example.ThangLongUniversityWeb.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Thông tin hóa đơn học phí")
public class TuitionResponse {
    @Schema(description = "Tên học kỳ", example = "HK1 2025-2026")
    private String semesterName;

    @Schema(description = "Tổng số tín chỉ", example = "20")
    private Integer totalCredits;

    @Schema(description = "Tổng tiền (VNĐ)", example = "10000000")
    private Long totalAmount;

    @Schema(description = "Số tiền đã thanh toán (VNĐ)", example = "8000000")
    private Long paidAmount;

    @Schema(description = "Giá mỗi tín chỉ (VNĐ)", example = "850000")
    private Long pricePerCredit;

    @Schema(description = "Trạng thái đã thanh toán", example = "false")
    private boolean isPaid;

    @Schema(description = "Chi tiết từng môn học trong hóa đơn")
    private List<TuitionItemResponse> items;
}
