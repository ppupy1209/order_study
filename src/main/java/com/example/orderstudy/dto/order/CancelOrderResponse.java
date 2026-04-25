package com.example.orderstudy.dto.order;

import com.example.orderstudy.domain.order.OrderStatus;
import java.time.LocalDateTime;

public record CancelOrderResponse(Long orderId, OrderStatus status, LocalDateTime canceledAt) {
}
