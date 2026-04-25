package com.example.orderstudy.dto.order;

import com.example.orderstudy.domain.order.Order;
import com.example.orderstudy.domain.order.OrderStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class OrderDtos {
    public record CreateOrderRequest(
            @NotNull Long userId,
            @NotNull Long productId,
            long quantity,
            Long userCouponId
    ) {
    }

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

    public record CancelOrderResponse(Long orderId, OrderStatus status, LocalDateTime canceledAt) {
    }
}
