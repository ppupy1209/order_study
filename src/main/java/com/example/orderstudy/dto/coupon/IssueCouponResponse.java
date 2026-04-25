package com.example.orderstudy.dto.coupon;

import com.example.orderstudy.domain.coupon.UserCoupon;
import com.example.orderstudy.domain.coupon.UserCouponStatus;
import java.time.LocalDateTime;

public record IssueCouponResponse(
        Long userCouponId,
        Long userId,
        Long couponPolicyId,
        UserCouponStatus status,
        LocalDateTime issuedAt,
        LocalDateTime expiredAt
) {
    public static IssueCouponResponse from(UserCoupon userCoupon) {
        return new IssueCouponResponse(
                userCoupon.getId(),
                userCoupon.getUser().getId(),
                userCoupon.getCouponPolicy().getId(),
                userCoupon.getStatus(),
                userCoupon.getIssuedAt(),
                userCoupon.getExpiredAt()
        );
    }
}
