# Multi-stage build for the Spring Boot app
# Build stage
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Prepare Maven wrapper and dependencies
COPY pom.xml mvnw mvnw.cmd ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline

# Copy source
COPY src src

# Build the jar
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/scheduler-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
