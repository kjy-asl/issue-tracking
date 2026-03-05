package com.example.msa.posrelay.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * order-db의 order_outbox 테이블에 읽기 전용으로 접근하는 Repository.
 * JPA 대신 JdbcTemplate을 사용하여 멀티 DataSource 복잡도를 최소화한다.
 */
@Repository
public class OrderOutboxReadRepository {

    private final JdbcTemplate orderJdbcTemplate;

    public OrderOutboxReadRepository(@Qualifier("orderJdbcTemplate") JdbcTemplate orderJdbcTemplate) {
        this.orderJdbcTemplate = orderJdbcTemplate;
    }

    public List<OrderOutboxEntry> findAfter(LocalDateTime lastRelayedAt, int limit) {
        String sql = "SELECT event_id, payload, created_at " +
                     "FROM order_outbox " +
                     "WHERE created_at > ? " +
                     "ORDER BY created_at ASC " +
                     "LIMIT ?";
        return orderJdbcTemplate.query(sql,
                (rs, rowNum) -> new OrderOutboxEntry(
                        rs.getString("event_id"),
                        rs.getString("payload"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                ),
                lastRelayedAt, limit);
    }
}
