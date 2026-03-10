package com.example.msa.order.service;

import com.example.msa.order.api.OrderDetailResponse;
import com.example.msa.order.api.OrderSummaryResponse;
import com.example.msa.order.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrderQueryService {

    private final OrderRepository orderRepository;

    public OrderQueryService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Page<OrderSummaryResponse> getOrdersByMember(Long memberId, Pageable pageable) {
        return orderRepository.findAllByMemberId(memberId, pageable)
                .map(OrderSummaryResponse::from);
    }

    public OrderDetailResponse getOrderDetail(String orderNumber) {
        return orderRepository.findByOrderNumberWithItems(orderNumber)
                .map(OrderDetailResponse::from)
                .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다: " + orderNumber));
    }
}
