package com.piotrwalkusz.taskmanager.service;

import com.piotrwalkusz.taskmanager.config.DatabaseConfig;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;

/**
 * Base class for service tests with common database setup and cleanup
 */
abstract class BaseServiceTest {

    @TempDir
    static Path tempDir;

    protected static TaskService taskService;
    protected static WorkSessionService workSessionService;
    private static DatabaseConfig databaseConfig;

    @BeforeAll
    static void setupDatabase() {
        // Create single database for all tests in this class
        String testDbFile = tempDir.resolve("test-" + UUID.randomUUID() + ".db").toString();
        String dbUrl = "jdbc:sqlite:" + testDbFile;

        // Initialize database and services once
        databaseConfig = new DatabaseConfig(dbUrl);
        taskService = new TaskService(databaseConfig);
        workSessionService = new WorkSessionService(databaseConfig);
    }

    @BeforeEach
    void clearDatabase() {
        // Clear all data before each test
        try (SqlSession session = databaseConfig.getSqlSessionFactory().openSession()) {
            session.getConnection().createStatement().execute("DELETE FROM work_session");
            session.getConnection().createStatement().execute("DELETE FROM task");
            session.commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear database", e);
        }
    }

    @AfterAll
    static void closeDatabase() {
        // Close database connections to release file locks
        if (databaseConfig != null) {
            databaseConfig.close();
        }
    }
}
