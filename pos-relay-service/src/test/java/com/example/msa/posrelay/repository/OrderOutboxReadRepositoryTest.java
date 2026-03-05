package com.example.msa.posrelay.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderOutboxReadRepositoryTest {

    @Mock JdbcTemplate orderJdbcTemplate;

    OrderOutboxReadRepository repository;

    @BeforeEach
    void setUp() {
        repository = new OrderOutboxReadRepository(orderJdbcTemplate);
    }

    @Test
    void findAfter_파라미터_정확히_전달() {
        LocalDateTime since = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        when(orderJdbcTemplate.query(anyString(), any(RowMapper.class), any(), anyInt()))
                .thenReturn(List.of());

        repository.findAfter(since, 50);

        verify(orderJdbcTemplate).query(anyString(), any(RowMapper.class), eq(since), eq(50));
    }

    @Test
    void findAfter_결과_리스트_반환() {
        LocalDateTime since = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        LocalDateTime createdAt = since.plusSeconds(10);

        OrderOutboxEntry expected = new OrderOutboxEntry("evt-1", "{\"orderId\":1}", createdAt);
        when(orderJdbcTemplate.query(anyString(), any(RowMapper.class), any(), anyInt()))
                .thenReturn(List.of(expected));

        List<OrderOutboxEntry> result = repository.findAfter(since, 50);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).eventId()).isEqualTo("evt-1");
        assertThat(result.get(0).payload()).isEqualTo("{\"orderId\":1}");
        assertThat(result.get(0).createdAt()).isEqualTo(createdAt);
    }

    @Test
    void findAfter_결과없으면_빈리스트() {
        LocalDateTime since = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        when(orderJdbcTemplate.query(anyString(), any(RowMapper.class), any(), anyInt()))
                .thenReturn(List.of());

        List<OrderOutboxEntry> result = repository.findAfter(since, 50);

        assertThat(result).isEmpty();
    }

    @Test
    void findAfter_SQL에_created_at_조건_포함() {
        LocalDateTime since = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        when(orderJdbcTemplate.query(anyString(), any(RowMapper.class), any(), anyInt()))
                .thenReturn(List.of());

        repository.findAfter(since, 10);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(orderJdbcTemplate).query(sqlCaptor.capture(), any(RowMapper.class), any(), anyInt());
        assertThat(sqlCaptor.getValue())
                .containsIgnoringCase("order_outbox")
                .containsIgnoringCase("created_at");
    }
}
