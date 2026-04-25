package com.example.orderstudy.service.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.orderstudy.exception.BusinessException;
import com.example.orderstudy.exception.ErrorCode;
import com.example.orderstudy.dto.coupon.CouponDtos;
import com.example.orderstudy.domain.coupon.CouponPolicy;
import com.example.orderstudy.repository.coupon.CouponPolicyRepository;
import com.example.orderstudy.service.coupon.CouponService;
import com.example.orderstudy.domain.coupon.DiscountType;
import com.example.orderstudy.domain.coupon.UserCoupon;
import com.example.orderstudy.repository.coupon.UserCouponRepository;
import com.example.orderstudy.domain.coupon.UserCouponStatus;
import com.example.orderstudy.domain.order.OrderStatus;
import com.example.orderstudy.domain.product.Product;
import com.example.orderstudy.repository.product.ProductRepository;
import com.example.orderstudy.domain.user.User;
import com.example.orderstudy.dto.order.OrderDtos;
import com.example.orderstudy.repository.order.OrderRepository;
import com.example.orderstudy.repository.user.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class OrderApplicationTest {
    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CouponPolicyRepository couponPolicyRepository;

    @Autowired
    UserCouponRepository userCouponRepository;

    @Autowired
    CouponService couponService;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAllInBatch();
        userCouponRepository.deleteAllInBatch();
        couponPolicyRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    void createOrderWithCoupon() {
        User user = userRepository.save(User.create("userA"));
        Product product = productRepository.save(Product.create("기계식 키보드", 100_000, 10));
        CouponPolicy policy = couponPolicyRepository.save(activePolicy("5000원 할인", DiscountType.FIXED_AMOUNT, 5_000, 100));
        UserCoupon coupon = userCouponRepository.save(UserCoupon.issue(user, policy, LocalDateTime.now()));

        OrderDtos.CreateOrderResponse response = orderService.create(
                new OrderDtos.CreateOrderRequest(user.getId(), product.getId(), 2, coupon.getId())
        );

        assertThat(response.originalPrice()).isEqualTo(200_000);
        assertThat(response.discountAmount()).isEqualTo(5_000);
        assertThat(response.finalPrice()).isEqualTo(195_000);
        assertThat(userCouponRepository.findById(coupon.getId()).orElseThrow().getStatus()).isEqualTo(UserCouponStatus.USED);
        assertThat(productRepository.findById(product.getId()).orElseThrow().getStockQuantity()).isEqualTo(8);
    }

    @Test
    void createOrderWithoutCoupon() {
        User user = userRepository.save(User.create("userB"));
        Product product = productRepository.save(Product.create("마우스", 30_000, 5));

        OrderDtos.CreateOrderResponse response = orderService.create(
                new OrderDtos.CreateOrderRequest(user.getId(), product.getId(), 2, null)
        );

        assertThat(response.originalPrice()).isEqualTo(60_000);
        assertThat(response.discountAmount()).isZero();
        assertThat(response.finalPrice()).isEqualTo(60_000);
    }

    @Test
    void outOfStockFails() {
        User user = userRepository.save(User.create("userC"));
        Product product = productRepository.save(Product.create("모니터", 200_000, 1));

        assertThatThrownBy(() -> orderService.create(new OrderDtos.CreateOrderRequest(user.getId(), product.getId(), 2, null)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.OUT_OF_STOCK);
    }

    @Test
    void cancelOrderRestoresStockAndCoupon() {
        User user = userRepository.save(User.create("userD"));
        Product product = productRepository.save(Product.create("노트북", 1_000_000, 3));
        CouponPolicy policy = couponPolicyRepository.save(activePolicy("10% 할인", DiscountType.PERCENTAGE, 10, 100));
        UserCoupon coupon = userCouponRepository.save(UserCoupon.issue(user, policy, LocalDateTime.now()));
        OrderDtos.CreateOrderResponse order = orderService.create(
                new OrderDtos.CreateOrderRequest(user.getId(), product.getId(), 1, coupon.getId())
        );

        OrderDtos.CancelOrderResponse response = orderService.cancel(order.orderId());

        assertThat(response.status()).isEqualTo(OrderStatus.CANCELED);
        assertThat(productRepository.findById(product.getId()).orElseThrow().getStockQuantity()).isEqualTo(3);
        assertThat(userCouponRepository.findById(coupon.getId()).orElseThrow().getStatus()).isEqualTo(UserCouponStatus.ISSUED);
    }

    @Test
    void sameOrderCanBeCanceledOnlyOnceConcurrently() throws Exception {
        User user = userRepository.save(User.create("cancel-user"));
        Product product = productRepository.save(Product.create("취소 상품", 10_000, 3));
        CouponPolicy policy = couponPolicyRepository.save(activePolicy("취소 쿠폰", DiscountType.FIXED_AMOUNT, 1_000, 100));
        UserCoupon coupon = userCouponRepository.save(UserCoupon.issue(user, policy, LocalDateTime.now()));
        OrderDtos.CreateOrderResponse order = orderService.create(
                new OrderDtos.CreateOrderRequest(user.getId(), product.getId(), 1, coupon.getId())
        );

        AtomicInteger success = new AtomicInteger();
        AtomicInteger failure = new AtomicInteger();
        runConcurrently(10, index -> {
            try {
                orderService.cancel(order.orderId());
                success.incrementAndGet();
            } catch (BusinessException exception) {
                failure.incrementAndGet();
            }
        });

        assertThat(success.get()).isEqualTo(1);
        assertThat(failure.get()).isEqualTo(9);
        assertThat(productRepository.findById(product.getId()).orElseThrow().getStockQuantity()).isEqualTo(3);
        assertThat(userCouponRepository.findById(coupon.getId()).orElseThrow().getStatus()).isEqualTo(UserCouponStatus.ISSUED);
    }

    @Test
    void duplicateCouponIssueFails() {
        User user = userRepository.save(User.create("userE"));
        CouponPolicy policy = couponPolicyRepository.save(activePolicy("선착순", DiscountType.FIXED_AMOUNT, 1_000, 100));
        couponService.issue(new CouponDtos.IssueCouponRequest(user.getId(), policy.getId()));

        assertThatThrownBy(() -> couponService.issue(new CouponDtos.IssueCouponRequest(user.getId(), policy.getId())))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATED_COUPON_ISSUE);
    }

    @Test
    void soldOutCouponIssueFails() {
        User user1 = userRepository.save(User.create("userF1"));
        User user2 = userRepository.save(User.create("userF2"));
        CouponPolicy policy = couponPolicyRepository.save(activePolicy("한장", DiscountType.FIXED_AMOUNT, 1_000, 1));
        couponService.issue(new CouponDtos.IssueCouponRequest(user1.getId(), policy.getId()));

        assertThatThrownBy(() -> couponService.issue(new CouponDtos.IssueCouponRequest(user2.getId(), policy.getId())))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.COUPON_SOLD_OUT);
    }

    @Test
    void concurrentCouponIssueDoesNotExceedTotalQuantity() throws Exception {
        int requestCount = 1_000;
        int totalQuantity = 100;
        CouponPolicy policy = couponPolicyRepository.save(activePolicy("100장", DiscountType.FIXED_AMOUNT, 1_000, totalQuantity));
        List<User> users = new ArrayList<>();
        for (int i = 0; i < requestCount; i++) {
            users.add(User.create("coupon-user-" + i));
        }
        List<User> savedUsers = userRepository.saveAll(users);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger failure = new AtomicInteger();
        runConcurrently(requestCount, index -> {
            try {
                couponService.issue(new CouponDtos.IssueCouponRequest(savedUsers.get(index).getId(), policy.getId()));
                success.incrementAndGet();
            } catch (BusinessException exception) {
                failure.incrementAndGet();
            }
        });

        CouponPolicy savedPolicy = couponPolicyRepository.findById(policy.getId()).orElseThrow();
        assertThat(success.get()).isEqualTo(totalQuantity);
        assertThat(failure.get()).isEqualTo(requestCount - totalQuantity);
        assertThat(savedPolicy.getIssuedQuantity()).isEqualTo(totalQuantity);
        assertThat(userCouponRepository.count()).isEqualTo(totalQuantity);
    }

    @Test
    void concurrentOrdersDoNotMakeStockNegative() throws Exception {
        int requestCount = 100;
        int stock = 50;
        Product product = productRepository.save(Product.create("한정 상품", 10_000, stock));
        List<User> users = new ArrayList<>();
        for (int i = 0; i < requestCount; i++) {
            users.add(User.create("order-user-" + i));
        }
        List<User> savedUsers = userRepository.saveAll(users);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger failure = new AtomicInteger();
        runConcurrently(requestCount, index -> {
            try {
                orderService.create(new OrderDtos.CreateOrderRequest(savedUsers.get(index).getId(), product.getId(), 1, null));
                success.incrementAndGet();
            } catch (BusinessException exception) {
                failure.incrementAndGet();
            }
        });

        Product savedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(success.get()).isEqualTo(stock);
        assertThat(failure.get()).isEqualTo(requestCount - stock);
        assertThat(savedProduct.getStockQuantity()).isZero();
    }

    @Test
    void sameCouponCanBeUsedOnlyOnceConcurrently() throws Exception {
        User user = userRepository.save(User.create("same-coupon-user"));
        Product product = productRepository.save(Product.create("동시성 상품", 10_000, 10));
        CouponPolicy policy = couponPolicyRepository.save(activePolicy("쿠폰", DiscountType.FIXED_AMOUNT, 1_000, 1));
        UserCoupon coupon = userCouponRepository.save(UserCoupon.issue(user, policy, LocalDateTime.now()));

        AtomicInteger success = new AtomicInteger();
        AtomicInteger failure = new AtomicInteger();
        runConcurrently(10, index -> {
            try {
                orderService.create(new OrderDtos.CreateOrderRequest(user.getId(), product.getId(), 1, coupon.getId()));
                success.incrementAndGet();
            } catch (BusinessException exception) {
                failure.incrementAndGet();
            }
        });

        assertThat(success.get()).isEqualTo(1);
        assertThat(failure.get()).isEqualTo(9);
        assertThat(userCouponRepository.findById(coupon.getId()).orElseThrow().getStatus()).isEqualTo(UserCouponStatus.USED);
        assertThat(productRepository.findById(product.getId()).orElseThrow().getStockQuantity()).isEqualTo(9);
    }

    private CouponPolicy activePolicy(String name, DiscountType discountType, long discountValue, long totalQuantity) {
        return CouponPolicy.create(
                name,
                discountType,
                discountValue,
                totalQuantity,
                LocalDateTime.now().minusMinutes(1),
                LocalDateTime.now().plusDays(1)
        );
    }

    private void runConcurrently(int count, ThrowingConsumer action) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(Math.min(count, 64));
        CountDownLatch ready = new CountDownLatch(count);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            int index = i;
            executorService.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    action.accept(index);
                } catch (Exception ignored) {
                } finally {
                    done.countDown();
                }
            });
        }
        ready.await(10, TimeUnit.SECONDS);
        start.countDown();
        done.await(60, TimeUnit.SECONDS);
        executorService.shutdown();
    }

    @FunctionalInterface
    private interface ThrowingConsumer {
        void accept(int index) throws Exception;
    }
}
