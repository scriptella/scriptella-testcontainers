# Scriptella Testcontainers

This project is a small public compatibility smoke suite for Scriptella. It
will run checked-in Scriptella ETL fixtures against real containerized
PostgreSQL, MariaDB, Oracle Free, and Microsoft SQL Server databases.

The suite targets Java 17 and uses one Maven module. Database tests run
sequentially on developer machines; CI will run each database in its own Linux
job. SQL Server is not supported locally on Apple Silicon.

## Prerequisites

- Java 17 or later;
- Maven 3.9 or later; and
- a Testcontainers-compatible Docker runtime.

The PostgreSQL smoke test is the first Phase 2 target. The other database
targets will be added in later phases.

Run the project from this directory with:

```shell
mvn verify
```

Run the PostgreSQL target explicitly with:

```shell
mvn verify -Ddatabase=postgresql
```

The default `scriptella.version` is the latest stable Scriptella version used
by the public suite. To test a locally installed development snapshot, first
install that snapshot from a sibling `scriptella-etl` checkout, then override
the property:

```shell
mvn -f ../scriptella-etl/pom.xml install -DskipTests
mvn verify -Dscriptella.version=1.6-SNAPSHOT
```

The override selects the same version for both `scriptella-core` and
`scriptella-drivers`.

## Test layout

Each database target has three small pieces:

- a JUnit test under `src/test/java` that starts one shared container, seeds
  and verifies data with the vendor JDBC driver, and runs the Scriptella ETL;
- one or more ETL fixtures under `src/test/resources/etl` showing the
  database connection alias and row-copy pattern; and
- database-specific setup SQL under `src/test/resources/sql`.

The PostgreSQL rollback fixture deliberately performs one valid write and then
fails on a second write. The test checks through a fresh JDBC connection that
Scriptella rolled back the first write as well. New database tests should keep
this separation: use Scriptella for the ETL behavior and the vendor driver for
independent setup and assertions.

## Pinned targets

The database image pins are recorded in
`src/test/resources/database-images.properties`. Vendor JDBC drivers are
explicit test dependencies in `pom.xml`; they are not bundled with Scriptella.
