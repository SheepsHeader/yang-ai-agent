package com.example.yangaiagent.Config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * PgVector 专用 PostgreSQL 数据源配置。
 * 从 application.yml 的 pgvector.datasource 段读取连接信息，创建独立的 DataSource 和 JdbcTemplate，
 * 避免与默认 MySQL 数据源（spring.datasource）混淆。
 */
@Configuration
public class PgVectorDataSourceConfig {

    /**
     * 先用 DataSourceProperties 承接 yml 中的 url/username/password/driver-class-name，
     * 再通过 DataSourceBuilder 创建 DataSource。这样可以避免 HikariCP 的 url vs jdbcUrl 属性名不匹配问题。
     */
    @Bean
    @ConfigurationProperties(prefix = "pgvector.datasource")
    public DataSourceProperties pgVectorDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource pgVectorDataSource(DataSourceProperties pgVectorDataSourceProperties) {
        return pgVectorDataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Bean
    public JdbcTemplate pgVectorJdbcTemplate(DataSource pgVectorDataSource) {
        return new JdbcTemplate(pgVectorDataSource);
    }
}
