#!/usr/bin/env bash
# apply_migrations.sh
# Applies SQL migration files in src/main/resources/db/migration to a Postgres database using psql.
# Usage: export DB_URL or SPRING_DATASOURCE_URL or provide -h host -p port -d db -U user -W password

set -euo pipefail

MIGRATIONS_DIR="src/main/resources/db/migration"

print_usage(){
  cat <<EOF
Usage: DB connect via env or args:
  export SPRING_DATASOURCE_URL=jdbc:postgresql://host:port/dbname
  export SPRING_DATASOURCE_USERNAME=user
  export SPRING_DATASOURCE_PASSWORD=pass
  ./scripts/apply_migrations.sh

Or provide params:
  ./scripts/apply_migrations.sh -h host -p port -d db -U user -W password

This script will execute SQL files in ${MIGRATIONS_DIR} in lexical order.
EOF
}

# parse args
HOST=""
PORT=""
DBNAME=""
USER=""
PASSWORD=""

while getopts "+h:p:d:U:W:?" opt; do
  case "$opt" in
    h) HOST="$OPTARG" ;;
    p) PORT="$OPTARG" ;;
    d) DBNAME="$OPTARG" ;;
    U) USER="$OPTARG" ;;
    W) PASSWORD="$OPTARG" ;;
    ?) print_usage; exit 1 ;;
  esac
done

# Try environment values if not provided
if [ -z "$HOST" ] || [ -z "$PORT" ] || [ -z "$DBNAME" ]; then
  URL=${SPRING_DATASOURCE_URL:-${NEON_DATABASE_URL:-}}
  if [ -n "$URL" ] && [[ "$URL" == jdbc:postgresql:* ]]; then
    # parse jdbc:postgresql://host:port/dbname
    stripped=${URL#jdbc:postgresql://}
    hostport_db=${stripped%%/*}
    HOST=${HOST:-${hostport_db%%:*}}
    PORT=${PORT:-${hostport_db##*:}}
    DBNAME=${DBNAME:-${stripped#*/}}
    # strip params
    DBNAME=${DBNAME%%\?*}
  fi
fi

USER=${USER:-${SPRING_DATASOURCE_USERNAME:-${NEON_DATABASE_USERNAME:-}}}
PASSWORD=${PASSWORD:-${SPRING_DATASOURCE_PASSWORD:-${NEON_DATABASE_PASSWORD:-}}}

if [ -z "$HOST" ] || [ -z "$PORT" ] || [ -z "$DBNAME" ] || [ -z "$USER" ]; then
  echo "Missing DB connection details. Provide via env or args."
  print_usage
  exit 2
fi

export PGPASSWORD="$PASSWORD"

echo "Applying migrations from ${MIGRATIONS_DIR} to ${HOST}:${PORT}/${DBNAME} as ${USER}"

# Ensure psql exists
if ! command -v psql >/dev/null 2>&1; then
  echo "psql not found in PATH. Install postgresql-client or run migrations another way."
  exit 3
fi

# Execute files in order
for f in $(ls ${MIGRATIONS_DIR}/*.sql | sort); do
  echo "-- Applying $f"
  psql -h "$HOST" -p "$PORT" -U "$USER" -d "$DBNAME" -v ON_ERROR_STOP=1 -f "$f"
done

echo "Migrations applied successfully."
