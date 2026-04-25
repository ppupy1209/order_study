package com.example.orderstudy.coupon;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponPolicyRepository extends JpaRepository<CouponPolicy, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update CouponPolicy cp
               set cp.issuedQuantity = cp.issuedQuantity + 1
             where cp.id = :couponPolicyId
               and cp.issuedQuantity < cp.totalQuantity
               and cp.startedAt <= :now
               and cp.endedAt >= :now
            """)
    int increaseIssuedQuantity(@Param("couponPolicyId") Long couponPolicyId, @Param("now") LocalDateTime now);
}
