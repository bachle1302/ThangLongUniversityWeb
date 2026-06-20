package com.example.ThangLongUniversityWeb.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Kafka configuration: creates topics automatically and sends failed messages to DLT.
 * Active when spring.kafka.enabled=true.
 */
@Configuration
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true")
public class KafkaConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConfig.class);

    public static final String ENROLLMENT_TOPIC = "class-registration";
    public static final String ENROLLMENT_DLT = "class-registration.DLT";

    @Bean
    public NewTopic enrollmentTopic() {
        return TopicBuilder.name(ENROLLMENT_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic enrollmentDltTopic() {
        return TopicBuilder.name(ENROLLMENT_DLT)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public CommonErrorHandler enrollmentErrorHandler(KafkaTemplate<String, String> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> {
                    log.error(
                            "[Kafka DLT] Message sent to DLT after retries. topic={}, key={}, error={}",
                            record.topic(),
                            record.key(),
                            ex.getMessage()
                    );
                    return new TopicPartition(ENROLLMENT_DLT, 0);
                }
        );

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3L));
        handler.addNotRetryableExceptions(IllegalArgumentException.class);
        return handler;
    }
}
