package com.example.ThangLongUniversityWeb.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

/**
 * Cấu hình Redis cho:
 * 1. Caching online users
 * 2. Pub/Sub để broadcast tin nhắn
 * 3. Session management
 */
@Configuration
public class RedisConfig {

    /**
     * Cấu hình RedisTemplate để serialize data thành JSON
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Serializer cho key (String)
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        
        // Serializer cho value (JSON) - sử dụng String để tránh deprecated warning
        // Sẽ tự handle JSON serialization trong service layer

        // Thiết lập serializer
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer); // Sử dụng String cho value
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.activateDefaultTyping(om.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        // Tự implement RedisSerializer<Object> bằng Jackson thuần — tránh mọi deprecated wrapper
        RedisSerializer<Object> serializer = new RedisSerializer<>() {
            @Override
            public byte[] serialize(Object value) throws SerializationException {
                if (value == null) return new byte[0];
                try {
                    return om.writeValueAsBytes(value);
                } catch (Exception e) {
                    throw new SerializationException("Redis serialize error: " + e.getMessage(), e);
                }
            }

            @Override
            public Object deserialize(byte[] bytes) throws SerializationException {
                if (bytes == null || bytes.length == 0) return null;
                try {
                    return om.readValue(bytes, Object.class);
                } catch (Exception e) {
                    throw new SerializationException("Redis deserialize error: " + e.getMessage(), e);
                }
            }
        };

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        Map<String, RedisCacheConfiguration> cacheConfigurations = Map.of(
                "adminDashboard", config.entryTtl(Duration.ofSeconds(60)),
                "teacherDashboard", config.entryTtl(Duration.ofSeconds(60)),
                "classSectionOptions", config.entryTtl(Duration.ofMinutes(5)),
                "semesters", config.entryTtl(Duration.ofMinutes(5)),
                "courses", config.entryTtl(Duration.ofMinutes(60)),
                "rooms", config.entryTtl(Duration.ofMinutes(60)),
                "periods", config.entryTtl(Duration.ofMinutes(60))
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}

/**
 * Redis Key Patterns:
 * 
 * 1. Online Users:
 *    - Key: "chat:online:users"
 *    - Value: Set<userId>
 *    - TTL: 5 minutes (tự động expire)
 * 
 * 2. User Session Info:
 *    - Key: "chat:user:{userId}:session"
 *    - Value: { username, fullName, avatar, lastActive }
 *    - TTL: 30 minutes
 * 
 * 3. Chat Room Members:
 *    - Key: "chat:room:{chatRoomId}:members"
 *    - Value: Set<userId>
 *    - TTL: Vĩnh viễn (hoặc update khi có thay đổi)
 * 
 * 4. Unread Message Count:
 *    - Key: "chat:user:{userId}:unread"
 *    - Value: { chatRoomId: count, ... }
 *    - TTL: Vĩnh viễn (update real-time)
 * 
 * 5. Message Cache (lịch sử gần đây):
 *    - Key: "chat:room:{chatRoomId}:messages"
 *    - Value: List<Message>
 *    - TTL: 1 hour (để lấy nhanh)
 * 
 * 6. Typing Indicator:
 *    - Key: "chat:room:{chatRoomId}:typing"
 *    - Value: { userId: timestamp, ... }
 *    - TTL: 3 seconds (tự động expire)
 */
