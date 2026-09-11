-- SQL Server-specific tables used by the copy and rollback fixtures.
IF OBJECT_ID('etl_destination', 'U') IS NOT NULL DROP TABLE etl_destination;
IF OBJECT_ID('etl_source', 'U') IS NOT NULL DROP TABLE etl_source;

CREATE TABLE etl_source (
    id INT PRIMARY KEY,
    amount DECIMAL(12, 2) NOT NULL,
    description NVARCHAR(200) NOT NULL,
    unicode_text NVARCHAR(200) NOT NULL,
    nullable_value NVARCHAR(200),
    happened_at DATETIME2(0) NOT NULL
);

CREATE TABLE etl_destination (
    id INT PRIMARY KEY,
    amount DECIMAL(12, 2) NOT NULL,
    description NVARCHAR(200) NOT NULL,
    unicode_text NVARCHAR(200) NOT NULL,
    nullable_value NVARCHAR(200),
    happened_at DATETIME2(0) NOT NULL
);
