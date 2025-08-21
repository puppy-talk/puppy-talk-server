# Multi-stage build for optimized image size
FROM openjdk:21-jdk-slim as builder

# Install necessary tools
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle .
COPY settings.gradle .
COPY gradle/ gradle/

# Copy all source code
COPY . .

# Make gradlew executable
RUN chmod +x ./gradlew

# Build the application
RUN ./gradlew clean build -x test --no-daemon

# Runtime stage
FROM openjdk:21-jre-slim

# Add metadata
LABEL maintainer="Puppy Talk Team"
LABEL description="Puppy Talk Server - AI-powered Pet Chat Application"
LABEL version="1.0.0"

# Create non-root user for security
RUN groupadd -r puppytalk && useradd -r -g puppytalk -s /bin/false puppytalk

# Install curl for health checks
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/application-api/build/libs/application-api-*.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs && chown -R puppytalk:puppytalk /app

# Switch to non-root user
USER puppytalk

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM optimization for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:+UseStringDeduplication \
               -XX:+OptimizeStringConcat \
               -Djava.security.egd=file:/dev/./urandom"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]