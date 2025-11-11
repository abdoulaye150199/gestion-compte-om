Migration & Neon reinitialization helper

What I changed

- `src/main/resources/application.yaml`: set `spring.jpa.hibernate.ddl-auto: none` to avoid Hibernate altering schema after migrations.
- `docker-compose.yml`: `SPRING_FLYWAY_ENABLED` default set to `false` to avoid accidental migrations at container start.
- Added `neon_reinit_and_migrations.sql` — a safe script that:
  - Copies existing tables into a schema named `backup_20251111_0015`.
  - Drops and recreates the `public` schema.
  - Applies migrations V3 → V6 (V6 adjusted to use UUID for `compte_id` to match `comptes.id`).
  - Creates a minimal `flyway_schema_history` table and inserts markers for V3..V6.
- Added helper scripts:
  - `run_migrations.sh` — runs the SQL script with `psql` (requires PGHOST/PGUSER/PGPASSWORD/PGDATABASE).
  - `run_flyway.sh` — runs `mvn flyway:migrate` using provided JDBC credentials (JDBC_URL/DB_USER/DB_PASS).
- Updated `.env.example` with Neon, psql and Twilio placeholders.

How to run (recommended, manual control)

1) Create a snapshot in Neon Console (recommended) or export the DB.
2) Copy `.env.example` to `.env` and fill real values.
3) Make the migration script executable (optional):

```bash
chmod +x run_migrations.sh run_flyway.sh
```

4) To run the SQL reinit script with psql (script performs backup then reinit):

```bash
# from repository root
export PGHOST=... PGPORT=5432 PGDATABASE=... PGUSER=... PGPASSWORD=...
./run_migrations.sh
```

5) Alternatively run Flyway via Maven (if you prefer Flyway):

```bash
export JDBC_URL=jdbc:postgresql://host:5432/dbname
export DB_USER=your_user
export DB_PASS=your_pass
./run_flyway.sh
```

6) After migrations succeed, update `application.yaml` locally to set `spring.jpa.hibernate.ddl-auto` to `validate` or `none` (already set to `none` by this change), and in your deployment ensure `JWT_SECRET` and other secrets are set.

If you want me to actually run the migration for you, paste the JDBC URL, username and password here and I will run the script and post the output. If you'd rather run it yourself, run the `run_migrations.sh` script in your environment and paste the result here; I'll verify and then run a smoke test.
