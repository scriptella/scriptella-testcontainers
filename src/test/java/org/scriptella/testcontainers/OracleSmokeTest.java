/*
 * Copyright 2026 The Scriptella Project Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scriptella.testcontainers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.DockerImageName;
import scriptella.execution.EtlExecutor;
import scriptella.execution.EtlExecutorException;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Runs the shared Scriptella compatibility-test contract against Oracle Free.
 */
@Testcontainers
@EnabledIfSystemProperty(named = "database", matches = "(?i)oracle|all")
class OracleSmokeTest {
    private static final String IMAGE = imageProperty("oracle.image");
    private static final String USER = "scriptella";
    private static final String PASSWORD = "scriptella";
    private static final LocalDateTime EXPECTED_TIMESTAMP = LocalDateTime.of(2025, 1, 2, 3, 4, 5);

    @Container
    private static final OracleContainer ORACLE = new OracleContainer(DockerImageName.parse(IMAGE))
            .withUsername(USER)
            .withPassword(PASSWORD)
            .withStartupTimeout(Duration.ofMinutes(10));

    @BeforeAll
    static void createSchemaAndSourceRows() throws SQLException, IOException {
        try (Connection connection = ORACLE.createConnection("")) {
            executeSqlScript(connection, "/sql/oracle/schema.sql");
            insertSourceRows(connection);
        }
    }

    @Test
    void copiesRepresentativeRowsAndRollsBackFailedTransaction() throws Exception {
        executeFixture("/etl/oracle-copy.etl.xml");

        try (Connection connection = ORACLE.createConnection("")) {
            assertDestinationRows(connection);
        }

        assertThrows(EtlExecutorException.class,
                () -> executeFixture("/etl/oracle-rollback.etl.xml"));

        try (Connection connection = ORACLE.createConnection("")) {
            assertEquals(3, countRows(connection, "etl_destination"));
            assertEquals(0, countRowsWhereId(connection, "etl_destination", 999));
        }
    }

    private static void executeFixture(String resource) throws Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put("source.url", ORACLE.getJdbcUrl());
        properties.put("source.user", ORACLE.getUsername());
        properties.put("source.password", ORACLE.getPassword());
        properties.put("destination.url", ORACLE.getJdbcUrl());
        properties.put("destination.user", ORACLE.getUsername());
        properties.put("destination.password", ORACLE.getPassword());

        EtlExecutor.newExecutor(resourceUrl(resource), properties).execute();
    }

    private static void insertSourceRows(Connection connection) throws SQLException {
        String sql = "INSERT INTO etl_source "
                + "(id, amount, description, unicode_text, nullable_value, happened_at) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            insertRow(statement, 1, "12345.67", "normal text", "こんにちは 🌍", null);
            insertRow(statement, 2, "0.01", "second row", "Привет мир", "optional value");
            insertRow(statement, 3, "-42.50", "third row", "مرحبا بالعالم", null);
        }
    }

    private static void insertRow(PreparedStatement statement, int id, String amount,
                                  String description, String unicodeText, String nullableValue)
            throws SQLException {
        statement.setInt(1, id);
        statement.setBigDecimal(2, new BigDecimal(amount));
        statement.setString(3, description);
        statement.setString(4, unicodeText);
        if (nullableValue == null) {
            statement.setNull(5, java.sql.Types.VARCHAR);
        } else {
            statement.setString(5, nullableValue);
        }
        statement.setTimestamp(6, Timestamp.valueOf(EXPECTED_TIMESTAMP));
        statement.executeUpdate();
    }

    private static void assertDestinationRows(Connection connection) throws SQLException {
        String sql = "SELECT id, amount, description, unicode_text, nullable_value, happened_at "
                + "FROM etl_destination ORDER BY id";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            assertRow(resultSet, 1, "12345.67", "normal text", "こんにちは 🌍", null);
            assertRow(resultSet, 2, "0.01", "second row", "Привет мир", "optional value");
            assertRow(resultSet, 3, "-42.50", "third row", "مرحبا بالعالم", null);
            if (resultSet.next()) {
                throw new AssertionError("Destination contains more than three rows");
            }
        }
    }

    private static void assertRow(ResultSet resultSet, int id, String amount, String description,
                                  String unicodeText, String nullableValue) throws SQLException {
        if (!resultSet.next()) {
            throw new AssertionError("Destination is missing row " + id);
        }
        assertEquals(id, resultSet.getInt("id"));
        assertEquals(0, new BigDecimal(amount).compareTo(resultSet.getBigDecimal("amount")));
        assertEquals(description, resultSet.getString("description"));
        assertEquals(unicodeText, resultSet.getString("unicode_text"));
        if (nullableValue == null) {
            assertNull(resultSet.getObject("nullable_value"));
        } else {
            assertEquals(nullableValue, resultSet.getString("nullable_value"));
        }
        assertEquals(Timestamp.valueOf(EXPECTED_TIMESTAMP), resultSet.getTimestamp("happened_at"));
    }

    private static int countRows(Connection connection, String table) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM " + table);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    private static int countRowsWhereId(Connection connection, String table, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM " + table + " WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    private static void executeSqlScript(Connection connection, String resource)
            throws IOException, SQLException {
        String script;
        try (InputStream input = resourceStream(resource)) {
            script = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        String executableSql = script.lines()
                .filter(line -> !line.stripLeading().startsWith("--"))
                .collect(Collectors.joining("\n"));
        for (String statementText : executableSql.split(";")) {
            String statement = statementText.trim();
            if (!statement.isEmpty()) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
                    preparedStatement.execute();
                }
            }
        }
    }

    private static java.net.URL resourceUrl(String resource) {
        java.net.URL url = OracleSmokeTest.class.getResource(resource);
        if (url == null) {
            throw new IllegalArgumentException("Missing test resource: " + resource);
        }
        return url;
    }

    private static InputStream resourceStream(String resource) {
        InputStream input = OracleSmokeTest.class.getResourceAsStream(resource);
        if (input == null) {
            throw new IllegalArgumentException("Missing test resource: " + resource);
        }
        return input;
    }

    private static String imageProperty(String name) {
        Properties properties = new Properties();
        try (InputStream input = resourceStream("/database-images.properties")) {
            properties.load(input);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
        String value = properties.getProperty(name);
        if (value == null || value.isBlank()) {
            throw new ExceptionInInitializerError("Missing image property: " + name);
        }
        return value;
    }
}
