FROM amazoncorretto:21-alpine-jre

# Create a non-root user
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /application

# Copy the jar file and change ownership
COPY application-api/build/libs/application-api.jar app.jar
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring:spring

# Set JVM defaults
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:+UseContainerSupport"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=docker -Duser.timezone=Asia/Seoul -jar app.jar"]