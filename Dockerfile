FROM gradle:8.5-jdk17 AS builder

WORKDIR /app

# Gradle 설정 파일만 먼저 복사 (캐싱 최적화)
COPY build.gradle settings.gradle gradlew ./
COPY gradle/ gradle/

# 모듈별 build.gradle 복사
COPY puppytalk-domain/build.gradle puppytalk-domain/
COPY puppytalk-application/build.gradle puppytalk-application/
COPY puppytalk-infrastructure/build.gradle puppytalk-infrastructure/
COPY puppytalk-api/build.gradle puppytalk-api/
COPY puppytalk-bootstrap/build.gradle puppytalk-bootstrap/

RUN gradle dependencies --no-daemon

# 소스 코드 복사
COPY puppytalk-domain/src puppytalk-domain/src
COPY puppytalk-application/src puppytalk-application/src
COPY puppytalk-infrastructure/src puppytalk-infrastructure/src
COPY puppytalk-api/src puppytalk-api/src
COPY puppytalk-bootstrap/src puppytalk-bootstrap/src

# JAR 빌드
RUN gradle :puppytalk-bootstrap:bootJar --no-daemon

# Production runtime image
FROM openjdk:17-jdk-slim

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

ENTRYPOINT ["java", "-jar", "/app/app.jar"]