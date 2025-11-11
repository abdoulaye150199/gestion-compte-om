#!/usr/bin/env bash
# Interactive script to configure and start the app with Neon

set -euo pipefail

echo "=========================================="
echo "Gestion-CompteOM: Configuration & Startup"
echo "=========================================="
echo ""

# Check if .env.local exists
if [[ -f .env.local ]]; then
    echo "Found .env.local"
    read -p "Use existing .env.local? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        export $(cat .env.local | grep -v '^#' | grep '=' | xargs)
    fi
fi

# Prompt for credentials if not set
if [[ -z "${SPRING_DATASOURCE_URL:-}" ]]; then
    echo ""
    echo "Enter your Neon PostgreSQL connection details:"
    read -p "JDBC URL (e.g., jdbc:postgresql://ep-xxx.c-3.us-east-1.aws.neon.tech/ompay?sslmode=require&channel_binding=require): " SPRING_DATASOURCE_URL
fi

if [[ -z "${SPRING_DATASOURCE_USERNAME:-}" ]]; then
    read -p "Username (e.g., neondb_owner): " SPRING_DATASOURCE_USERNAME
fi

if [[ -z "${SPRING_DATASOURCE_PASSWORD:-}" ]]; then
    read -sp "Password: " SPRING_DATASOURCE_PASSWORD
    echo ""
fi

if [[ -z "${JWT_SECRET:-}" ]]; then
    read -p "JWT Secret (or press Enter for default): " JWT_SECRET
    JWT_SECRET="${JWT_SECRET:=dev-secret-please-change-in-production}"
fi

# Export variables
export SPRING_DATASOURCE_URL
export SPRING_DATASOURCE_USERNAME
export SPRING_DATASOURCE_PASSWORD
export SPRING_DATASOURCE_DRIVER="${SPRING_DATASOURCE_DRIVER:=org.postgresql.Driver}"
export SPRING_FLYWAY_ENABLED="${SPRING_FLYWAY_ENABLED:=false}"
export JWT_SECRET

# Test connection briefly
echo ""
echo "Testing database connection..."
if timeout 10 mvn -DskipTests spring-boot:run -Dspring-boot.run.arguments="--spring.jpa.hibernate.ddl-auto=validate" 2>&1 | grep -q "Started GesionCompteOmApplication"; then
    echo "✓ Connection successful!"
else
    echo "⚠ Connection test inconclusive (this is normal if startup takes time)"
fi

# Save to .env.local for next time
echo ""
read -p "Save credentials to .env.local for next time? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    cat > .env.local <<EOF
NEON_DATABASE_URL=${SPRING_DATASOURCE_URL}
NEON_DATABASE_USERNAME=${SPRING_DATASOURCE_USERNAME}
NEON_DATABASE_PASSWORD=${SPRING_DATASOURCE_PASSWORD}
JWT_SECRET=${JWT_SECRET}
SPRING_FLYWAY_ENABLED=false
EOF
    echo "✓ Saved to .env.local"
fi

# Start the app
echo ""
echo "Starting app..."
echo "Database: ${SPRING_DATASOURCE_URL}"
echo ""

mvn -DskipTests spring-boot:run
