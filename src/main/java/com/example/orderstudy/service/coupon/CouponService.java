package com.example.orderstudy.service.coupon;

import com.example.orderstudy.domain.coupon.CouponPolicy;
import com.example.orderstudy.domain.coupon.UserCoupon;
import com.example.orderstudy.domain.user.User;
import com.example.orderstudy.dto.coupon.CouponPolicyResponse;
import com.example.orderstudy.dto.coupon.CreateCouponPolicyRequest;
import com.example.orderstudy.dto.coupon.IssueCouponRequest;
import com.example.orderstudy.dto.coupon.IssueCouponResponse;
import com.example.orderstudy.dto.coupon.UserCouponResponse;
import com.example.orderstudy.exception.BusinessException;
import com.example.orderstudy.exception.ErrorCode;
import com.example.orderstudy.repository.coupon.CouponPolicyRepository;
import com.example.orderstudy.repository.coupon.UserCouponRepository;
import com.example.orderstudy.repository.user.UserRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CouponService {
    private final CouponPolicyRepository couponPolicyRepository;
    private final UserCouponRepository userCouponRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public CouponService(CouponPolicyRepository couponPolicyRepository, UserCouponRepository userCouponRepository,
                         UserRepository userRepository, Clock clock) {
        this.couponPolicyRepository = couponPolicyRepository;
        this.userCouponRepository = userCouponRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Transactional
    public CouponPolicyResponse createPolicy(CreateCouponPolicyRequest request) {
        CouponPolicy policy = CouponPolicy.create(
                request.name(),
                request.discountType(),
                request.discountValue(),
                request.totalQuantity(),
                request.startedAt(),
                request.endedAt()
        );
        return CouponPolicyResponse.from(couponPolicyRepository.save(policy));
    }

    @Transactional
    public IssueCouponResponse issue(IssueCouponRequest request) {
        LocalDateTime now = LocalDateTime.now(clock);
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        CouponPolicy policy = couponPolicyRepository.findById(request.couponPolicyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_POLICY_NOT_FOUND));

        if (!policy.isActive(now)) {
            throw new BusinessException(ErrorCode.COUPON_POLICY_NOT_ACTIVE);
        }
        if (userCouponRepository.existsByUserIdAndCouponPolicyId(user.getId(), policy.getId())) {
            throw new BusinessException(ErrorCode.DUPLICATED_COUPON_ISSUE);
        }

        int updated = couponPolicyRepository.increaseIssuedQuantity(policy.getId(), now);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.COUPON_SOLD_OUT);
        }

        try {
            UserCoupon userCoupon = userCouponRepository.saveAndFlush(UserCoupon.issue(user, policy, now));
            return IssueCouponResponse.from(userCoupon);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.DUPLICATED_COUPON_ISSUE);
        }
    }

    @Transactional(readOnly = true)
    public List<UserCouponResponse> findUserCoupons(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return userCouponRepository.findByUserIdOrderByIssuedAtDesc(userId)
                .stream()
                .map(UserCouponResponse::from)
                .toList();
    }
}
