#!/usr/bin/env bash
# Run SQL migration files in src/main/resources/db/migration in alphabetical order
# Usage examples:
#   PGHOST=localhost PGPORT=5432 PGUSER=me PGPASSWORD=secret PGDATABASE=mydb ./scripts/run_migrations_local.sh
#   or set SPRING_DATASOURCE_URL=jdbc:postgresql://host:port/dbname and SPRING_DATASOURCE_USERNAME / _PASSWORD

set -euo pipefail

MIGRATION_DIR="$(dirname "$0")/../src/main/resources/db/migration"
if [ ! -d "$MIGRATION_DIR" ]; then
  echo "Migration directory not found: $MIGRATION_DIR"
  exit 1
fi

# If SPRING_DATASOURCE_URL present and PG vars not set, try to parse it
if [ -z "${PGHOST:-}" ] && [ -n "${SPRING_DATASOURCE_URL:-}" ]; then
  # expect jdbc:postgresql://host:port/database
  url="${SPRING_DATASOURCE_URL}"
  url_no_prefix=${url#jdbc:postgresql://}
  hostport=${url_no_prefix%%/*}
  PGHOST=${hostport%%:*}
  PGPORT=${hostport##*:}
  if [ "$PGPORT" = "$PGHOST" ]; then
    # no port specified
    PGPORT=5432
  fi
  PGDATABASE=${url_no_prefix#*/}
  echo "Parsed SPRING_DATASOURCE_URL -> host=$PGHOST port=$PGPORT db=$PGDATABASE"
fi

# Ensure required vars
: "${PGHOST:?Please export PGHOST or set SPRING_DATASOURCE_URL}" 
: "${PGPORT:=5432}"
: "${PGUSER:=${SPRING_DATASOURCE_USERNAME:-}}"
: "${PGPASSWORD:=${SPRING_DATASOURCE_PASSWORD:-}}"
: "${PGDATABASE:=${SPRING_DATASOURCE_NAME:-${SPRING_DATASOURCE_URL##*/}}}"

if [ -z "$PGUSER" ] || [ -z "$PGPASSWORD" ] || [ -z "$PGDATABASE" ]; then
  echo "Please set PGUSER, PGPASSWORD and PGDATABASE (or SPRING_DATASOURCE_* equivalents)."
  echo "Example: PGHOST=localhost PGPORT=5432 PGUSER=user PGPASSWORD=pass PGDATABASE=mydb ./scripts/run_migrations_local.sh"
  exit 1
fi

export PGPASSWORD="$PGPASSWORD"

echo "Applying SQL migrations from: $MIGRATION_DIR"
for f in $(ls "$MIGRATION_DIR"/*.sql | sort); do
  echo "---- applying: $f"
  psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$PGDATABASE" -f "$f"
done

echo "All migrations applied."
