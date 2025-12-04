package com.piotrwalkusz.taskmanager.config;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.flywaydb.core.Flyway;

import java.io.IOException;
import java.io.InputStream;

/**
 * Database configuration and initialization
 */
public class DatabaseConfig {

    private static final String DB_URL = "jdbc:sqlite:taskmanager.db";
    private static SqlSessionFactory sqlSessionFactory;

    /**
     * Initialize database with Flyway migrations and MyBatis
     */
    public static void initialize() {
        // Run Flyway migrations
        Flyway flyway = Flyway.configure()
                .dataSource(DB_URL, null, null)
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();

        // Initialize MyBatis
        try (InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml")) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize MyBatis", e);
        }
    }

    /**
     * Get MyBatis SqlSessionFactory
     */
    public static SqlSessionFactory getSqlSessionFactory() {
        if (sqlSessionFactory == null) {
            initialize();
        }
        return sqlSessionFactory;
    }
}
