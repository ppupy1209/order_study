package com.example.orderstudy.dto.coupon;

import jakarta.validation.constraints.NotNull;

public record IssueCouponRequest(@NotNull Long userId, @NotNull Long couponPolicyId) {
}
