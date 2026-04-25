package com.example.orderstudy.domain.coupon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Check(constraints = "issued_quantity <= total_quantity")
public class CouponPolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DiscountType discountType;

    @Column(nullable = false)
    private long discountValue;

    @Column(nullable = false)
    private long totalQuantity;

    @Column(nullable = false)
    private long issuedQuantity;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column(nullable = false)
    private LocalDateTime endedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected CouponPolicy() {
    }

    private CouponPolicy(String name, DiscountType discountType, long discountValue, long totalQuantity,
                        LocalDateTime startedAt, LocalDateTime endedAt) {
        this.name = name;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.totalQuantity = totalQuantity;
        this.issuedQuantity = 0;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }

    public static CouponPolicy create(String name, DiscountType discountType, long discountValue, long totalQuantity,
                                      LocalDateTime startedAt, LocalDateTime endedAt) {
        return new CouponPolicy(name, discountType, discountValue, totalQuantity, startedAt, endedAt);
    }

    public boolean isActive(LocalDateTime now) {
        return !now.isBefore(startedAt) && !now.isAfter(endedAt);
    }

    public long calculateDiscount(long originalPrice) {
        if (discountType == DiscountType.FIXED_AMOUNT) {
            return Math.min(discountValue, originalPrice);
        }
        return originalPrice * discountValue / 100;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public long getDiscountValue() {
        return discountValue;
    }

    public long getTotalQuantity() {
        return totalQuantity;
    }

    public long getIssuedQuantity() {
        return issuedQuantity;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }
}
