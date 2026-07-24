# 多数据源下 JdbcTemplate 注入冲突的解决

## 问题场景

Spring Boot 项目同时配置了多个数据源（如 MySQL 主数据源 + PgVector PostgreSQL 数据源），
当某个 Bean 需要注入 `JdbcTemplate` 时，Spring 默认注入的是 `spring.datasource` 对应的
默认 `JdbcTemplate`，而不是业务期望的那个数据源。

## 典型症状

- 向量存储（PgVector）的 Bean 拿到的是 MySQL 的 `JdbcTemplate`
- 启动时 JDBC 连接失败，报 `Communications link failure` / `Connection refused`
- 或者连接成功但运行时 SQL 语法不兼容（MySQL vs PostgreSQL）

## 解决方案

### 1. 为附加数据源创建独立的 DataSource 和 JdbcTemplate

```java
@Configuration
public class PgVectorDataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "pgvector.datasource")   // 绑定 yml 中的配置段
    public DataSource pgVectorDataSource() {
        return new HikariDataSource();
    }

    @Bean
    public JdbcTemplate pgVectorJdbcTemplate(DataSource pgVectorDataSource) {
        return new JdbcTemplate(pgVectorDataSource);            // @Bean 方法名 = bean 名称
    }
}
```

配合 `application.yml`：

```yaml
# 独立数据源配置段
pgvector:
  datasource:
    url: jdbc:postgresql://localhost:5432/yu_ai_agent
    username: postgresql
    password: password
    driver-class-name: org.postgresql.Driver
```

### 2. 注入时使用 @Qualifier 指定具体 Bean

```java
@Bean
public VectorStore pgVectorVectorStore(
        @Qualifier("pgVectorJdbcTemplate") JdbcTemplate jdbcTemplate,  // 指定 PgVector 的 JdbcTemplate
        EmbeddingModel dashscopeEmbeddingModel) {
    return PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
            .build();
}
```

### 3. 如果需要多个 Qualifier，参数名也可能参与歧义

如果注入的参数名恰好与某个 Bean 名称匹配，Spring 会优先按名称注入，此时即使不加
`@Qualifier` 也能正确匹配。但这个行为依赖参数命名，不够显式，**推荐始终加 `@Qualifier`**。

## 关键点

| 要点 | 说明 |
|------|------|
| `@ConfigurationProperties(prefix = "...")` | 将 yml 中对应前缀的属性绑定到 DataSource |
| `@Bean` 方法名 | 默认即 bean 名称，供 `@Qualifier` 引用 |
| 默认数据源 | `spring.datasource.*` 是 Spring Boot 自动配置的主数据源，不要覆盖 |
| 附加数据源 | 单独写 Config 类，手动创建 DataSource 和 JdbcTemplate |
