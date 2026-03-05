package com.example.msa.posrelay.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * 멀티 DataSource 설정.
 * - relayDataSource (Primary): relay-db, JPA + Flyway 대상 (relay_pointer 관리)
 * - orderDataSource: order-db, JdbcTemplate 읽기 전용 (order_outbox 폴링)
 */
@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource relayDataSource(
            @Value("${pos.datasource.relay.url}") String url,
            @Value("${pos.datasource.relay.username}") String username,
            @Value("${pos.datasource.relay.password}") String password) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setPoolName("relay-pool");
        return ds;
    }

    @Bean
    public DataSource orderDataSource(
            @Value("${pos.datasource.order.url}") String url,
            @Value("${pos.datasource.order.username}") String username,
            @Value("${pos.datasource.order.password}") String password) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setReadOnly(true);
        ds.setPoolName("order-readonly-pool");
        return ds;
    }

    @Bean("orderJdbcTemplate")
    public JdbcTemplate orderJdbcTemplate(@Qualifier("orderDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
