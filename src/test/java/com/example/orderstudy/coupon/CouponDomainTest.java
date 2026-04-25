package com.example.orderstudy.coupon;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class CouponDomainTest {
    @Test
    void fixedAmountDiscountCannotMakeFinalPriceNegative() {
        CouponPolicy policy = new CouponPolicy(
                "5000원 할인",
                DiscountType.FIXED_AMOUNT,
                5_000,
                100,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1)
        );

        long discountAmount = policy.calculateDiscount(3_000);

        assertThat(discountAmount).isEqualTo(3_000);
    }

    @Test
    void percentageDiscountIsCalculatedFromOriginalPrice() {
        CouponPolicy policy = new CouponPolicy(
                "10% 할인",
                DiscountType.PERCENTAGE,
                10,
                100,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1)
        );

        long discountAmount = policy.calculateDiscount(200_000);

        assertThat(discountAmount).isEqualTo(20_000);
    }

    @Test
    void couponPolicyIsActiveOnStartAndEndBoundary() {
        LocalDateTime startedAt = LocalDateTime.of(2026, 5, 1, 10, 0);
        LocalDateTime endedAt = LocalDateTime.of(2026, 5, 31, 23, 59, 59);
        CouponPolicy policy = new CouponPolicy(
                "기간 쿠폰",
                DiscountType.FIXED_AMOUNT,
                1_000,
                100,
                startedAt,
                endedAt
        );

        assertThat(policy.isActive(startedAt)).isTrue();
        assertThat(policy.isActive(endedAt)).isTrue();
        assertThat(policy.isActive(startedAt.minusNanos(1))).isFalse();
        assertThat(policy.isActive(endedAt.plusNanos(1))).isFalse();
    }
}
