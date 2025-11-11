#!/usr/bin/env bash
set -euo pipefail

# Run Flyway migrate using Maven. Requires these env vars:
# JDBC_URL, DB_USER, DB_PASS
: "${JDBC_URL:?Need JDBC_URL (jdbc:postgresql://host:port/db)}"
: "${DB_USER:?Need DB_USER}"
: "${DB_PASS:?Need DB_PASS}"

mvn -DskipTests \
  -Dflyway.url="$JDBC_URL" \
  -Dflyway.user="$DB_USER" \
  -Dflyway.password="$DB_PASS" \
  flyway:migrate

echo "Flyway migrate finished (check mvn output)."
