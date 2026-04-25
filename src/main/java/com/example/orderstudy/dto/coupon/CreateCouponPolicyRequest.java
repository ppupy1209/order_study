package com.example.orderstudy.dto.coupon;

import com.example.orderstudy.domain.coupon.DiscountType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CreateCouponPolicyRequest(
        @NotBlank String name,
        @NotNull DiscountType discountType,
        @Min(0) long discountValue,
        @Min(1) long totalQuantity,
        @NotNull LocalDateTime startedAt,
        @NotNull LocalDateTime endedAt
) {
    @AssertTrue(message = "쿠폰 종료 시간은 시작 시간보다 이후여야 합니다.")
    public boolean isValidPeriod() {
        return startedAt == null || endedAt == null || endedAt.isAfter(startedAt);
    }

    @AssertTrue(message = "정률 할인 값은 0부터 100 사이여야 합니다.")
    public boolean isValidPercentageDiscount() {
        return discountType != DiscountType.PERCENTAGE || (discountValue >= 0 && discountValue <= 100);
    }
}
