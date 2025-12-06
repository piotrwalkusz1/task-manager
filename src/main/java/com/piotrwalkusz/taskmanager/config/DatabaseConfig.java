package com.piotrwalkusz.taskmanager.config;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.flywaydb.core.Flyway;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Database configuration and initialization
 */
public class DatabaseConfig {

    private final SqlSessionFactory sqlSessionFactory;

    /**
     * Create DatabaseConfig with default database URL (system-specific location)
     */
    public DatabaseConfig() {
        this(getDefaultDatabasePath());
    }

    /**
     * Get default database path based on operating system
     * Windows: %LOCALAPPDATA%\TaskManager\taskmanager.db
     * Linux: ~/.local/share/TaskManager/taskmanager.db
     */
    private static String getDefaultDatabasePath() {
        String os = System.getProperty("os.name").toLowerCase();
        Path dataDir;

        if (os.contains("win")) {
            // Windows: use LOCALAPPDATA
            String localAppData = System.getenv("LOCALAPPDATA");
            if (localAppData == null) {
                localAppData = System.getProperty("user.home") + "\\AppData\\Local";
            }
            dataDir = Paths.get(localAppData, "TaskManager");
        } else {
            // Linux/Unix: use XDG Base Directory
            String home = System.getProperty("user.home");
            dataDir = Paths.get(home, ".local", "share", "TaskManager");
        }

        // Create directory if it doesn't exist
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create data directory: " + dataDir, e);
        }

        Path dbPath = dataDir.resolve("taskmanager.db");
        return "jdbc:sqlite:" + dbPath.toString();
    }

    /**
     * Create DatabaseConfig with custom database URL
     */
    public DatabaseConfig(String dbUrl) {
        // Enable foreign keys for SQLite
        String dbUrlWithForeignKeys = dbUrl + "?foreign_keys=on";

        // Run Flyway migrations
        Flyway flyway = Flyway.configure()
                .dataSource(dbUrlWithForeignKeys, null, null)
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();

        // Initialize MyBatis with URL override
        try (InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml")) {
            Properties properties = new Properties();
            properties.setProperty("url", dbUrlWithForeignKeys);
            this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream, properties);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize MyBatis", e);
        }
    }

    /**
     * Get MyBatis SqlSessionFactory
     */
    public SqlSessionFactory getSqlSessionFactory() {
        return sqlSessionFactory;
    }
}
