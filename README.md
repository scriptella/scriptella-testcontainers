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

Phase 1 establishes the build and resource layout. The live database smoke
tests will be added in the next phase.

Run the project from this directory with:

```shell
mvn verify
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

## Pinned targets

The database image pins are recorded in
`src/test/resources/database-images.properties`. Vendor JDBC drivers are
explicit test dependencies in `pom.xml`; they are not bundled with Scriptella.
