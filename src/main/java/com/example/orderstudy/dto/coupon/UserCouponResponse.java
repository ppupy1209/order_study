package com.example.orderstudy.dto.coupon;

import com.example.orderstudy.domain.coupon.UserCoupon;
import com.example.orderstudy.domain.coupon.UserCouponStatus;
import java.time.LocalDateTime;

public record UserCouponResponse(
        Long userCouponId,
        Long couponPolicyId,
        String couponName,
        UserCouponStatus status,
        LocalDateTime issuedAt,
        LocalDateTime usedAt,
        LocalDateTime expiredAt
) {
    public static UserCouponResponse from(UserCoupon userCoupon) {
        return new UserCouponResponse(
                userCoupon.getId(),
                userCoupon.getCouponPolicy().getId(),
                userCoupon.getCouponPolicy().getName(),
                userCoupon.getStatus(),
                userCoupon.getIssuedAt(),
                userCoupon.getUsedAt(),
                userCoupon.getExpiredAt()
        );
    }
}
