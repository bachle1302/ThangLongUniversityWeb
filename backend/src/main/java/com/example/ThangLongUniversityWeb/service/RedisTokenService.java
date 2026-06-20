package com.example.ThangLongUniversityWeb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate; // <-- Dùng cái này chuẩn hơn
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class RedisTokenService {

    // Spring Boot luôn tạo sẵn bean này, không bao giờ lỗi thiếu Bean
    private final StringRedisTemplate redisTemplate;

    // Lấy giá trị từ file properties
    @Value("${application.redis.refresh-token.ttl-days}")
    private long refreshTokenTTL;

    @Value("${application.redis.fallback-in-memory-enabled:true}")
    private boolean fallbackInMemoryEnabled;

    private static final String KEY_USER_CURRENT_JTI_PREFIX = "refresh:user:";
    private static final String KEY_REFRESH_JTI_PREFIX = "refresh:jti:";
    private final Map<String, InMemoryToken> fallbackCurrentJtiByUser = new ConcurrentHashMap<>();
    private final Map<String, InMemoryToken> fallbackUserByJti = new ConcurrentHashMap<>();

    private Duration refreshTtl() {
        return Duration.ofDays(refreshTokenTTL);
    }

    public void saveCurrentRefreshToken(String username, String jti) {
        try {
            redisTemplate.opsForValue().set(KEY_USER_CURRENT_JTI_PREFIX + username, jti, refreshTtl());
            redisTemplate.opsForValue().set(KEY_REFRESH_JTI_PREFIX + jti, username, refreshTtl());
        } catch (RuntimeException ex) {
            if (!fallbackInMemoryEnabled) throw ex;
            Instant expiresAt = Instant.now().plus(refreshTtl());
            fallbackCurrentJtiByUser.put(username, new InMemoryToken(jti, expiresAt));
            fallbackUserByJti.put(jti, new InMemoryToken(username, expiresAt));
        }
    }

    public String getCurrentRefreshJti(String username) {
        try {
            return redisTemplate.opsForValue().get(KEY_USER_CURRENT_JTI_PREFIX + username);
        } catch (RuntimeException ex) {
            if (!fallbackInMemoryEnabled) throw ex;
            return getActiveValue(fallbackCurrentJtiByUser, username);
        }
    }

    public boolean isRefreshJtiActive(String jti) {
        try {
            return redisTemplate.hasKey(KEY_REFRESH_JTI_PREFIX + jti);
        } catch (RuntimeException ex) {
            if (!fallbackInMemoryEnabled) throw ex;
            return getActiveValue(fallbackUserByJti, jti) != null;
        }
    }

    public void revokeRefreshToken(String username, String jti) {
        try {
            if (username != null) {
                String currentJti = getCurrentRefreshJti(username);
                if (currentJti != null && currentJti.equals(jti)) {
                    redisTemplate.delete(KEY_USER_CURRENT_JTI_PREFIX + username);
                }
            }
            if (jti != null) {
                redisTemplate.delete(KEY_REFRESH_JTI_PREFIX + jti);
            }
        } catch (RuntimeException ex) {
            if (!fallbackInMemoryEnabled) throw ex;
            if (username != null && jti != null && jti.equals(getActiveValue(fallbackCurrentJtiByUser, username))) {
                fallbackCurrentJtiByUser.remove(username);
            }
            if (jti != null) {
                fallbackUserByJti.remove(jti);
            }
        }
    }

    public void revokeAllForUser(String username) {
        if (username == null) return;
        try {
            String currentJti = getCurrentRefreshJti(username);
            redisTemplate.delete(KEY_USER_CURRENT_JTI_PREFIX + username);
            if (currentJti != null) {
                redisTemplate.delete(KEY_REFRESH_JTI_PREFIX + currentJti);
            }
        } catch (RuntimeException ex) {
            if (!fallbackInMemoryEnabled) throw ex;
            String currentJti = getActiveValue(fallbackCurrentJtiByUser, username);
            fallbackCurrentJtiByUser.remove(username);
            if (currentJti != null) {
                fallbackUserByJti.remove(currentJti);
            }
        }
    }

    private String getActiveValue(Map<String, InMemoryToken> store, String key) {
        InMemoryToken token = store.get(key);
        if (token == null) return null;
        if (token.expiresAt().isBefore(Instant.now())) {
            store.remove(key);
            return null;
        }
        return token.value();
    }

    private record InMemoryToken(String value, Instant expiresAt) {
    }
}
