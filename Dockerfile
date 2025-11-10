# Multi-stage build using Maven
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /workspace/app

# Copy pom and sources
COPY pom.xml ./
COPY src ./src

# Build the application
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy entrypoint script and jar
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh
COPY --from=build /workspace/app/target/*.jar /app/app.jar

# Expose port
EXPOSE 8080

# Environment variables (do NOT hardcode secrets here). Set these at deploy time or in your CI.
ENV SPRING_DATASOURCE_URL=
ENV SPRING_DATASOURCE_USERNAME=
ENV SPRING_DATASOURCE_PASSWORD=
ENV TWILIO_ACCOUNT_SID=
ENV TWILIO_AUTH_TOKEN=
ENV TWILIO_FROM=
ENV JWT_SECRET=

ENTRYPOINT ["/app/entrypoint.sh"]
