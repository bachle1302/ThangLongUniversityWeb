package com.example.ThangLongUniversityWeb.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        String normalizedCloudName = cloudName == null ? "" : cloudName.trim().toLowerCase(Locale.ROOT);

        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", normalizedCloudName,
                "api_key", apiKey == null ? "" : apiKey.trim(),
                "api_secret", apiSecret == null ? "" : apiSecret.trim(),
                "secure", true
        ));
    }
}
