package com.example.orderstudy.dto.order;

import com.example.orderstudy.domain.order.Order;
import com.example.orderstudy.domain.order.OrderStatus;

public record CreateOrderResponse(
        Long orderId,
        Long userId,
        Long productId,
        long quantity,
        long originalPrice,
        long discountAmount,
        long finalPrice,
        OrderStatus status
) {
    public static CreateOrderResponse from(Order order) {
        return new CreateOrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getProduct().getId(),
                order.getQuantity(),
                order.getOriginalPrice(),
                order.getDiscountAmount(),
                order.getFinalPrice(),
                order.getStatus()
        );
    }
}
