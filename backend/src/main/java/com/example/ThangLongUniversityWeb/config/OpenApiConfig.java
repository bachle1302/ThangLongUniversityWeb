package com.example.ThangLongUniversityWeb.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Admin",
                        email = "admin@thanglong.edu.vn",
                        url = "https://thanglong.edu.vn"
                ),
                description = "Tài liệu API cho hệ thống quản lý đại học Thăng Long",
                title = "University Management System API",
                version = "1.0",
                license = @License(
                        name = "MIT License",
                        url = "https://choosealicense.com/licenses/mit/"
                ),
                termsOfService = "Terms of service"
        ),
        servers = {
                @Server(
                        description = "Local Development",
                        url = "http://localhost:8080"
                ),
                @Server(
                        description = "Production",
                        url = "https://api.thanglong.edu.vn"
                )
        },
        security = {
                @SecurityRequirement(
                        name = "bearerAuth"
                )
        }
)
@io.swagger.v3.oas.annotations.security.SecurityScheme(
        name = "bearerAuth",
        description = "JWT Authentication: Nhập token vào đây (không cần 'Bearer ' prefix)",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token for authentication")))
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("University Management System API")
                        .description("Complete API documentation for Thang Long University Management System")
                        .version("1.0")
                        .contact(new io.swagger.v3.oas.models.info.Contact()
                                .name("Admin Support")
                                .email("admin@thanglong.edu.vn")
                                .url("https://thanglong.edu.vn"))
                        .license(new io.swagger.v3.oas.models.info.License()
                                .name("MIT License")
                                .url("https://choosealicense.com/licenses/mit/")));
    }

    // API Groups for better organization
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                                .group("authentication")
                                .displayName("Authentication")
                .pathsToMatch("/api/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                                .group("admin-management")
                                .displayName("Admin Management")
                .pathsToMatch("/api/admin/**")
                .build();
    }

    @Bean
    public GroupedOpenApi studentApi() {
        return GroupedOpenApi.builder()
                                .group("student-operations")
                                .displayName("Student Operations")
                .pathsToMatch("/api/student/**")
                .build();
    }

    @Bean
    public GroupedOpenApi teacherApi() {
        return GroupedOpenApi.builder()
                                .group("teacher-operations")
                                .displayName("Teacher Operations")
                .pathsToMatch("/api/teacher/**")
                .build();
    }

    @Bean
    public GroupedOpenApi chatApi() {
        return GroupedOpenApi.builder()
                                .group("chat-system")
                                .displayName("Chat System")
                .pathsToMatch("/api/chat/**")
                .build();
    }
}
