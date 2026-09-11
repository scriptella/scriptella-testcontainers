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

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prevents an invalid database selector from silently skipping every database
 * compatibility test.
 */
class DatabaseSelectorTest {
    private static final Set<String> SUPPORTED_DATABASES = Set.of(
            "postgresql", "mariadb", "oracle", "mssql", "all");

    @Test
    void selectorNamesAnAvailableDatabaseTarget() {
        String selector = System.getProperty("database", "postgresql")
                .toLowerCase(Locale.ROOT);
        assertTrue(SUPPORTED_DATABASES.contains(selector),
                () -> "Unknown database selector '" + selector + "'. Supported values: "
                        + SUPPORTED_DATABASES);
    }
}
