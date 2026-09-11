-- Oracle-specific tables used by the copy and rollback fixtures.
CREATE TABLE etl_source (
    id NUMBER(10) PRIMARY KEY,
    amount NUMBER(12, 2) NOT NULL,
    description VARCHAR2(200 CHAR) NOT NULL,
    unicode_text VARCHAR2(200 CHAR) NOT NULL,
    nullable_value VARCHAR2(200 CHAR),
    happened_at TIMESTAMP(0) NOT NULL
);

CREATE TABLE etl_destination (
    id NUMBER(10) PRIMARY KEY,
    amount NUMBER(12, 2) NOT NULL,
    description VARCHAR2(200 CHAR) NOT NULL,
    unicode_text VARCHAR2(200 CHAR) NOT NULL,
    nullable_value VARCHAR2(200 CHAR),
    happened_at TIMESTAMP(0) NOT NULL
);
