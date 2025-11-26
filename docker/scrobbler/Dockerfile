FROM eclipse-temurin:17-jre-alpine AS runtime

RUN apk add --no-cache curl

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --chown=spring:spring target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Xmx128m", "-Xms64m", "-jar", "app.jar"]