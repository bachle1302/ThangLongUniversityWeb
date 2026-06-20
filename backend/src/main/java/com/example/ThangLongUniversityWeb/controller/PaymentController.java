package com.example.ThangLongUniversityWeb.controller;

import com.example.ThangLongUniversityWeb.service.StudentTuitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments")
public class PaymentController {

    private final StudentTuitionService studentTuitionService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Operation(summary = "Nhan ket qua tra ve tu VNPAY")
    @GetMapping("/vnpay/return")
    public ResponseEntity<?> vnpayReturn(HttpServletRequest request) {
        StudentTuitionService.VNPayReturnResult result = studentTuitionService.processVNPayReturnResult(request);
        URI redirectUri = UriComponentsBuilder
                .fromUriString(frontendUrl)
                .path("/student/payment-result")
                .queryParam("status", result.success() ? "success" : "failed")
                .queryParam("message", result.message())
                .queryParamIfPresent("transactionNo", Optional.ofNullable(result.transactionNo()))
                .queryParamIfPresent("txnRef", Optional.ofNullable(result.txnRef()))
                .queryParamIfPresent("responseCode", Optional.ofNullable(result.responseCode()))
                .queryParamIfPresent("transactionStatus", Optional.ofNullable(result.transactionStatus()))
                .queryParamIfPresent("amount", Optional.ofNullable(result.amount()))
                .queryParamIfPresent("bankCode", Optional.ofNullable(result.bankCode()))
                .build()
                .encode()
                .toUri();

        return ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();
    }
}
