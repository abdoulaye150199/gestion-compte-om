#!/usr/bin/env bash
# Run the application with Neon connection using environment variables from .env.local or explicit exports

set -euo pipefail

# Check if .env.local exists; if not, require explicit env vars
if [[ -f .env.local ]]; then
    echo "Loading environment from .env.local..."
    export $(cat .env.local | grep -v '^#' | xargs)
fi

# Verify required variables
: "${SPRING_DATASOURCE_URL:?Need SPRING_DATASOURCE_URL (e.g. jdbc:postgresql://host:5432/db)}"
: "${SPRING_DATASOURCE_USERNAME:?Need SPRING_DATASOURCE_USERNAME}"
: "${SPRING_DATASOURCE_PASSWORD:?Need SPRING_DATASOURCE_PASSWORD}"

# Default values
export SPRING_DATASOURCE_DRIVER="${SPRING_DATASOURCE_DRIVER:=org.postgresql.Driver}"
export SPRING_FLYWAY_ENABLED="${SPRING_FLYWAY_ENABLED:=false}"
export JWT_SECRET="${JWT_SECRET:=dev-secret-please-change}"

echo "✓ Starting app with Neon connection..."
echo "  Database: ${SPRING_DATASOURCE_URL}"
echo "  User: ${SPRING_DATASOURCE_USERNAME}"
echo "  Flyway: ${SPRING_FLYWAY_ENABLED}"

mvn -DskipTests spring-boot:run
