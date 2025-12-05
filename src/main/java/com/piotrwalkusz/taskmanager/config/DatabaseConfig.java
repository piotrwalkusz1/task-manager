package com.piotrwalkusz.taskmanager.config;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.flywaydb.core.Flyway;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Database configuration and initialization
 */
public class DatabaseConfig {

    private static final String DEFAULT_DB_URL = "jdbc:sqlite:taskmanager.db";
    private final SqlSessionFactory sqlSessionFactory;

    /**
     * Create DatabaseConfig with default database URL
     */
    public DatabaseConfig() {
        this(DEFAULT_DB_URL);
    }

    /**
     * Create DatabaseConfig with custom database URL
     */
    public DatabaseConfig(String dbUrl) {
        // Run Flyway migrations
        Flyway flyway = Flyway.configure()
                .dataSource(dbUrl, null, null)
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();

        // Initialize MyBatis with URL override
        try (InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml")) {
            Properties properties = new Properties();
            properties.setProperty("url", dbUrl);
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
