#!/usr/bin/env bash
set -e

# Wait for Postgres to be ready
host=$(echo "$SPRING_DATASOURCE_URL" | sed -E 's#.*//([^:/]+).*#\1#')
port=$(echo "$SPRING_DATASOURCE_URL" | sed -E 's#.*:([0-9]+).*#\1#')

if [ -z "$host" ] || [ -z "$port" ]; then
  echo "SPRING_DATASOURCE_URL is not set or cannot be parsed: $SPRING_DATASOURCE_URL"
else
  echo "Waiting for database $host:$port..."
  # wait loop
  for i in {1..30}; do
    if nc -z "$host" "$port"; then
      echo "Database is available"
      break
    fi
    echo "Waiting $i..."
    sleep 2
  done
fi

# Exec the jar
exec java -jar /app/app.jar "$@"

