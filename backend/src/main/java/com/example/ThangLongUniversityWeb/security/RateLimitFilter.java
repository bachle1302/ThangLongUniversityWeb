package com.example.ThangLongUniversityWeb.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip rate limiting for Swagger/OpenAPI and other public endpoints
        if (path.startsWith("/v3/api-docs") || 
            path.startsWith("/swagger-ui") || 
            path.startsWith("/swagger-resources") ||
            path.startsWith("/webjars") ||
            path.equals("/swagger-ui.html")) {
            return true;
        }
        // Apply only to sensitive endpoints
        return !(path.equals("/api/auth/login") || path.startsWith("/api/student/enroll"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String key = key(request);
        Bucket bucket = buckets.computeIfAbsent(key, k -> newBucket(request));

        io.github.bucket4j.ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
            return;
        }

        long nanosToWait = probe.getNanosToWaitForRefill();
        long secondsToWait = (nanosToWait / 1_000_000_000) + 1; // ceil

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", String.valueOf(secondsToWait));
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"Too many requests\",\"retryAfter\":" + secondsToWait + "}");
    }

    private Bucket newBucket(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Default: 10 req/min
        int capacity = 10;
        Duration period = Duration.ofMinutes(1);

        if (path.equals("/api/auth/login")) {
            capacity = 10; // 10 login attempts per minute per IP
        } else if (path.startsWith("/api/student/enroll")) {
            capacity = 20; // 20 enroll actions per minute per IP
        }

        Refill refill = Refill.greedy(capacity, period);
        Bandwidth limit = Bandwidth.classic(capacity, refill);
        return Bucket.builder().addLimit(limit).build();
    }

    private String key(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }
        return request.getRequestURI() + ":" + ip;
    }
}

