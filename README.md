# Scriptella Testcontainers

Scriptella Testcontainers is a public real-database compatibility suite for
Scriptella. It uses Testcontainers to run meaningful ETL compatibility tests
against PostgreSQL, MariaDB, Oracle Free, and Microsoft SQL Server.

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

PostgreSQL, MariaDB, Oracle Free, and Microsoft SQL Server implement the shared
compatibility-test contract. SQL Server is intended for Linux CI and is not
supported locally on Apple Silicon.

These public compatibility tests exercise core Scriptella ETL behavior against
real databases. They are intentionally narrower than the project's
comprehensive internal compatibility certification.

Run the project from this directory with the wrapper (each run writes a
human-readable log under `reports/`):

```shell
./run-db-test.sh postgresql
./run-db-test.sh mariadb
./run-db-test.sh oracle
./run-db-test.sh mssql
./run-db-test.sh all
```

The `all` command runs PostgreSQL, MariaDB, Oracle Free, and SQL Server
sequentially, producing one report file per database. SQL Server remains
intended for Linux and is unsupported locally on Apple Silicon; on those Macs,
run the first three wrapper commands separately.

Extra Maven arguments are forwarded, which makes snapshot testing convenient:

```shell
./run-db-test.sh postgresql -Dscriptella.version=1.6-SNAPSHOT
```

Each report records the database, pinned container image, effective Scriptella
version, and JDBC driver version before the Maven output. The configured
`scriptella.version` and driver properties are resolved from Maven, including
any command-line overrides.

For direct Maven debugging, the underlying commands remain available. For
example, run PostgreSQL with:

```shell
mvn verify -Ddatabase=postgresql
```

Run MariaDB directly with:

```shell
mvn verify -Ddatabase=mariadb
```

Run Oracle Free directly with:

```shell
mvn verify -Ddatabase=oracle
```

Run Microsoft SQL Server directly on Linux with:

```shell
mvn verify -Ddatabase=mssql
```

Run all four database tests directly on Linux or another host that supports the
SQL Server container with:

```shell
mvn verify -Ddatabase=all
```

On Apple Silicon, the direct Maven equivalents for the three supported local
targets are:

```shell
mvn verify -Ddatabase=postgresql
mvn verify -Ddatabase=mariadb
mvn verify -Ddatabase=oracle
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
