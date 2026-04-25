package com.example.orderstudy.service.order;

import com.example.orderstudy.domain.coupon.UserCoupon;
import com.example.orderstudy.domain.coupon.UserCouponStatus;
import com.example.orderstudy.domain.order.Order;
import com.example.orderstudy.domain.order.OrderStatus;
import com.example.orderstudy.domain.product.Product;
import com.example.orderstudy.domain.user.User;
import com.example.orderstudy.dto.order.CancelOrderResponse;
import com.example.orderstudy.dto.order.CreateOrderRequest;
import com.example.orderstudy.dto.order.CreateOrderResponse;
import com.example.orderstudy.dto.order.OrderResponse;
import com.example.orderstudy.exception.BusinessException;
import com.example.orderstudy.exception.ErrorCode;
import com.example.orderstudy.repository.coupon.UserCouponRepository;
import com.example.orderstudy.repository.order.OrderRepository;
import com.example.orderstudy.repository.product.ProductRepository;
import com.example.orderstudy.repository.user.UserRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final UserCouponRepository userCouponRepository;
    private final Clock clock;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository, ProductRepository productRepository,
                        UserCouponRepository userCouponRepository, Clock clock) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.userCouponRepository = userCouponRepository;
        this.clock = clock;
    }

    @Transactional
    public CreateOrderResponse create(CreateOrderRequest request) {
        if (request.quantity() < 1) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_QUANTITY);
        }

        LocalDateTime now = LocalDateTime.now(clock);
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        int stockUpdated = productRepository.decreaseStock(product.getId(), request.quantity());
        if (stockUpdated == 0) {
            throw new BusinessException(ErrorCode.OUT_OF_STOCK);
        }

        UserCoupon userCoupon = null;
        long discountAmount = 0;
        long originalPrice = product.getPrice() * request.quantity();
        if (request.userCouponId() != null) {
            userCoupon = userCouponRepository.findWithUserAndPolicyById(request.userCouponId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_COUPON_NOT_FOUND));
            validateCoupon(userCoupon, user.getId(), now);

            int couponUpdated = userCouponRepository.markUsed(userCoupon.getId(), user.getId(), now);
            if (couponUpdated == 0) {
                throw new BusinessException(ErrorCode.COUPON_NOT_AVAILABLE);
            }
            discountAmount = userCoupon.getCouponPolicy().calculateDiscount(originalPrice);
        }

        long finalPrice = Math.max(originalPrice - discountAmount, 0);
        Order order = orderRepository.save(Order.create(user, product, userCoupon, request.quantity(), originalPrice, discountAmount, finalPrice));
        return CreateOrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long orderId) {
        Order order = orderRepository.findWithRelationsById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        return OrderResponse.from(order);
    }

    @Transactional
    public CancelOrderResponse cancel(Long orderId) {
        LocalDateTime now = LocalDateTime.now(clock);
        Order order = orderRepository.findWithRelationsById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new BusinessException(ErrorCode.ALREADY_CANCELED_ORDER);
        }

        int canceled = orderRepository.cancel(order.getId(), now);
        if (canceled == 0) {
            throw new BusinessException(ErrorCode.ALREADY_CANCELED_ORDER);
        }

        productRepository.increaseStock(order.getProduct().getId(), order.getQuantity());
        if (order.getUserCoupon() != null) {
            userCouponRepository.restoreIssued(order.getUserCoupon().getId());
        }
        return new CancelOrderResponse(order.getId(), OrderStatus.CANCELED, now);
    }

    private void validateCoupon(UserCoupon userCoupon, Long userId, LocalDateTime now) {
        if (!userCoupon.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.COUPON_OWNER_MISMATCH);
        }
        if (userCoupon.getStatus() != UserCouponStatus.ISSUED) {
            throw new BusinessException(ErrorCode.COUPON_NOT_AVAILABLE);
        }
        if (userCoupon.isExpired(now)) {
            throw new BusinessException(ErrorCode.COUPON_EXPIRED);
        }
    }
}
