# Compatibility Results

This file records selected public Scriptella database compatibility runs. It
is a lightweight, human-readable history; GitHub Actions and the uploaded
reports remain authoritative for each run.

## 2026-09-11 — Scriptella 1.5

Workflow: [Database compatibility run 34651591632](https://github.com/scriptella/scriptella-testcontainers/actions/runs/34651591632)

| Database | Container image | JDBC driver | Result | Notes |
| --- | --- | --- | --- | --- |
| PostgreSQL | `postgres:17.11-alpine3.24` | `42.7.13` | ✅ PASS | Core ETL copy and rollback contract passed; report artifact uploaded. |
| MariaDB | `mariadb:11.8.8` | `3.5.7` | ✅ PASS | Core ETL copy and rollback contract passed; report artifact uploaded. |
| Oracle Free | `gvenzl/oracle-free:23.26.2-slim-faststart` | `23.26.3.0.0` | ✅ PASS | Core ETL copy and rollback contract passed; report artifact uploaded. |
| SQL Server | `mcr.microsoft.com/mssql/server:2022-CU26-ubuntu-22.04` | `13.6.0.jre11` | ✅ PASS | Core ETL copy and rollback contract passed on Linux; report artifact uploaded. |

Add future selected runs as dated sections. Keep one row per database and
include the workflow link, image, driver version, result, and a short note.
