package com.example.ThangLongUniversityWeb.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtUtils {

    // 1. Tiêm giá trị từ application.properties
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long accessExpiration;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    // 2. Lấy Key để ký tên
    private Key getSignInKey() {
        // Cách 1: Nếu key trong properties là chuỗi Base64 (Khuyên dùng cho prod)
        // byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        // return Keys.hmacShaKeyFor(keyBytes);

        // Cách 2: Nếu key trong properties là chuỗi thường (Dùng cách này cho giống code cũ của bạn để đỡ lỗi)
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // 3. Tạo Access Token (Ngắn hạn)
    public String generateAccessToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + accessExpiration)) // Dùng biến accessExpiration
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 4. Tạo Refresh Token (Dài hạn)
    public String generateRefreshToken(String username) {
        return generateRefreshToken(username, UUID.randomUUID().toString());
    }

    public String generateRefreshToken(String username, String jti) {
        return Jwts.builder()
                .setSubject(username)
                .setId(jti)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration)) // Dùng biến refreshExpiration
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 5. Lấy Username từ Token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);
    }

    // 6. Kiểm tra Token có hợp lệ không
    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }
}