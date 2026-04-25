package com.example.orderstudy.dto.coupon;

import com.example.orderstudy.domain.coupon.CouponPolicy;
import com.example.orderstudy.domain.coupon.DiscountType;
import com.example.orderstudy.domain.coupon.UserCoupon;
import com.example.orderstudy.domain.coupon.UserCouponStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import java.time.LocalDateTime;

public class CouponDtos {
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

    public record IssueCouponRequest(@NotNull Long userId, @NotNull Long couponPolicyId) {
    }

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
}
