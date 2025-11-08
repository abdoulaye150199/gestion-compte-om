# Build stage
FROM eclipse-temurin:17-jdk-focal AS build
WORKDIR /workspace/app

# Copy gradle wrapper and build files first for cache
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle pom.xml ./

# Copy source
COPY src ./src

# Grant executable permission for gradlew
RUN chmod +x gradlew

# Build the jar (use --no-daemon for CI-friendly)
RUN ./gradlew clean bootJar --no-daemon -x test || ./gradlew clean assemble --no-daemon -x test

# Run stage
FROM eclipse-temurin:17-jre-focal
WORKDIR /app

# Copy entrypoint script (will be added) and make executable
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

# Copy jar from build stage (use wildcard; Docker supports it here)
COPY --from=build /workspace/app/build/libs/*.jar /app/app.jar

# Expose port
EXPOSE 8080

# Default env (can be overridden)
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/railway
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=

# Use entrypoint which waits for DB then execs the jar
ENTRYPOINT ["/app/entrypoint.sh"]
