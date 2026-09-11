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

### Local macOS with Colima

Colima is the preferred Docker runtime for local macOS development. The
complete suite includes resource-intensive Oracle Free and SQL Server images;
the recommended shared VM configuration is:

```shell
colima start --cpus 4 --memory 4 --disk 20
```

Colima can increase an existing VM's disk size but cannot shrink it. Point
Testcontainers at Colima's socket before running the suite:

```shell
export DOCKER_HOST="unix://${HOME}/.colima/default/docker.sock"
export TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock
```

PostgreSQL, MariaDB, and Oracle Free currently implement the shared smoke-test
contract. The SQL Server target will be added in a later phase.

Run the project from this directory with:

```shell
mvn verify
```

Run the PostgreSQL target explicitly with:

```shell
mvn verify -Ddatabase=postgresql
```

Run MariaDB by itself with:

```shell
mvn verify -Ddatabase=mariadb
```

Run Oracle Free by itself with:

```shell
mvn verify -Ddatabase=oracle
```

Run all currently implemented database tests sequentially with:

```shell
mvn verify -Ddatabase=all
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

Each rollback fixture deliberately performs one valid write and then fails on
a second write. The test checks through a fresh JDBC connection that Scriptella
rolled back the first write as well. New database tests should keep this
separation: use Scriptella for the ETL behavior and the vendor driver for
independent setup and assertions.

## Pinned targets

The database image pins are recorded in
`src/test/resources/database-images.properties`. Vendor JDBC drivers are
explicit test dependencies in `pom.xml`; they are not bundled with Scriptella.
