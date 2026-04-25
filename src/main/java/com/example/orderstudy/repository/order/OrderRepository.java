package com.example.orderstudy.repository.order;

import com.example.orderstudy.domain.order.Order;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph(attributePaths = {"user", "product", "userCoupon"})
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findWithRelationsById(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Order o
               set o.status = com.example.orderstudy.domain.order.OrderStatus.CANCELED,
                   o.canceledAt = :now
             where o.id = :orderId
               and o.status = com.example.orderstudy.domain.order.OrderStatus.CREATED
            """)
    int cancel(@Param("orderId") Long orderId, @Param("now") LocalDateTime now);
}
