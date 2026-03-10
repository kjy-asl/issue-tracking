package com.example.msa.order;

import com.example.msa.order.api.OrderDetailResponse;
import com.example.msa.order.api.OrderSummaryResponse;
import com.example.msa.order.domain.Order;
import com.example.msa.order.domain.OrderItem;
import com.example.msa.order.repository.OrderRepository;
import com.example.msa.order.service.OrderQueryService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderQueryServiceTest {

    @Mock
    OrderRepository orderRepository;

    @InjectMocks
    OrderQueryService orderQueryService;

    @Test
    void getOrdersByMember_회원별_주문목록_반환() {
        Order order = new Order("ORD-001", 1L, "CREATED");
        ReflectionTestUtils.setField(order, "id", 1L);
        order.addItem(new OrderItem("PROD-001", 2, BigDecimal.valueOf(5000)));
        order.recalculateTotal();

        PageRequest pageable = PageRequest.of(0, 20);
        when(orderRepository.findAllByMemberId(eq(1L), any()))
                .thenReturn(new PageImpl<>(List.of(order), pageable, 1));

        Page<OrderSummaryResponse> result = orderQueryService.getOrdersByMember(1L, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        OrderSummaryResponse summary = result.getContent().get(0);
        assertThat(summary.orderNumber()).isEqualTo("ORD-001");
        assertThat(summary.memberId()).isEqualTo(1L);
        assertThat(summary.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(summary.status()).isEqualTo("CREATED");
    }

    @Test
    void getOrderDetail_주문번호로_상세_반환() {
        Order order = new Order("ORD-002", 2L, "CREATED");
        ReflectionTestUtils.setField(order, "id", 2L);
        order.addItem(new OrderItem("BOOK-001", 1, BigDecimal.valueOf(15000)));
        order.recalculateTotal();

        when(orderRepository.findByOrderNumberWithItems("ORD-002"))
                .thenReturn(Optional.of(order));

        OrderDetailResponse detail = orderQueryService.getOrderDetail("ORD-002");

        assertThat(detail.orderNumber()).isEqualTo("ORD-002");
        assertThat(detail.memberId()).isEqualTo(2L);
        assertThat(detail.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(15000));
        assertThat(detail.items()).hasSize(1);
        assertThat(detail.items().get(0).productId()).isEqualTo("BOOK-001");
        assertThat(detail.items().get(0).lineTotal()).isEqualByComparingTo(BigDecimal.valueOf(15000));
    }

    @Test
    void getOrderDetail_없는주문번호_예외발생() {
        when(orderRepository.findByOrderNumberWithItems("INVALID"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderQueryService.getOrderDetail("INVALID"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("INVALID");
    }
}
