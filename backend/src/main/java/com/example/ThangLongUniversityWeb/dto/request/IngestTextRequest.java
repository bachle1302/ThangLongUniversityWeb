package com.example.ThangLongUniversityWeb.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IngestTextRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private String sourceUrl;

    /** WEBSITE | HANDBOOK | ANNOUNCEMENT | FAQ | WIKIPEDIA */
    @NotBlank
    private String sourceType;

    /** 1 = highest priority, 5 = lowest */
    @NotNull
    private Integer priority;
}
