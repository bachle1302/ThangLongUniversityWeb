# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY gradlew gradlew.bat ./
COPY gradle/ gradle/
COPY build.gradle.kts settings.gradle.kts ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true
COPY src/ src/
RUN ./gradlew bootJar --no-daemon -x test

# Tự động tạo Truststore từ ca.pem (nếu có) để phục vụ Kafka Cloud (Aiven)
COPY ca.pem* ./
RUN if [ -f ca.pem ]; then \
      keytool -import -file ca.pem -alias AivenCA -keystore client.truststore.jks -storepass 123456 -noprompt; \
    fi

# Stage 2: Runtime (~200MB image)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=builder /app/build/libs/*.jar app.jar
# Copy file truststore sang môi trường chạy (nếu có)
COPY --from=builder /app/client.truststore.jks* ./
RUN chown -R app:app /app
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
