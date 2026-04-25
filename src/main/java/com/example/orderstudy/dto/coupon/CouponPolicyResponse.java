package com.example.orderstudy.dto.coupon;

import com.example.orderstudy.domain.coupon.CouponPolicy;
import com.example.orderstudy.domain.coupon.DiscountType;
import java.time.LocalDateTime;

public record CouponPolicyResponse(
        Long id,
        String name,
        DiscountType discountType,
        long discountValue,
        long totalQuantity,
        long issuedQuantity,
        LocalDateTime startedAt,
        LocalDateTime endedAt
) {
    public static CouponPolicyResponse from(CouponPolicy policy) {
        return new CouponPolicyResponse(
                policy.getId(),
                policy.getName(),
                policy.getDiscountType(),
                policy.getDiscountValue(),
                policy.getTotalQuantity(),
                policy.getIssuedQuantity(),
                policy.getStartedAt(),
                policy.getEndedAt()
        );
    }
}
