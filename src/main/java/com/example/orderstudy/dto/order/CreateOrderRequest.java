package com.example.orderstudy.dto.order;

import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
        @NotNull Long userId,
        @NotNull Long productId,
        long quantity,
        Long userCouponId
) {
}
