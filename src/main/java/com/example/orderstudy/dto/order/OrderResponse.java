package com.example.orderstudy.dto.order;

import com.example.orderstudy.domain.order.Order;
import com.example.orderstudy.domain.order.OrderStatus;
import java.time.LocalDateTime;

public record OrderResponse(
        Long orderId,
        Long userId,
        Long productId,
        Long userCouponId,
        long quantity,
        long originalPrice,
        long discountAmount,
        long finalPrice,
        OrderStatus status,
        LocalDateTime createdAt,
        LocalDateTime canceledAt
) {
    public static OrderResponse from(Order order) {
        Long userCouponId = order.getUserCoupon() == null ? null : order.getUserCoupon().getId();
        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getProduct().getId(),
                userCouponId,
                order.getQuantity(),
                order.getOriginalPrice(),
                order.getDiscountAmount(),
                order.getFinalPrice(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getCanceledAt()
        );
    }
}
