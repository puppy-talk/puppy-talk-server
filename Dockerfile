# Multi-stage Docker build for PuppyTalk Server
# Backend 관점: 최적화된 프로덕션 배포를 위한 멀티스테이지 빌드

FROM gradle:8.5-jdk17 AS builder

WORKDIR /app

# Gradle 설정 파일만 먼저 복사 (캐싱 최적화)
COPY build.gradle settings.gradle gradlew ./
COPY gradle/ gradle/

# 모든 모듈별 build.gradle 복사
COPY puppytalk-domain/build.gradle puppytalk-domain/
COPY puppytalk-application/build.gradle puppytalk-application/
COPY puppytalk-infrastructure/build.gradle puppytalk-infrastructure/
COPY puppytalk-api/build.gradle puppytalk-api/
COPY puppytalk-bootstrap/build.gradle puppytalk-bootstrap/
COPY puppytalk-test/build.gradle puppytalk-test/

RUN gradle dependencies --no-daemon

# 모든 소스 코드 복사
COPY puppytalk-domain/src puppytalk-domain/src
COPY puppytalk-application/src puppytalk-application/src
COPY puppytalk-infrastructure/src puppytalk-infrastructure/src
COPY puppytalk-api/src puppytalk-api/src
COPY puppytalk-bootstrap/src puppytalk-bootstrap/src
COPY puppytalk-test/src puppytalk-test/src

# JAR 빌드 (테스트 포함)
RUN gradle clean build --no-daemon

# Production runtime image
FROM openjdk:17-jre-slim

# 일반 계정 사용(보안)
RUN groupadd -g 1001 puppytalk && \
    useradd -u 1001 -g puppytalk puppytalk

WORKDIR /app

# health checks를 위한 curl 설치
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/puppytalk-bootstrap/build/libs/puppytalk-bootstrap-*.jar app.jar

RUN chown puppytalk:puppytalk app.jar

USER puppytalk

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Environment variables with defaults
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xmx512m -Xms256m -Djava.security.egd=file:/dev/./urandom"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]