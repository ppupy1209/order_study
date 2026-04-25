package com.example.orderstudy.domain.order;

import com.example.orderstudy.domain.coupon.UserCoupon;
import com.example.orderstudy.domain.product.Product;
import com.example.orderstudy.domain.user.User;
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
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_coupon_id")
    private UserCoupon userCoupon;

    @Column(nullable = false)
    private long quantity;

    @Column(nullable = false)
    private long originalPrice;

    @Column(nullable = false)
    private long discountAmount;

    @Column(nullable = false)
    private long finalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime canceledAt;

    protected Order() {
    }

    private Order(User user, Product product, UserCoupon userCoupon, long quantity,
                 long originalPrice, long discountAmount, long finalPrice) {
        this.user = user;
        this.product = product;
        this.userCoupon = userCoupon;
        this.quantity = quantity;
        this.originalPrice = originalPrice;
        this.discountAmount = discountAmount;
        this.finalPrice = finalPrice;
        this.status = OrderStatus.CREATED;
    }

    public static Order create(User user, Product product, UserCoupon userCoupon, long quantity,
                               long originalPrice, long discountAmount, long finalPrice) {
        return new Order(user, product, userCoupon, quantity, originalPrice, discountAmount, finalPrice);
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Product getProduct() {
        return product;
    }

    public UserCoupon getUserCoupon() {
        return userCoupon;
    }

    public long getQuantity() {
        return quantity;
    }

    public long getOriginalPrice() {
        return originalPrice;
    }

    public long getDiscountAmount() {
        return discountAmount;
    }

    public long getFinalPrice() {
        return finalPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCanceledAt() {
        return canceledAt;
    }
}
