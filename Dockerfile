# Multi-stage Dockerfile for tatakae-api (Spring Boot, Java 21)
# Stage 1: Build the application with Maven
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copy Maven wrapper and pom.xml first for better layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src/ ./src/

# Build the application (skip tests for production image; run tests in CI)
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Runtime image with non-root user
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install wget for healthcheck
RUN apk add --no-cache wget

# Create a non-root user for running the application
RUN addgroup -S spring && adduser -S spring -G spring

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring:spring

# Expose the application port (default 8080)
EXPOSE 8080

# Healthcheck using the existing /healthcheck endpoint
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/healthcheck || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
