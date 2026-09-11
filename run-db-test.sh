#!/usr/bin/env bash

set -euo pipefail

usage() {
    echo "Usage: $0 {postgresql|mariadb|oracle|mssql|all}" >&2
}

help() {
    cat <<'EOF'
Usage: ./run-db-test.sh {postgresql|mariadb|oracle|mssql|all} [MAVEN_ARGS...]

Run one Scriptella database compatibility test and save its output under reports/.
The all selector runs PostgreSQL, MariaDB, Oracle, and SQL Server sequentially.

Examples:
  ./run-db-test.sh postgresql
  ./run-db-test.sh all
  ./run-db-test.sh postgresql -Dscriptella.version=1.6-SNAPSHOT
EOF
}

if [[ $# -ge 1 && ( "$1" == --help || "$1" == -h ) ]]; then
    help
    exit 0
fi

if [[ $# -lt 1 ]]; then
    usage
    exit 2
fi

database_selector="$1"
shift

case "$database_selector" in
    postgresql|mariadb|oracle|mssql|all)
        ;;
    *)
    usage
    exit 2
    ;;
esac

for argument in "$@"; do
    case "$argument" in
        -Ddatabase|-Ddatabase=*)
            echo "Error: -Ddatabase is owned by run-db-test.sh; select the database as the first argument." >&2
            exit 2
            ;;
    esac
done

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
cd "$script_dir"

property_value() {
    local key="$1"
    awk -F= -v property_key="$key" \
        '$1 == property_key {print substr($0, index($0, "=") + 1); exit}' \
        src/test/resources/database-images.properties
}

pom_property() {
    local key="$1"
    local value
    local evaluate_result
    shift
    set +e
    value="$(mvn help:evaluate -q -DforceStdout "-Dexpression=${key}" "$@" 2>/dev/null | tail -n 1)"
    evaluate_result=$?
    set -e
    if [[ $evaluate_result -ne 0 || -z "$value" || "$value" == \[ERROR\]* ]]; then
        return 1
    fi
    printf '%s\n' "$value"
}

scriptella_version=""
if ! scriptella_version="$(pom_property scriptella.version "$@")"; then
    scriptella_version="unknown"
fi

run_database() {
    local database="$1"
    local report="reports/${database}.log"
    local result
    local image
    local jdbc_driver_version
    local scriptella_version_for_run
    local -a maven_command
    shift

    mkdir -p reports
    image="$(property_value "${database}.image")"
    if ! jdbc_driver_version="$(pom_property "${database}.jdbc.version" "$@")"; then
        jdbc_driver_version="unknown"
    fi
    scriptella_version_for_run="$scriptella_version"
    # Keep the wrapper's selector last so forwarded -Ddatabase values cannot
    # make the report identity disagree with the database Maven executes.
    maven_command=(mvn verify "$@" "-Ddatabase=${database}")

    {
        echo "Scriptella Database Compatibility Test"
        echo "Database: ${database}"
        echo "Container image: ${image}"
        echo "Scriptella version: ${scriptella_version_for_run}"
        echo "JDBC driver version: ${jdbc_driver_version}"
        echo "Started: $(date -u '+%Y-%m-%dT%H:%M:%SZ')"
        printf 'Command:'
        printf ' %q' "${maven_command[@]}"
        echo
        echo

        set +e
        "${maven_command[@]}"
        result=$?
        set -e

        echo
        if [[ $result -eq 0 ]]; then
            echo "Result: PASS"
        else
            echo "Result: FAIL"
        fi
        echo "Finished: $(date -u '+%Y-%m-%dT%H:%M:%SZ')"
        exit "$result"
    } 2>&1 | tee "$report"
}

if [[ "$database_selector" == all ]]; then
    run_database postgresql "$@"
    run_database mariadb "$@"
    run_database oracle "$@"
    run_database mssql "$@"
else
    run_database "$database_selector" "$@"
fi
