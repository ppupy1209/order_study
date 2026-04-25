package com.example.orderstudy.coupon;

import com.example.orderstudy.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_coupon",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_coupon_user_policy", columnNames = {"user_id", "coupon_policy_id"})
)
public class UserCoupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_policy_id", nullable = false)
    private CouponPolicy couponPolicy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserCouponStatus status;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    private LocalDateTime usedAt;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    protected UserCoupon() {
    }

    public UserCoupon(User user, CouponPolicy couponPolicy, LocalDateTime issuedAt) {
        this.user = user;
        this.couponPolicy = couponPolicy;
        this.status = UserCouponStatus.ISSUED;
        this.issuedAt = issuedAt;
        this.expiredAt = couponPolicy.getEndedAt();
    }

    public boolean isExpired(LocalDateTime now) {
        return expiredAt.isBefore(now);
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public CouponPolicy getCouponPolicy() {
        return couponPolicy;
    }

    public UserCouponStatus getStatus() {
        return status;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }
}
