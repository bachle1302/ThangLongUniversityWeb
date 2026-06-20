package com.example.ThangLongUniversityWeb.config;

import com.example.ThangLongUniversityWeb.security.JwtAuthenticationFilter;
import com.example.ThangLongUniversityWeb.security.RateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final RateLimitFilter rateLimitFilter;

    @Value("${app.cors.allowed-origins:http://localhost:*,http://127.0.0.1:*,http://localhost:3000,http://localhost:8080}")
    private String allowedOrigins;

    /**
     * IMPORTANT:
     * JwtAuthenticationFilter/RateLimitFilter là javax/jakarta Servlet Filter.
     * Nếu để Spring Boot auto-register vào Servlet FilterChain, chúng có thể bị đăng ký DUPLICATE
     * (vừa ở Servlet chain, vừa ở Spring Security chain) và gây lỗi "does not have a registered order".
     *
     * => Disable auto-registration, chỉ gắn filter qua SecurityFilterChain.
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(RateLimitFilter filter) {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // --- SỬA LẠI ĐOẠN NÀY ĐỂ HẾT LỖI ---
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // Khai báo thêm Bean này để Spring biết dùng UserDetailsService nào
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    // ------------------------------------

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. CORS
                .cors(cors -> cors.configurationSource(request -> {
                    var corsConfig = new org.springframework.web.cors.CorsConfiguration();
                        corsConfig.setAllowedOriginPatterns(java.util.Arrays.stream(allowedOrigins.split(","))
                            .map(String::trim)
                            .filter(origin -> !origin.isBlank())
                            .toList());
                    corsConfig.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    corsConfig.setAllowedHeaders(java.util.List.of("*"));
                    corsConfig.setAllowCredentials(true);
                    return corsConfig;
                }))
                // 2. CSRF off (JWT)
                .csrf(csrf -> csrf.disable())
                // 3. Disable default auth mechanisms
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                // 4. Authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                // Swagger/OpenAPI endpoints - MUST be permitAll
                            "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**",
                                // Auth endpoints
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/api/auth/logout",
                                // Public student endpoints
                                "/api/student/tuition/vnpay-return",
                                "/api/payments/vnpay/return",
                                // WebSocket
                                "/ws/**"
                        ).permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/student/**").hasRole("STUDENT")
                        .requestMatchers("/api/teacher/**").hasRole("TEACHER")
                        .requestMatchers("/api/chat/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                        .requestMatchers("/api/chatbot/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                        .anyRequest().authenticated()
                )
                // 5. Stateless session
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 6. Provider & Filters
                .authenticationProvider(authenticationProvider())
                // Mốc phải là filter chuẩn của Spring (không dùng custom filter class làm mốc)
                // Order hiệu quả: rate limit -> jwt -> username/password
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
