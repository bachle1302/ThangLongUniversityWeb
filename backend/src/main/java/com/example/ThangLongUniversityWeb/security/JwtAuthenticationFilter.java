package com.example.ThangLongUniversityWeb.security;
import com.example.ThangLongUniversityWeb.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip JWT filter for Swagger/OpenAPI endpoints
        return path.startsWith("/v3/api-docs") || 
               path.startsWith("/swagger-ui") || 
               path.startsWith("/swagger-resources") ||
               path.startsWith("/webjars") ||
               path.equals("/swagger-ui.html");
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            // 1. Lấy token từ request
            String jwt = parseJwt(request);

            // 2. Nếu có token và token hợp lệ
            if (jwt != null) {
                String username = jwtUtils.extractUsername(jwt);

                // 3. Nếu đọc được username và trong SecurityContext chưa có ai đăng nhập
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // 4. Kiểm tra token có khớp với user không và còn hạn không
                    if (jwtUtils.isTokenValid(jwt, userDetails.getUsername())) {
                        // 5. Set thông tin user vào Context để các API sau biết ai đang gọi
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Không thể thiết lập xác thực người dùng: " + e.getMessage());
        }

        // Cho phép request đi tiếp
        filterChain.doFilter(request, response);
    }
    

    // Hàm phụ: Cắt chữ "Bearer " để lấy đúng chuỗi token
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}