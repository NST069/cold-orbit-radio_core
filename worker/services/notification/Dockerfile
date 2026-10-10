FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY . .

ENV GRADLE_OPTS="-Dorg.gradle.java.installations.auto-download=true"

RUN chmod +x gradlew
RUN ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

RUN useradd -ms /bin/bash spring
RUN mkdir -p /app/music && chown -R spring:spring /app
USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
