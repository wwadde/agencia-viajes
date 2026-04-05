
FROM eclipse-temurin:25-jdk-noble AS builder

WORKDIR /workspace


COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle.kts .
COPY settings.gradle.kts .

RUN chmod +x gradlew && ./gradlew dependencies --no-daemon --quiet

COPY src/ src/

RUN ./gradlew bootJar --no-daemon -x test


FROM eclipse-temurin:25-jre-noble AS runtime

LABEL org.opencontainers.image.title="agencia-backend"
LABEL org.opencontainers.image.description="API REST Spring Boot – Agencia de viajes"
LABEL org.opencontainers.image.vendor="Agencia"

RUN groupadd --system appgroup && useradd --system --gid appgroup --no-create-home appuser

WORKDIR /app

COPY --from=builder /workspace/build/libs/*.jar app.jar

RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]

