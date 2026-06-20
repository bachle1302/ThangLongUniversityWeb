package com.example.ThangLongUniversityWeb.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IngestUrlRequest {

    @NotBlank
    private String url;

    @NotBlank
    private String title;

    @NotBlank
    private String sourceType;

    @NotNull
    private Integer priority;
}
