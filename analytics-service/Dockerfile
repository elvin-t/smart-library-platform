FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY target/analytics-service-0.0.1-SNAPSHOT.jar analytics-service.jar

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "analytics-service.jar"]