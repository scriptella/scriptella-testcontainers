-- MariaDB-specific tables used by the copy and rollback fixtures.
-- Explicit utf8mb4 columns preserve all representative Unicode values.
DROP TABLE IF EXISTS etl_destination;
DROP TABLE IF EXISTS etl_source;

CREATE TABLE etl_source (
    id INTEGER PRIMARY KEY,
    amount DECIMAL(12, 2) NOT NULL,
    description VARCHAR(200) NOT NULL,
    unicode_text VARCHAR(200) CHARACTER SET utf8mb4 NOT NULL,
    nullable_value VARCHAR(200),
    happened_at DATETIME NOT NULL
);

CREATE TABLE etl_destination (
    id INTEGER PRIMARY KEY,
    amount DECIMAL(12, 2) NOT NULL,
    description VARCHAR(200) NOT NULL,
    unicode_text VARCHAR(200) CHARACTER SET utf8mb4 NOT NULL,
    nullable_value VARCHAR(200),
    happened_at DATETIME NOT NULL
);
