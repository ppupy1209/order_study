package com.example.orderstudy.repository.coupon;

import com.example.orderstudy.domain.coupon.UserCoupon;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
    boolean existsByUserIdAndCouponPolicyId(Long userId, Long couponPolicyId);

    @EntityGraph(attributePaths = "couponPolicy")
    List<UserCoupon> findByUserIdOrderByIssuedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"user", "couponPolicy"})
    @Query("select uc from UserCoupon uc where uc.id = :id")
    Optional<UserCoupon> findWithUserAndPolicyById(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update UserCoupon uc
               set uc.status = com.example.orderstudy.domain.coupon.UserCouponStatus.USED,
                   uc.usedAt = :now
             where uc.id = :userCouponId
               and uc.user.id = :userId
               and uc.status = com.example.orderstudy.domain.coupon.UserCouponStatus.ISSUED
               and uc.expiredAt >= :now
            """)
    int markUsed(@Param("userCouponId") Long userCouponId, @Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update UserCoupon uc
               set uc.status = com.example.orderstudy.domain.coupon.UserCouponStatus.ISSUED,
                   uc.usedAt = null
             where uc.id = :userCouponId
               and uc.status = com.example.orderstudy.domain.coupon.UserCouponStatus.USED
            """)
    int restoreIssued(@Param("userCouponId") Long userCouponId);
}
