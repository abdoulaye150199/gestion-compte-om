#!/usr/bin/env bash
set -e

# Wait for Postgres to be ready (portable method using /dev/tcp)
# Support explicit SPRING_DATASOURCE_HOST and SPRING_DATASOURCE_PORT as alternatives

# Try to parse host and port from SPRING_DATASOURCE_URL (jdbc:postgresql://host:port/db)
host=${SPRING_DATASOURCE_HOST:-}
port=${SPRING_DATASOURCE_PORT:-}

if [ -z "$host" ] || [ -z "$port" ]; then
  if [ -n "$SPRING_DATASOURCE_URL" ]; then
    # Extract host
    parsed_host=$(echo "$SPRING_DATASOURCE_URL" | sed -E 's#.*//([^:/]+).*#\1#')
    # Extract port if present
    parsed_port=$(echo "$SPRING_DATASOURCE_URL" | sed -E 's#.*://[^:/]+:([0-9]+).*#\1#')

    # Use parsed values if not empty
    host=${host:-$parsed_host}
    port=${port:-$parsed_port}
  fi
fi

# Default port for Postgres
port=${port:-5432}

if [ -z "$host" ]; then
  echo "SPRING_DATASOURCE_URL / SPRING_DATASOURCE_HOST not set or cannot be parsed: $SPRING_DATASOURCE_URL"
else
  echo "Waiting for database $host:$port..."
  max_retries=30
  wait_seconds=2
  i=0
  while [ $i -lt $max_retries ]; do
    # Use bash TCP redirection; this works in bash (no external nc required)
    if (echo > /dev/tcp/$host/$port) >/dev/null 2>&1; then
      echo "Database is available at $host:$port"
      break
    fi
    i=$((i+1))
    echo "Waiting for DB ($i/$max_retries) - sleeping $wait_seconds seconds..."
    sleep $wait_seconds
  done

  if [ $i -ge $max_retries ]; then
    echo "Timed out waiting for database at $host:$port" >&2
    # continue anyway to let the java process handle/fail if necessary
  fi
fi

# Exec the jar
exec java -jar /app/app.jar "$@"
