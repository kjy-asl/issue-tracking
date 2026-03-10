package com.example.msa.order.api;

import com.example.msa.order.service.OrderQueryService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderQueryController {

    private final OrderQueryService orderQueryService;

    public OrderQueryController(OrderQueryService orderQueryService) {
        this.orderQueryService = orderQueryService;
    }

    @GetMapping
    public ResponseEntity<Page<OrderSummaryResponse>> getOrdersByMember(
            @RequestParam Long memberId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(orderQueryService.getOrdersByMember(memberId, pageable));
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(@PathVariable String orderNumber) {
        try {
            return ResponseEntity.ok(orderQueryService.getOrderDetail(orderNumber));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
