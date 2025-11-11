#!/usr/bin/env bash
set -euo pipefail

# Run the reinitialization + migrations SQL against a Postgres-compatible DB.
# Requires these env vars to be set:
# PGHOST, PGPORT (optional, default 5432), PGDATABASE, PGUSER, PGPASSWORD

: "${PGHOST:?Need PGHOST environment variable (host)}"
: "${PGDATABASE:?Need PGDATABASE environment variable (database name)}"
: "${PGUSER:?Need PGUSER environment variable (username)}"
: "${PGPASSWORD:?Need PGPASSWORD environment variable (password)}"
: "${PGPORT:=5432}"

SQL_FILE="neon_reinit_and_migrations.sql"
if [[ ! -f "$SQL_FILE" ]]; then
  echo "ERROR: $SQL_FILE not found in $(pwd)"
  exit 2
fi

export PGPASSWORD="$PGPASSWORD"

echo "Running migrations script: $SQL_FILE against $PGHOST:$PGPORT/$PGDATABASE"
psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$PGDATABASE" -f "$SQL_FILE"

echo "Migrations script executed. Check output above for any errors."
