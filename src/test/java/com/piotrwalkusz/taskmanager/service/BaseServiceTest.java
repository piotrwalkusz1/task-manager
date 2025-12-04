package com.piotrwalkusz.taskmanager.service;

import com.piotrwalkusz.taskmanager.config.DatabaseConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Base class for service tests with common database setup and cleanup
 */
abstract class BaseServiceTest {

    @TempDir
    Path tempDir;

    protected TaskService taskService;
    protected WorkSessionService workSessionService;
    private DatabaseConfig databaseConfig;
    private File testDbFile;

    @BeforeEach
    void setupDatabase() {
        // Create unique database for each test
        testDbFile = tempDir.resolve("test-" + UUID.randomUUID() + ".db").toFile();
        String dbUrl = "jdbc:sqlite:" + testDbFile.getAbsolutePath();

        // Initialize database and services
        databaseConfig = new DatabaseConfig(dbUrl);
        taskService = new TaskService(databaseConfig);
        workSessionService = new WorkSessionService(databaseConfig);
    }

    @AfterEach
    void cleanupDatabase() {
        // Delete test database file after each test
        if (testDbFile != null && testDbFile.exists()) {
            testDbFile.delete();
        }
    }
}
