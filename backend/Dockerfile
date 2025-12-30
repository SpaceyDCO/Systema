# Build
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build
COPY pom.xml .
COPY backend/pom.xml ./backend/
COPY frontend/pom.xml ./frontend/
RUN mvn dependency:go-offline -B --projects backend
COPY backend/src ./backend/src
RUN mvn clean package org.springframework.boot:spring-boot-maven-plugin:4.0.0-RC1:repackage -DskipTests -B -pl backend -am

# Runtime
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app
COPY --from=builder /build/backend/target/systema-backend-*.jar app.jar
RUN chown spring:spring app.jar
USER spring:spring
EXPOSE 8080

# Health checks
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
