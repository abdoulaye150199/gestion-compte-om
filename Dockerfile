# Multi-stage build using Maven
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /workspace/app

# Copy pom and source
COPY pom.xml ./
COPY src ./src

# Build the application with Maven directly
RUN mvn -B -DskipTests clean package

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the JAR from build stage
COPY --from=build /workspace/app/target/gestion-compte-om-1.0.0.jar /app/app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application - environment variables come from Render
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
