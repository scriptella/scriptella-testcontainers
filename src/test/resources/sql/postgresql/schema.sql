-- PostgreSQL-specific tables used by the copy and rollback fixtures.
-- The source table is seeded by the Java test; Scriptella copies it to the
-- destination table through the checked-in ETL fixture.
DROP TABLE IF EXISTS etl_destination;
DROP TABLE IF EXISTS etl_source;

CREATE TABLE etl_source (
    id INTEGER PRIMARY KEY,
    amount NUMERIC(12, 2) NOT NULL,
    description VARCHAR(200) NOT NULL,
    unicode_text VARCHAR(200) NOT NULL,
    nullable_value VARCHAR(200),
    happened_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE TABLE etl_destination (
    id INTEGER PRIMARY KEY,
    amount NUMERIC(12, 2) NOT NULL,
    description VARCHAR(200) NOT NULL,
    unicode_text VARCHAR(200) NOT NULL,
    nullable_value VARCHAR(200),
    happened_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);
